# Local Environment Setup

This guide explains how to run the complete Gym CRM system locally.

The system consists of:

* **discovery-server** — Eureka service registry on port `8761`.
* **gym-crm** — main API on port `8080`, with PostgreSQL on `5432`.
* **trainer-workload-service** — trainer workload service on port `8082`, with PostgreSQL on `5433`.

`gym-crm` communicates with `trainer-workload-service` through Eureka using a service-to-service JWT.

## Prerequisites

* JDK 25
* Docker
* All three repositories cloned locally

The repositories can be located anywhere. The commands below assume they are sibling directories.

## 1. Start discovery-server

```bash
cd ../discovery-server
./gradlew bootRun
```

Eureka dashboard: `http://localhost:8761`

## 2. Start trainer-workload-service

```bash
cd ../trainer-workload-service
docker compose up -d
./gradlew bootRun
```

No environment variables are required for local development.

## 3. Start gym-crm

```bash
cd ../gym-crm
docker compose up -d
./gradlew bootRun
```

No environment variables are required for local development.

> **Important:** `SERVICE_JWT_SECRET` must be identical in `gym-crm` and `trainer-workload-service`. The local profiles
> already use the same default value.

Wait a few seconds for both services to appear as `UP` in the Eureka dashboard.

## Smoke Test

Use Swagger UI to verify the main end-to-end flow:

`http://localhost:8080/swagger-ui/index.html`

1. **Register a trainer** using `POST /api/v1/trainers`.
2. **Register a trainee** using `POST /api/v1/trainees`.
3. **Log in as a trainer** using `POST /api/v1/auth/login` and copy the returned JWT.
4. Click **Authorize** in Swagger UI and enter the JWT.
5. **Add a training** using `POST /api/v1/trainings`.
6. Check the `trainer-workload-service` logs and verify that the training duration was added to the trainer's monthly
   workload.

This verifies the main cross-service flow:

```text
gym-crm → Eureka → trainer-workload-service → PostgreSQL
```

## Troubleshooting

**PostgreSQL connection refused**

Check that the database container is running:

```bash
docker compose ps
```

**Service does not appear in Eureka**

Wait around 30 seconds for registration, then check the service logs.

**`401` when gym-crm calls trainer-workload-service**

Check that `SERVICE_JWT_SECRET` is identical in both services.

**Spring Cloud compatibility error**

If required, disable the compatibility verifier in `gym-crm`:

```yaml
spring:
  cloud:
    compatibility-verifier:
      enabled: false
```

## Teardown

Stop the three `bootRun` processes with `Ctrl+C`.

Then stop and remove the PostgreSQL containers:

```bash
# In gym-crm
docker compose down -v

# In trainer-workload-service
docker compose down -v
```
