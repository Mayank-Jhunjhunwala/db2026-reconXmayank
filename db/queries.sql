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
