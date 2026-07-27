-- ============================================================================
-- TICKET-ADV008 — REFRESH the daily-summary materialised view (concurrent so it can
--         run while the dashboard is reading it)
-- ============================================================================
REFRESH MATERIALIZED VIEW CONCURRENTLY mv_daily_recon_summary;


-- TICKET-ADV010: VWAP per instrument per day (window function, no GROUP BY collapse)
SELECT
    t.trade_ref,
    t.trade_date,
    i.symbol,
    t.quantity,
    t.price,
    t.quantity * t.price AS notional,
    SUM(t.price * t.quantity) OVER (
        PARTITION BY t.instrument_id, t.trade_date
    ) / NULLIF(SUM(t.quantity) OVER (
        PARTITION BY t.instrument_id, t.trade_date
    ), 0) AS vwap,
    ROW_NUMBER() OVER (
        PARTITION BY t.instrument_id, t.trade_date
        ORDER BY t.created_at
    ) AS trade_seq,
    SUM(t.quantity) OVER (
        PARTITION BY t.instrument_id, t.trade_date
        ORDER BY t.created_at
        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
    ) AS running_quantity
FROM trades t
JOIN instruments i ON i.id = t.instrument_id
ORDER BY t.instrument_id, t.trade_date, trade_seq;

-- TICKET-ADV011: Recursive CTE - trade lifecycle rollup
WITH RECURSIVE trade_lifecycle AS (
    -- Base case: every trade starts at stage 1 = EXECUTION
    SELECT
        t.id AS trade_id,
        1 AS stage,
        'EXECUTION' AS stage_name,
        t.created_at AS event_at,
        'COMPLETED' AS event_status
    FROM trades t

    UNION ALL

    -- Recursive step: advance one stage at a time
    SELECT
        tl.trade_id,
        tl.stage + 1 AS stage,
        next_stage.stage_name,
        next_stage.event_at,
        next_stage.event_status
    FROM trade_lifecycle tl
    JOIN LATERAL (
        SELECT 'CONFIRMATION' AS stage_name, tl.event_at + INTERVAL '1 hour'  AS event_at, 'COMPLETED' AS event_status WHERE tl.stage = 1
        UNION ALL
        SELECT 'SETTLEMENT',   tl.event_at + INTERVAL '1 day',   'COMPLETED' WHERE tl.stage = 2
        UNION ALL
        SELECT 'RECON_BREAK',  tl.event_at + INTERVAL '2 days',  'OPEN'      WHERE tl.stage = 3
        UNION ALL
        SELECT 'RESOLUTION',   tl.event_at + INTERVAL '3 days',  'CLOSED'    WHERE tl.stage = 4
    ) next_stage ON TRUE
    WHERE tl.stage < 5
)
SELECT trade_id, stage, stage_name, event_at, event_status
FROM trade_lifecycle
ORDER BY trade_id, stage;
