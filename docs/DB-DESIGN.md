# Database Design

## Simplification note

All four tables live in a single shared MySQL schema (`food_order_system`) rather than
physically separate databases per service. In a production microservices setup each service
would own its own schema/database and never read another's tables directly; here that's
simplified for local dev convenience. Note this is enforced at the *ownership* level even in
this build — each service's JPA repository only ever writes to its own table(s); cross-service
reads always go through the owning service's REST API, never direct SQL joins.

Camunda's own engine tables (`ACT_*`) live in the same schema and are auto-created/managed by
`order-service` on first boot via `camunda.bpm.database.schema-update: true`.

## ER overview

```
orders (1) ───< payments (many)
orders (1) ───< kitchen_tickets (many)
orders (1) ───< deliveries (many)
```

One order can, in principle, have more than one payment/ticket/delivery row (e.g. a retried
direct-REST test call), so each downstream table stores `order_id` as a plain foreign key rather
than a 1:1 shared primary key, and services fetch the *latest* row by `created_at` when asked
"what's the current state for this order".

## Tables

### `orders` — owned by order-service
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | |
| customer_name | VARCHAR(120) | |
| customer_address | VARCHAR(255) | |
| items | VARCHAR(1000) | free-text item list |
| amount | DECIMAL(10,2) | |
| status | VARCHAR(30) | PLACED / PAYMENT_PROCESSING / KITCHEN_PREP / OUT_FOR_DELIVERY / DELIVERED / CANCELLED |
| process_instance_id | VARCHAR(64) | Camunda process instance backing this order |
| created_at | TIMESTAMP | default now |
| updated_at | TIMESTAMP | auto-updated on write |

### `payments` — owned by payment-service
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | |
| order_id | BIGINT FK → orders.id | |
| amount | DECIMAL(10,2) | |
| status | VARCHAR(30) | SUCCESS / FAILED |
| transaction_id | VARCHAR(64) | mock UUID |
| created_at | TIMESTAMP | |

### `kitchen_tickets` — owned by kitchen-service
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | |
| order_id | BIGINT FK → orders.id | |
| items | VARCHAR(1000) | |
| status | VARCHAR(30) | PREPARING / READY |
| prep_time_seconds | INT | simulated prep duration |
| created_at | TIMESTAMP | |

### `deliveries` — owned by delivery-service
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | |
| order_id | BIGINT FK → orders.id | |
| driver_name | VARCHAR(120) | mock driver pool |
| status | VARCHAR(30) | ASSIGNED / DELIVERED |
| created_at | TIMESTAMP | |

## Why foreign keys exist despite service-owned tables

The FKs (`payments.order_id → orders.id`, etc.) are enforced at the DB level purely as a data
-integrity safety net for this single-schema simplified setup. In a real separate-databases
deployment there would be no cross-database FK constraint — referential integrity would instead
be an application-level concern (or eventual consistency via events), which is called out in the
README's "Known simplifications" section.
