# Gym CRM

A Spring Boot REST API for managing gym trainees, trainers, and training sessions.

## Tech Stack

- Java / Spring Boot
- Spring Data JPA
- Spring Security (JWT)
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

Use the **Authorize** button to provide a Bearer JWT token (obtained via `POST /api/v1/auth/login`).

## Authentication

All endpoints require a **Bearer JWT** token, except:

- `POST /api/v1/trainees` - register trainee
- `POST /api/v1/trainers` - register trainer
- `POST /api/v1/auth/login` - login
- `POST /api/v1/auth/refresh` - exchange a refresh token for a new token pair

Credentials are generated automatically on registration and returned in the response. 
Use them to log in and obtain a token; passwords are stored hashed (BCrypt), never in plaintext.

Access tokens expire after 1 hour. Login also returns a refresh token (valid for 7 days), which can be 
exchanged for a new access/refresh token pair via POST /api/v1/auth/refresh without re-entering credentials. 
Each refresh token can only be used once - using it immediately invalidates it and issues a new one (rotation), 
so a stolen-but-already-used refresh token can't be replayed.

`POST /api/v1/auth/logout` invalidates the current access token immediately. To also invalidate the refresh
token on logout, send it in an X-Refresh-Token header alongside the usual Authorization: Bearer <token> header.

After 3 failed login attempts for a username, further attempts are blocked for 5 minutes.

## Authorization

Trainees and trainers can only access and modify their own data - a trainee can't view or edit another
trainee's profile, trainings, or trainer list, and likewise for trainers. Attempting to access another user's
resource, or accessing a resource with the wrong role (e.g. a trainer requesting a trainee endpoint), returns
`403 Forbidden`. Registration endpoints and reference data (training types) have no ownership restriction.

## Main Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/auth/login` | Login, obtain JWT token |
| POST | `/api/v1/auth/refresh` | Exchange a refresh token for a new token pair |
| POST | `/api/v1/auth/logout` | Logout, invalidate current token(s) |
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
| GET | `/api/v1/trainings/training-types` | List training types |

## Testing

```bash
./gradlew test
```
