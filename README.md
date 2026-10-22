# Brainworm API

Brainworm is a small app for keeping track of the food you buy. You add your foods, and the app lets you know if any of them have been recalled by the FDA.

This repo is the API (Spring Boot). The Android app is in our app repo:
https://github.com/Niko-Proud/CST-438_Project_2_App

## Team

- Victoria Ha
- Alexander Trujillo
- Nikolii Proud
- Hyun Jeong Lim

## Links

- Live API: https://brain-worm-api.onrender.com
- Health check: https://brain-worm-api.onrender.com/actuator/health
- API contract: `docs/openapi.yaml` (paste it into https://editor.swagger.io to see it)
- ER diagram: `docs/erDiagram.md`

The server can take a little while to wake up the first time.

## Tech stack

- Java 21 and Spring Boot
- PostgreSQL with Flyway migrations
- Google sign-in (the API checks Google ID tokens)
- Docker and Render
- JUnit and PMD, run by GitHub Actions

## Endpoints

All endpoints start with `/api/v1` and need a Google ID token:
`Authorization: Bearer <token>`

| Method | Path | What it does |
|---|---|---|
| GET | `/users/me` | Get my account. It is made the first time I sign in. |
| DELETE | `/users/me` | Delete my account and my foods |
| GET | `/registry` | List my foods (with paging) |
| GET | `/registry/{itemId}` | Get one of my foods |
| POST | `/registry` | Add a food |
| PUT | `/registry/{itemId}` | Edit a food |
| DELETE | `/registry/{itemId}` | Delete a food |
| GET | `/recalls` | List FDA recalls. You can filter by product, contaminant, batch, or recallDate. |
| GET | `/recalls/{recallId}` | Get one recall |
| GET | `/users` | Admin only: list users |
| GET | `/users/{userId}` | Admin only: get one user |
| PATCH | `/users/{userId}` | Admin only: change a user's admin role |
| DELETE | `/users/{userId}` | Admin only: delete a user |

## How to run

1. Clone the repo.
   ```bash
   git clone https://github.com/Niko-Proud/CST-438_Project_2_API.git
   ```
2. Make a `.env` file in the root folder with the Google client ID (ask the team). Don't commit this file.
   ```properties
   GOOGLE_CLIENT_ID=your-client-id
   ```
3. Start the API and the database with Docker.
   ```bash
   docker compose up --build
   ```
4. The API is now at http://localhost:8080. The Android emulator can reach it at `http://10.0.2.2:8080/`.

## Local CI and Docker testing

Run these checks from the repository root before pushing workflow changes.

### Gradle tests on Windows

```powershell
.\gradlew.bat test --no-daemon
```

Run PMD separately:

```powershell
.\gradlew.bat pmdMain --no-daemon
```

### Build the Docker image without publishing it

```powershell
docker build --no-cache -t brain-worm:local .
```

This creates a local image only. It does not push anything to GHCR.

### Start the local API and PostgreSQL database

```powershell
docker compose up --build
```

Stop the stack:

```powershell
docker compose down
```

Reset the disposable local database completely:

```powershell
docker compose down -v
```

### Run the GitHub Actions checks locally

After installing `act`, run the checks job with:

```powershell
act pull_request -W .github/workflows/ciactions.yaml -j checks
```

This simulates the pull-request checks locally and should run tests and PMD
without publishing an image. Local `act` results are useful but are not
identical to GitHub-hosted runners.

Testcontainers also requires Docker to be running. A Dockerfile build and a
Compose run do not replace a Testcontainers integration test.
