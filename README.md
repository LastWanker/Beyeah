# Beyeah Revival Workspace

This repository is prepared for plan A revival.

## Structure
- backend: Spring Boot backend (migrated from Beyeah1211)
- frontend: Vue3 + Vite frontend (migrated from front0106)
- database: SQL and DB assets (migrated from BY_DB)
- references: historical references for troubleshooting
- docs: setup and revival guides

## Recommended Environment
- JDK: 17
- MySQL: 8.x
- Node.js: 18 LTS
- npm: 9+
- IntelliJ IDEA: 2024+ (Ultimate preferred for full Node support)

## Configuration
1. Copy `.env.example` to `.env` (or export env vars in your shell).
2. Fill database and auth variables:
   - `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
   - `APP_AUTH_TOKEN_SECRET`
   - `APP_CORS_ALLOWED_ORIGINS`
   - `APP_ORDER_AUTO_CLOSE_SECONDS`, `APP_ORDER_AUTO_CLOSE_BATCH_SIZE`, `APP_ORDER_AUTO_CLOSE_FIXED_DELAY_MS`
   - Optional frontend override: `VITE_API_BASE_URL` (for example `http://localhost:23333`)
3. Backend reads env vars from `backend/src/main/resources/application.properties`.

### Local connectivity notes
- If frontend runs on `localhost`/`127.0.0.1`, backend CORS now auto-allows loopback aliasing between them.
- If backend is not on port `23333`, set `VITE_API_BASE_URL` to the real API base URL.

## Quick Start
1. Read `docs/00-IDEA-Setup.md`
2. Read `docs/02-Revival-Checklist.md`
3. Start backend first, then frontend

## Unified Test Scripts
- Script directory: `scripts/tests`
- Run all checks in one command (PowerShell, from repo root):
  - `.\scripts\tests\run-all.ps1`
- Run single checks:
  - Backend integration tests: `.\scripts\tests\run-backend-tests.ps1`
  - Frontend E2E tests: `.\scripts\tests\run-frontend-e2e.ps1`
  - Frontend build check: `.\scripts\tests\run-frontend-build.ps1`

## Test Maintenance Rule
- When adding a new feature, add or update related automated tests first.
- If the feature introduces a new test scope, append the new test step into `scripts/tests/run-all.ps1`.
- Keep CI and local scripts aligned so local one-command checks match pipeline behavior.

## Notes
- API v1 now uses bearer token authentication for protected routes:
  - `/api/v1/cart/**`
  - `/api/v1/orders/**`
  - `/api/v1/users/**`
- Batch B address book APIs are available:
  - `GET /api/v1/addresses`
  - `POST /api/v1/addresses`
  - `PUT /api/v1/addresses/{id}`
  - `DELETE /api/v1/addresses/{id}`
  - `PUT /api/v1/addresses/{id}/default`
- `references/Beyeah1206` and `references/Beyeah1205-resources` are kept for fast backport/fix.
