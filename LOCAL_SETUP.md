# Local Environment Setup

This guide explains how to run the complete Gym CRM system locally.

The system consists of:

* **gym-crm** — main API on port `8080`, with PostgreSQL on `5432`.
* **trainer-workload-service** — trainer workload service on port `8082`, with PostgreSQL on `5433` and a shared
  ActiveMQ broker on `61616` (web console on `8161`).

`gym-crm` communicates with `trainer-workload-service` asynchronously through ActiveMQ: gym-crm publishes workload
events to a queue, and trainer-workload-service consumes them.

> `discovery-server` (Eureka) is **not required** for this flow. It's a separate, optional module kept around only
> in case a future service needs HTTP-based discovery.

## Prerequisites

* JDK 25
* Docker
* Both repositories cloned locally (`gym-crm`, `trainer-workload-service`)

The repositories can be located anywhere. The commands below assume they are sibling directories.

## 1. Start trainer-workload-service (brings up PostgreSQL + ActiveMQ)

```bash
cd ../trainer-workload-service
docker compose up -d
./gradlew bootRun
```

No environment variables are required for local development.

This also starts the shared ActiveMQ broker used by both services. Web console: `http://localhost:8161`
(user/password: `admin`/`admin`).

## 2. Start gym-crm

```bash
cd ../gym-crm
docker compose up -d
./gradlew bootRun
```

No environment variables are required for local development.

> gym-crm and trainer-workload-service must point at the same broker. Both default to
> `ACTIVEMQ_BROKER_URL=tcp://localhost:61616`,
> which matches the container started in step 1.

## Smoke Test

Use Swagger UI to verify the main end-to-end flow:

`http://localhost:8080/swagger-ui/index.html`

1. **Register a trainer** using `POST /api/v1/trainers`.
2. **Register a trainee** using `POST /api/v1/trainees`.
3. **Log in as a trainer** using `POST /api/v1/auth/login` and copy the returned JWT.
4. Click **Authorize** in Swagger UI and enter the JWT.
5. **Add a training** using `POST /api/v1/trainings`.
6. Check the `trainer-workload-service` logs and verify a workload event was consumed and the training duration was
   added to the trainer's monthly workload. You can inspect the `trainer-workload.events` queue (and the
   `trainer-workload.events.dlq` dead letter queue) via the ActiveMQ web console at `http://localhost:8161`.

This verifies the main cross-service flow:

```text
gym-crm -> ActiveMQ (trainer-workload.events) -> trainer-workload-service -> PostgreSQL
```

## Troubleshooting

**PostgreSQL connection refused**

Check that the database container is running:

```bash
docker compose ps
```

**Training added in gym-crm but workload never updates**

* Confirm the ActiveMQ container from step 1 is running and both services point at the same broker URL
  (`ACTIVEMQ_BROKER_URL`, default `tcp://localhost:61616`).
* Check `trainer-workload-service` logs for a rejected/dead-lettered event - malformed or incomplete messages are routed
  to `trainer-workload.events.dlq` instead of being applied. Inspect it via the ActiveMQ web console.
* Check gym-crm logs for "Failed to publish workload event" - publish failures are logged and swallowed rather than
  failing the training operation.

## Teardown

Stop the two `bootRun` processes with `Ctrl+C`.

Then stop and remove the containers:

```bash
# In gym-crm
docker compose down -v

# In trainer-workload-service
docker compose down -v
```
