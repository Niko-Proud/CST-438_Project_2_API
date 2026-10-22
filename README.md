# CST 438 Project 02: API Template

Starter repo for Project 02. **One person per team** clicks **Use this template → Create a new
repository**, names it after your API, and adds teammates as collaborators.

> Replace this README with your own in Sprint 4. The prompt lists what it must contain.

## What's in here

| Path | What it is | When |
|---|---|---|
| `docs/proposal.md` | Proposal template: pitch, resources, ER sketch, endpoints, choices, risks | **Thu 10/1** |
| `docs/openapi.yaml` | A complete example contract (workout tracker). Replace it with your API. | Sprint 1 |
| `redocly.yaml` | Lint rules for the contract | Sprint 1 |
| `.github/ISSUE_TEMPLATE/user_story.md` | Every new issue starts as a user story with acceptance criteria | now |
| `.github/pull_request_template.md` | Every PR starts with `Closes #` and a test checklist | now |
| `.gitignore` | Gradle, Android, IntelliJ, Node, and **secrets** (`.env`, keystores, `local.properties`) | now |

Where your code goes is up to you: an `api/` and an `android/` folder in this repo (monorepo),
or a second repo for the Android app. Write it down in an ADR.

## First 15 minutes

- [ ] Add teammates: **Settings → Collaborators**
- [ ] Protect `main`: **Settings → Rules → Rulesets → New branch ruleset**. Target the default branch,
      require a PR with **2 approvals** (1 for a team of 3), block force pushes. Enforcement: **Active**.
- [ ] Create milestones `Sprint 1` through `Sprint 4`: **Issues → Milestones**
- [ ] Create a Project board (Kanban) and link it: **Projects tab → Link a project**
- [ ] Start `docs/proposal.md` on a branch:

```bash
git switch -c docs/proposal
# edit docs/proposal.md
git add docs/proposal.md
git commit -m "Draft proposal"
git push -u origin docs/proposal
gh pr create --fill
```

## Working with the contract

No installs needed beyond Node (`npx` runs the tools).

```bash
# Lint: fix every error before you open a PR
npx @redocly/cli lint docs/openapi.yaml

# Mock server: a fake API built from the contract, at http://127.0.0.1:4010
npx @stoplight/prism-cli mock docs/openapi.yaml

curl -H "Authorization: Bearer x" "http://127.0.0.1:4010/api/v1/workouts?page=0&size=20"
curl -i http://127.0.0.1:4010/api/v1/workouts          # 401: no token
curl -H "Authorization: Bearer x" -H "Prefer: code=404" \
     http://127.0.0.1:4010/api/v1/workouts/42          # force an error response
```

The mock **validates** your requests and returns the `example:` values from the contract.
**Nothing is saved**: a POST followed by a GET returns the example, not what you posted. The
Android emulator reaches it at `http://10.0.2.2:4010`.

To see the contract rendered, paste `docs/openapi.yaml` into https://editor.swagger.io.

## Links

- Project 02 prompt and rubric: Canvas → Project 02 module
- OpenAPI 3.0 spec: https://spec.openapis.org/oas/v3.0.3
- Prism: https://docs.stoplight.io/docs/prism
- Mermaid ER diagrams: https://mermaid.js.org/syntax/entityRelationshipDiagram.html
- RFC 9457 Problem Details: https://www.rfc-editor.org/rfc/rfc9457

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

## API usage

### Base URLs

- Local Docker image: `http://localhost:8080`
- Production Render API: `https://brain-worm-api.onrender.com`

### Health check

```text
GET https://brain-worm-api.onrender.com/actuator/health
```

Expected response:

```json
{"status":"UP"}
```

### Authenticated PowerShell setup

```powershell
$base = "https://brain-worm-api.onrender.com"
$token = Read-Host "Paste Google bearer token"
$headers = @{ Authorization = "Bearer $token" }
```

### Current user

```text
GET /api/v1/users/me
DELETE /api/v1/users/me
```

```powershell
Invoke-RestMethod "$base/api/v1/users/me" -Headers $headers
```

The delete route removes the authenticated account and its owned registry items.

### Registry routes

```text
GET    /api/v1/registry?page=0&size=20
POST   /api/v1/registry
GET    /api/v1/registry/{itemId}
PUT    /api/v1/registry/{itemId}
DELETE /api/v1/registry/{itemId}
```

Example create request:

```powershell
$body = @{ product = "Organic Spinach"; batch = "LOT-123" } | ConvertTo-Json
Invoke-RestMethod "$base/api/v1/registry" `
  -Method Post -Headers $headers `
  -ContentType "application/json" -Body $body
```

### Recall routes

```text
GET /api/v1/recalls?page=0&size=20
GET /api/v1/recalls?product=spinach
GET /api/v1/recalls/{recallId}
```

### Admin-only routes

```text
GET    /api/v1/users?page=0&size=20
GET    /api/v1/users/{userId}
PATCH  /api/v1/users/{userId}
DELETE /api/v1/users/{userId}
```

### Notes

- `/actuator/health` is public.
- `/api/v1/**` requires a Google bearer token.
- User administration routes also require database admin privileges.
- Never commit bearer tokens, passwords, or `.env` contents.
- Replace values in braces, such as `{itemId}`, with real IDs.
