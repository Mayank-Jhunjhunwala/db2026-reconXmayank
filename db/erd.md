# ReconX Entity-Relationship Diagram (ERD)

This document defines the core 8-entity database schema for the **ReconX** platform. It serves as the primary reference for database partitioning (TICKET-ADV007), JSONB column structures (TICKET-ADV009), soft deletes (TICKET-ADV067), and foreign key join paths across services.

---

## ER Diagram (Mermaid)

```mermaid
erDiagram
    COUNTERPARTIES ||--o{ TRADES : "executes"
    INSTRUMENTS    ||--o{ TRADES : "covers"
    TRADES         ||--o{ SETTLEMENTS : "settles via"
    TRADES         ||--o{ RECON_BREAKS : "may produce"
    RECON_JOBS     ||--o{ RECON_BREAKS : "detected by"
    USERS          ||--o{ AUDIT_LOG : "actor"
    TRADES         ||--o{ AUDIT_LOG : "audited"

    COUNTERPARTIES {
        bigint id PK
        varchar name
        char lei_code UK
        varchar region
    }

    INSTRUMENTS {
        bigint id PK
        varchar symbol UK
        varchar name
        varchar asset_class
        char currency
        char isin UK
        jsonb metadata "TICKET-ADV009"
    }

    TRADES {
        bigint id PK
        varchar trade_ref UK
        bigint instrument_id FK
        bigint counterparty_id FK
        varchar asset_class
        varchar side
        numeric quantity
        numeric price
        date trade_date "PARTITION KEY (TICKET-ADV007)"
        varchar status
        timestamp deleted_at "TICKET-ADV067 soft delete"
        timestamp created_at
        timestamp modified_at
    }

    SETTLEMENTS {
        bigint id PK
        bigint trade_id FK
        date settlement_date
        numeric amount
        varchar status
    }

    RECON_BREAKS {
        bigint id PK
        bigint trade_id FK
        varchar discrepancy_type
        varchar status
        timestamp detected_at
        timestamp resolved_at
        varchar resolution_note
    }

    RECON_JOBS {
        bigint id PK
        varchar job_id UK
        date from_date
        date to_date
        varchar status
        timestamp started_at
        timestamp finished_at
        int trades_processed
        int breaks_detected
    }

    AUDIT_LOG {
        bigint id PK
        varchar event_id UK
        varchar trade_ref
        varchar event_type
        timestamp event_timestamp
        varchar actor
        clob before_state
        clob after_state
    }

    USERS {
        bigint id PK
        varchar email UK
        varchar password_hash
        varchar role
        boolean enabled
        timestamp created_at
    }
```

---

## Entity Details & Special Annotations

1. **`TRADES`**:
   - **`trade_date`**: Partition key for range partitioning by date (`TICKET-ADV007`).
   - **`deleted_at`**: Supports soft-delete semantics (`TICKET-ADV067`).
2. **`INSTRUMENTS`**:
   - **`metadata`**: `jsonb` field indexed using GIN (`jsonb_path_ops`) for flexible containment queries (`TICKET-ADV009`).
3. **`AUDIT_LOG`**:
   - Tracks mutations across trade lifecycle events and user actions.