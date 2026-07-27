# 2. Use JSONB for Instrument Metadata

## Status
Accepted

## Context
Instruments across asset classes (Equities, FX, Fixed Income, Derivatives) have widely varying attributes. Using dedicated table columns for every field results in sparse, wide tables and frequent schema migration requirements.

## Decision
Use PostgreSQL \JSONB\ column named \metadata\ on the \instruments\ table for flexible attributes.

## Consequences
### Positive
- Flexible schema that accommodates new instrument types without DDL migrations.
- Efficient binary representation and querying in PostgreSQL.

### Negative
- Less static typing enforcement at the database level.
- Requires specialized GIN indexing for high-performance JSON query predicates.
