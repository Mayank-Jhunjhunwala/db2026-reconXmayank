

-- TICKET-ADV017: Deterministic seed data (dev/test only)
-- 10 counterparties, 50 instruments, 500 trades spread across 4 monthly partitions

-- 1. Counterparties (10, spanning all 4 regions)
INSERT INTO counterparties (id, name, lei_code, region) VALUES
    (1,  'Meridian Capital',      'LEI0000000000000001', 'NAMR'),
    (2,  'Falcon Trading LLC',    'LEI0000000000000002', 'NAMR'),
    (3,  'Northgate Partners',    'LEI0000000000000003', 'NAMR'),
    (4,  'Alpine Financial',      'LEI0000000000000004', 'EMEA'),
    (5,  'Ravenswood Securities', 'LEI0000000000000005', 'EMEA'),
    (6,  'Solstice Markets',      'LEI0000000000000006', 'EMEA'),
    (7,  'Pacific Rim Holdings',  'LEI0000000000000007', 'APAC'),
    (8,  'Kestrel Asset Mgmt',    'LEI0000000000000008', 'APAC'),
    (9,  'Andes Capital Group',   'LEI0000000000000009', 'LATAM'),
    (10, 'Cordillera Trading',    'LEI0000000000000010', 'LATAM');

-- 2. Instruments: 5 explicit (one per asset class, including the two ADV009 needs) + 45 generated
INSERT INTO instruments (id, symbol, name, asset_class, currency, isin) VALUES
    (1, 'SAP.DE', 'SAP SE',                 'Equity',     'EUR', 'DE0007164600'),
    (2, 'UST10',  'US Treasury 10Y',        'Bond',       'USD', 'US0000000002'),
    (3, 'EURUSD', 'Euro / US Dollar',       'FX',         'USD', 'US0000000003'),
    (4, 'CL_FUT', 'WTI Crude Oil Future',   'Commodity',  'USD', 'US0000000004'),
    (5, 'ESZ26',  'E-mini S&P 500 Future',  'Derivative', 'USD', 'US0000000005');

INSERT INTO instruments (id, symbol, name, asset_class, currency, isin)
SELECT
    n + 5                                   AS id,
    'INST-' || LPAD((n + 5)::text, 3, '0')  AS symbol,
    'Generated Instrument ' || (n + 5)      AS name,
    (ARRAY['Equity','Bond','FX','Commodity','Derivative'])[1 + (n % 5)] AS asset_class,
    'USD'                                    AS currency,
    'US' || LPAD((n + 5)::text, 10, '0')    AS isin
FROM generate_series(1, 45) AS n;

-- 3. Trades: 500 rows spread across April-July 2026 (4 monthly partitions, ~125 each)
INSERT INTO trades (id, trade_ref, counterparty_id, instrument_id, trade_date, quantity, price, created_at)
SELECT
    n                                              AS id,
    'TRD-' || LPAD(n::text, 6, '0')                AS trade_ref,
    1 + (n % 10)                                    AS counterparty_id,
    1 + (n % 50)                                    AS instrument_id,
    DATE '2026-04-01' + ((n % 120) * INTERVAL '1 day') AS trade_date,
    (10 + (n % 90)) * 10                            AS quantity,
    100 + (n % 500) * 0.37                          AS price,
    (DATE '2026-04-01' + ((n % 120) * INTERVAL '1 day'))::timestamp + (n || ' minutes')::interval AS created_at
FROM generate_series(1, 500) AS n;

-- 4. A handful of recon_breaks against a subset of trades
INSERT INTO recon_breaks (trade_id, status, reason)
SELECT
    id,
    (ARRAY['UNMATCHED', 'DISPUTED'])[1 + (id % 2)] AS status,
    CASE WHEN id % 2 = 0 THEN 'Counterparty confirmation missing'
         ELSE 'Price mismatch vs external feed' END AS reason
FROM trades
WHERE id IN (7, 23, 89, 154, 201, 267, 333, 410, 478, 499);

-- 5. Verification queries
SELECT count(*) FROM counterparties;   -- expect 10
SELECT count(*) FROM instruments;      -- expect 50
SELECT count(*) FROM trades;           -- expect 500
SELECT count(*), DATE_TRUNC('month', trade_date) AS month
FROM trades
GROUP BY 2
ORDER BY 2;                             -- expect ~125 per month

-- TICKET-ADV009: Sample JSONB payloads (runs after seed data, so symbols now exist)
UPDATE instruments SET metadata = '{
  "sector": "Technology",
  "exchange": "XETR",
  "issuer": {"name": "SAP SE", "country": "DE", "lei": "529900D6BF99LW9R2E68"},
  "rating": {"sp": "AA-", "moody": "Aa3"},
  "tags": ["DAX40", "ESG-tier-1"]
}'::JSONB WHERE symbol = 'SAP.DE';

UPDATE instruments SET metadata = '{
  "sector": "Energy",
  "underlying": "WTI",
  "contractSize": 1000,
  "expiryMonth": "2026-12",
  "tags": ["futures", "physical-settlement"]
}'::JSONB WHERE symbol = 'CL_FUT';

-- Containment (uses GIN):
SELECT symbol, metadata->>'sector' AS sector
FROM instruments
WHERE metadata @> '{"sector": "Technology"}';

-- Path extraction:
SELECT symbol, metadata->'issuer'->>'country' AS country FROM instruments;

-- Array membership:
SELECT symbol FROM instruments WHERE metadata->'tags' ? 'DAX40';

-- Existence:
SELECT symbol FROM instruments WHERE metadata ? 'rating';
