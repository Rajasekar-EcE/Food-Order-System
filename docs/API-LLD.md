# API Low-Level Design

## Overview

Four services. `order-service` is the only one with a database-writing public entry point for
placing orders and the only one embedding the Camunda engine. `payment-service`,
`kitchen-service`, and `delivery-service` each expose **two** ways in:

1. A **Camunda external task worker** (background, polls `/engine-rest`, drives the real
   order lifecycle end-to-end).
2. A **direct REST API** (foreground, for independently testing/demoing that service without
   needing the full workflow running).

Both paths call the same underlying `*Service` class, so behavior is identical either way — the
only difference is whether the result is fed back into a running process instance and whether the
order's dashboard status gets updated.

---

## order-service (port 8081)

| Method | Path | Description |
|---|---|---|
| POST | `/api/orders` | Create an order. Saves to DB, publishes `OrderCreatedMessage` to the `order.created` ActiveMQ queue. Returns the created `Order`. |
| GET | `/api/orders` | List all orders (used by the polling dashboard). |
| GET | `/api/orders/{id}` | Fetch a single order. |
| PUT | `/api/orders/{id}/status` | Update an order's status. Called internally by Payment/Kitchen/Delivery services as the workflow progresses. Body: `{ "status": "PAYMENT_PROCESSING" }`. |

**Request body — POST /api/orders**
```json
{
  "customerName": "Asha Rao",
  "customerAddress": "12 MG Road, Bengaluru",
  "items": "1x Margherita Pizza, 2x Garlic Bread",
  "amount": 24.99
}
```

**Response — Order**
```json
{
  "id": 1,
  "customerName": "Asha Rao",
  "customerAddress": "12 MG Road, Bengaluru",
  "items": "1x Margherita Pizza, 2x Garlic Bread",
  "amount": 24.99,
  "status": "PLACED",
  "processInstanceId": "a1b2c3d4-...",
  "createdAt": "2026-07-14T10:00:00",
  "updatedAt": "2026-07-14T10:00:00"
}
```

Also exposes Camunda's own REST API at `/engine-rest/*` (used by the external task workers)
and the Cockpit/Tasklist web apps at `/camunda` (login `demo`/`demo`).

---

## payment-service (port 8082)

**Camunda external task worker** — subscribes to topic `payment-processing`. Input variables:
`orderId`, `amount`. On completion, sets process variable `paymentStatus` to `SUCCESS` or
`FAILED` (~85% success rate, mocked), and — on this path only — pushes
`PAYMENT_PROCESSING` then (on failure) `CANCELLED` to order-service.

**Direct REST API:**

| Method | Path | Description |
|---|---|---|
| POST | `/api/payments` | Process a payment standalone. Body: `{ "orderId": 1, "amount": 24.99 }`. Does **not** touch order-service or any process instance. |
| GET | `/api/payments/{orderId}` | Latest payment record for an order. |
| GET | `/api/payments` | List all payment records. |

**Response — Payment**
```json
{ "id": 5, "orderId": 1, "amount": 24.99, "status": "SUCCESS", "transactionId": "9f2e...", "createdAt": "2026-07-14T10:00:05" }
```

---

## kitchen-service (port 8083)

**Camunda external task worker** — subscribes to topic `kitchen-preparation`. Input variables:
`orderId`, `items`. Simulates a 5-20s prep time, always completes as `READY`, pushes
`KITCHEN_PREP` to order-service on the workflow path.

**Direct REST API:**

| Method | Path | Description |
|---|---|---|
| POST | `/api/kitchen/tickets` | Create + prepare a ticket standalone. Body: `{ "orderId": 1, "items": "1x Pizza" }`. |
| GET | `/api/kitchen/tickets/{orderId}` | Latest ticket for an order. |
| PUT | `/api/kitchen/tickets/{orderId}/status` | Manually override a ticket's status (demo convenience). |
| GET | `/api/kitchen/tickets` | List all tickets. |

---

## delivery-service (port 8084)

**Camunda external task worker** — subscribes to topic `delivery-assignment`. Input variable:
`orderId`. Assigns a random mock driver, pushes `OUT_FOR_DELIVERY` then `DELIVERED` to
order-service on the workflow path.

**Direct REST API:**

| Method | Path | Description |
|---|---|---|
| POST | `/api/deliveries` | Assign + complete a delivery standalone. Body: `{ "orderId": 1 }`. |
| GET | `/api/deliveries/{orderId}` | Latest delivery record for an order. |
| GET | `/api/deliveries` | List all delivery records. |

---

## Queue contract — `order.created` (ActiveMQ)

Published by `order-service` after an order is saved. Consumed by `order-service` itself (not
the downstream services) to start the `orderProcess` Camunda process instance — the queue exists
to decouple "order saved" from "workflow started" and to mirror a realistic event-driven
boundary, even though both ends live in the same service in this simplified build.

```json
{ "orderId": 1, "items": "1x Margherita Pizza, 2x Garlic Bread", "amount": 24.99 }
```

## Note on the "direct REST" endpoints

Hitting a downstream service's direct REST endpoint exercises its business logic and persists a
record exactly like the workflow path does, but it does **not** advance any Camunda process
instance (there's no external task to complete) and, in payment-service's and kitchen-service's
case, does not push a status update to order-service. This is intentional — it's a testing/demo
convenience, not an alternate way of running a real order to completion.
