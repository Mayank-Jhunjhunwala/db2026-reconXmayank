# Architecture Decision Records (ADR) Prompt Template

Use the following prompt template when asking AI tools (such as Claude) to draft new ADRs for ReconX:

\\\markdown
Act as a Principal Software & Database Architect. Draft an Architecture Decision Record (ADR) for project ReconX in the Michael Nygard format.

Context & Scale Constraints:
- System processes 50,000 trades/day with a 91M-row steady-state table size.
- Low-latency query requirements for intraday analytics and reconciliation.

Decision Topic: [INSERT TOPIC HERE]

Structure Required:
1. Title
2. Status (Accepted / Proposed / Rejected)
3. Context (Problem statement, scale, alternatives considered)
4. Decision (Clear declaration of choice)
5. Consequences (Positive outcomes and trade-offs/negatives)
\\\
"@

Set-Content -Path "docs/adr/0001-partition-by-trade-date.md" -Encoding UTF8 -Value @"
# 1. Partition Trades Table by Trade Date

## Status
Accepted

## Context
ReconX manages large volumes of trade data (50,000 trades/day, target 91M rows over 5 years). Queries primarily run against date ranges (e.g., intraday recon, daily analytics). Monolithic tables lead to index bloat and degraded write/read performance.

## Decision
Partition the \	rades\ table by range on \	rade_date\ using monthly child partitions.

## Consequences
### Positive
- Substantially faster range queries via partition pruning.
- Efficient bulk drop/archive of historical partitions without heavy DELETE locks.

### Negative
- Primary keys must include \	rade_date\.
- Cross-partition queries without a date filter can be slower than on a flat table.
