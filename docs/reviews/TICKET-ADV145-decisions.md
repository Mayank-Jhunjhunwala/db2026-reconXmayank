# TICKET-ADV145 Kafka consumer config review

## AI Review Prompt Used
```text
Review the following Spring Kafka consumer configuration for production readiness. Flag any missing or risky settings in these areas:
  (1) backpressure & poll tuning,
  (2) error handling, retry & DLQ,
  (3) idempotence and exactly-once semantics,
  (4) observability — metrics, logging, traces,
  (5) security — TLS, SASL, ACLs.

For each finding, give the concrete config key, the recommended value, and a one-line justification. Do NOT rewrite the whole file — just list findings.

Application context: trade reconciliation service, ~500 events/sec, strict audit requirements.
```

## Decisions Table

| # | Area          | Finding                                                  | Recommendation              | Decision | Rationale                                          |
|---|---------------|----------------------------------------------------------|-----------------------------|----------|----------------------------------------------------|
| 1 | Backpressure  | max.poll.records default 500 risks max.poll.interval.ms  | Set to 100                  | Accept   | Slow downstream — keeps poll loop responsive       |
| 2 | Error handling| ExponentialBackOff has no jitter                         | Add jitter (custom BackOff) | Defer    | Logged as backlog item; out of scope today         |
| 3 | Idempotence   | Producer enable.idempotence not asserted                 | enable.idempotence: true    | Accept   | Cheap insurance against duplicate sends            |
| 4 | Observability | Metrics tags missing spring.application.name             | Add tags.application        | Accept   | Prevents collisions across services in Prometheus  |
| 5 | Security      | bootstrap-servers is PLAINTEXT                           | Use SASL_SSL in prod        | Reject   | Known dev gap; tracked separately for Day 10       |
