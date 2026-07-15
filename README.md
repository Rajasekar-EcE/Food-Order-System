# Online Food Order Processing System

A practice implementation of the Waffor take-home assessment: React frontend + 4 Spring Boot
microservices, orchestrated by an embedded Camunda BPMN workflow, communicating via ActiveMQ
and MySQL.

## Architecture

```
React UI --REST--> Order Service --publish--> ActiveMQ (order.created)
                        |                            |
                   (embeds Camunda)  <----consume-----+
                        |
                Camunda starts "orderProcess"
                        |
        +---------------+----------------+
        |               |                |
  Payment Service  Kitchen Service  Delivery Service
  (external task   (external task   (external task
   worker, polls    worker)          worker)
   Camunda REST)
```

Payment/Kitchen/Delivery Services never talk to each other or to the Order Service's database
directly — they only talk to the Camunda REST API (`/engine-rest`) as **external task workers**,
and call the Order Service's `PUT /api/orders/{id}/status` endpoint to push real-time status to
the dashboard.

**Each of the three downstream services also exposes its own direct REST API**, independent of
Camunda, so you can test or demo each piece in isolation (e.g. `POST /api/payments` on
payment-service) without running the full order workflow. Both the external-task-worker path and
the direct REST path call the same underlying service class, so behavior is identical — the
direct path just doesn't advance a real process instance or push status to the dashboard. See
`docs/API-LLD.md` for the full endpoint list, and the frontend's "Test Services Independently"
tab for a UI to try them.

## Prerequisites (install locally — not available in this sandbox)

1. **Java 17+** and **Maven 3.8+**
2. **MySQL 8** running on `localhost:3306`, root password `root` (or edit each service's
   `application.yml` to match your credentials)
3. **ActiveMQ** (Classic) running on `localhost:61616` — download from
   https://activemq.apache.org/components/classic/download/ and run
   `./bin/activemq start`. The web console (http://localhost:8161) defaults to user/pass
   `admin`/`admin`.
4. **Node.js 18+** and npm, for the React frontend.

## Setup

### 1. Database
```bash
mysql -u root -p < db/schema.sql
```
(The `orders`, `payments`, `kitchen_tickets`, `deliveries` tables are created here; Camunda's
own engine tables are auto-created by the Order Service on first boot.)

### 2. Start ActiveMQ
```bash
/path/to/apache-activemq-x.x.x/bin/activemq start
```

### 3. Build everything
From the project root:
```bash
mvn clean install
```

### 4. Run the services (each in its own terminal, in this order)
```bash
cd order-service    && mvn spring-boot:run   # port 8081 — start first, hosts Camunda engine
cd payment-service  && mvn spring-boot:run   # port 8082
cd kitchen-service   && mvn spring-boot:run   # port 8083
cd delivery-service  && mvn spring-boot:run   # port 8084
```
Wait for Order Service to fully start (it boots the Camunda engine + REST API) before starting
the other three, since they poll `http://localhost:8081/engine-rest` for external tasks.

Camunda's Cockpit/Tasklist web apps (optional, for visually inspecting running process
instances) will be at http://localhost:8081/camunda — login `demo`/`demo`.

### 5. Run the frontend
```bash
cd frontend
npm install
npm start
```
Opens at http://localhost:3000.

## Trying it out
1. Open http://localhost:3000, fill in the order form, click **Place Order**.
2. Watch the dashboard update every 2 seconds as the order moves through
   `PLACED → PAYMENT_PROCESSING → KITCHEN_PREP → OUT_FOR_DELIVERY → DELIVERED`
   (or straight to `CANCELLED` if the mocked payment fails — happens ~15% of the time).
3. Watch each service's console for log lines like:
   ```
   [OrderService] Order #1 - Status: PLACED, saved to DB
   [OrderService] Order #1 - Published to 'order.created' queue
   [OrderService] Order #1 - Workflow started (processInstanceId=...)
   [PaymentService] Order #1 - Payment processing... SUCCESS
   [KitchenService] Order #1 - Kitchen ticket created, preparing food... READY
   [DeliveryService] Order #1 - Driver Priya assigned, delivering... DELIVERED
   [OrderService] Order #1 - Workflow COMPLETE
   ```

## Trying each service standalone

You don't need Camunda or ActiveMQ running to smoke-test an individual service — just that
service and MySQL:

```bash
# Payment service (port 8082)
curl -X POST http://localhost:8082/api/payments -H "Content-Type: application/json" \
  -d '{"orderId": 1, "amount": 24.99}'
curl http://localhost:8082/api/payments/1

# Kitchen service (port 8083)
curl -X POST http://localhost:8083/api/kitchen/tickets -H "Content-Type: application/json" \
  -d '{"orderId": 1, "items": "1x Pizza"}'

# Delivery service (port 8084)
curl -X POST http://localhost:8084/api/deliveries -H "Content-Type: application/json" \
  -d '{"orderId": 1}'
```

Or use the "Test Services Independently" tab in the React frontend, which wraps these same
calls in a small form per service.

## Project layout
```
food-order-system/
├── db/schema.sql                  # MySQL schema
├── docs/API-LLD.md                # API + queue contract documentation
├── docs/DB-DESIGN.md              # DB schema + ER diagram
├── order-service/                 # REST API + Camunda engine + ActiveMQ producer/consumer
├── payment-service/                # External task worker: payment-processing
├── kitchen-service/                 # External task worker: kitchen-preparation
├── delivery-service/                # External task worker: delivery-assignment
└── frontend/                       # React order form + polling dashboard
```

## Deliverables checklist mapping
- **Deliverable 1 (API LLD)** → `docs/API-LLD.md`
- **Deliverable 2 (DB Design)** → `docs/DB-DESIGN.md`
- **Deliverable 3 (Frontend screenshots)** → take these yourself after running `npm start`
- **Deliverable 4 (Log output)** → copy your terminal output after placing an order
- **Deliverable 5 (AI-generated Implementation Report)** → once you've run everything, paste your
  workspace into an AI tool with the report prompt from the assessment PDF (Section 7)

## Known simplifications (be upfront about these in your interview)
- No retry/outbox pattern if ActiveMQ publish fails after the order is saved.
- No idempotency key — resubmitting identical form data creates a new order each time (as
  intended, no dedup requirement was specified).
- Payment success/failure is a random mock (~85% success), not a real payment gateway.
- Single shared MySQL schema instead of physically separate databases per service (documented
  as a deliberate simplification for local dev in `docs/DB-DESIGN.md`).
