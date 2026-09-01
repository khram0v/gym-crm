# Gym CRM

A Spring Boot REST API for managing gym trainees, trainers, and training sessions.

## Tech Stack

- Java / Spring Boot
- Spring Data JPA
- Spring Security (JWT)
- MapStruct
- OpenAPI (Swagger)
- JUnit 5 / Mockito
- Spring JMS + ActiveMQ

## Getting Started

```bash
./gradlew bootRun
```

The API runs at `http://localhost:8080`.

## Running the Full System Locally

This service publishes trainer workload events to `trainer-workload-service` over ActiveMQ. For a complete local smoke
test covering the message queue and end-to-end cross-service tracing, see [LOCAL_SETUP.md](LOCAL_SETUP.md).

## API Docs

Swagger UI: `http://localhost:8080/swagger-ui.html`

Use the **Authorize** button to provide a Bearer JWT token (obtained via `POST /api/v1/auth/login`).

## Authentication

All endpoints require a **Bearer JWT** token, except:

- `POST /api/v1/trainees` - register trainee
- `POST /api/v1/trainers` - register trainer
- `POST /api/v1/auth/login` - login
- `POST /api/v1/auth/refresh` - exchange a refresh token for a new token pair

Credentials are generated automatically on registration and returned in the response. Use them to log in and obtain a
token; passwords are stored hashed (BCrypt), never in plaintext.

Access tokens expire after 1 hour. Login also returns a refresh token (valid for 7 days), which can be exchanged for a
new access/refresh token pair via `POST /api/v1/auth/refresh` without re-entering credentials. Each refresh token can
only be used once - using it immediately invalidates it and issues a new one (rotation), so a stolen-but-already-used
refresh token can't be replayed.

`POST /api/v1/auth/logout` invalidates the current access token immediately. To also invalidate the refresh token on
logout, send it in an X-Refresh-Token header alongside the usual Authorization: Bearer <token> header.

After 3 failed login attempts for a username, further attempts are blocked for 5 minutes.

## Authorization

Trainees and trainers can only access and modify their own data - a trainee can't view or edit another trainee's
profile, trainings, or trainer list, and likewise for trainers. Attempting to access another user's resource, or
accessing a resource with the wrong role (e.g. a trainer requesting a trainee endpoint), returns
`403 Forbidden`. Registration endpoints and reference data (training types) have no ownership restriction.

A single configuration-defined `admin` account (see security.admin.* properties) bypasses these ownership checks and can
access every trainee/trainer endpoint regardless of who owns the resource.

Trainings can only be canceled while their date is still in the future; a training that has already taken place is
immutable and cannot be deleted.

## Trainer Workload Integration

Whenever a training is added or canceled, gym-crm publishes a workload event to the `trainer-workload.events` queue on
ActiveMQ (`TrainerWorkloadClientImpl`, a JMS `TextMessage` carrying a JSON body). `trainer-workload-service`
consumes that queue asynchronously and updates the trainer's monthly training total.

This is a best-effort side effect: publish failures (broker unavailable, serialization error) are logged and swallowed -
the training add/cancel operation itself always succeeds regardless of whether the workload event could be published.

Because the two services now communicate purely through the broker, gym-crm no longer needs to discover
`trainer-workload-service`'s network location, load-balance calls to it, circuit-break on it, or mint a
service-to-service JWT for it - all of that REST-era machinery has been removed. `discovery-server` (Eureka) is not
required for this integration anymore.

Requires an ActiveMQ broker (see [LOCAL_SETUP.md](LOCAL_SETUP.md)) and `trainer-workload-service` running to actually
process events; if either is unavailable, events simply queue up on the broker (or fail to publish, per the best-effort
behavior above) without affecting gym-crm's own API responses.

## Observability

Requests are traced using a `transactionId` across services:

* **Request logging:** `RequestLoggingInterceptor` logs request start/completion.
* **Operation logging:** service methods log business operations (`DEBUG` for reads, `INFO` for mutations).
* **Transaction ID:** `TransactionIdFilter` generates or reuses `X-Transaction-Id`, stores it in MDC, and returns it in
  the response.
* **Cross-service tracing:** gym-crm propagates the current transaction ID as a JMS message property (`transactionId`)
  on every workload event it publishes, so `trainer-workload-service` can correlate its own logs for that event back to
  the originating request.

## Main Endpoints

| Method | Path                                    | Description                                   |
|--------|-----------------------------------------|-----------------------------------------------|
| POST   | `/api/v1/auth/login`                    | Login, obtain JWT token                       |
| POST   | `/api/v1/auth/refresh`                  | Exchange a refresh token for a new token pair |
| POST   | `/api/v1/auth/logout`                   | Logout, invalidate current token(s)           |
| POST   | `/api/v1/trainees`                      | Register trainee                              |
| POST   | `/api/v1/trainers`                      | Register trainer                              |
| GET    | `/api/v1/trainees/{username}`           | Get trainee profile                           |
| GET    | `/api/v1/trainers/{username}`           | Get trainer profile                           |
| PUT    | `/api/v1/trainees/{username}`           | Update trainee                                |
| PUT    | `/api/v1/trainers/{username}`           | Update trainer                                |
| DELETE | `/api/v1/trainees/{username}`           | Delete trainee                                |
| GET    | `/api/v1/trainees/{username}/trainings` | List trainee trainings                        |
| GET    | `/api/v1/trainers/{username}/trainings` | List trainer trainings                        |
| POST   | `/api/v1/trainings`                     | Add training                                  |
| DELETE | `/api/v1/trainings/{id}`                | Cancel (delete) a training                    |
| GET    | `/api/v1/trainings/training-types`      | List training types                           |

## Testing

```bash
./gradlew test
```
