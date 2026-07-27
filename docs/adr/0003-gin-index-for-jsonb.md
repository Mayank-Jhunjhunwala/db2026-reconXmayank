# 3. GIN Index with jsonb_path_ops for JSONB Metadata

## Status
Accepted

## Context
Querying nested JSONB metadata in \instruments\ using standard B-tree indexes is not possible. Standard GIN indexes index both keys and values, resulting in larger index sizes and slower write performance.

## Decision
Implement a GIN index on \instruments(metadata)\ using the \jsonb_path_ops\ operator class.

## Consequences
### Positive
- Faster index lookups for containment queries (\@>\).
- Smaller index size compared to default \jsonb_ops\.

### Negative
- Does not support queries checking for standalone key existence (\?\ operator), only value containment.
