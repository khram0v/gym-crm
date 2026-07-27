# Gym CRM

A Spring Boot REST API for managing gym trainees, trainers, and training sessions.

## Tech Stack

- Java / Spring Boot
- Spring Data JPA
- MapStruct
- OpenAPI (Swagger)
- JUnit 5 / Mockito

## Getting Started

```bash
./gradlew bootRun
```

The API runs at `http://localhost:8080`.

## API Docs

Swagger UI: `http://localhost:8080/swagger-ui.html`

Use the **Authorize** button to provide Basic auth credentials.

## Authentication

All endpoints use **HTTP Basic auth**, except:

- `POST /api/v1/trainees` — register trainee
- `POST /api/v1/trainers` — register trainer
- `PUT /api/v1/trainees/{username}/password` — change password
- `PUT /api/v1/trainers/{username}/password` — change password

Credentials are generated automatically on registration and returned in the response.

## Main Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/trainees` | Register trainee |
| POST | `/api/v1/trainers` | Register trainer |
| GET | `/api/v1/trainees/{username}` | Get trainee profile |
| GET | `/api/v1/trainers/{username}` | Get trainer profile |
| PUT | `/api/v1/trainees/{username}` | Update trainee |
| PUT | `/api/v1/trainers/{username}` | Update trainer |
| DELETE | `/api/v1/trainees/{username}` | Delete trainee |
| GET | `/api/v1/trainees/{username}/trainings` | List trainee trainings |
| GET | `/api/v1/trainers/{username}/trainings` | List trainer trainings |
| POST | `/api/v1/trainings` | Add training |
| GET | `/api/v1/training-types` | List training types |

## Testing

```bash
./gradlew test
```
