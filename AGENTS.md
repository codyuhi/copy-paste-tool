# Copy-Paste Tool — AI Agent Context & SOP

## Project Overview
- **Application Repository**: `copy-paste-tool` (`/Users/codyuhi/dev/repos/copy-paste-tool`)
- **Deployment Repository**: `app-deployments` (`/Users/codyuhi/dev/repos/app-deployments/minipc/copy-paste-tool`)
- **Canonical Ingress Domain**: `https://copy-paste-tool.lan.codyuhi.online`
- **Container Registry**: Harbor (`harbor.minipc.local/apps/copy-paste-tool:<tag>`)
- **Origin Ticket**: COPY-2 (Build native Android client for copy-paste-tool using create-android-app skill)

## Architecture
- **Backend (`/backend`)**: Go, Chi router, pure-Go SQLite (`modernc.org/sqlite`, single connection WAL). DB at `/app/data/copypaste.db`.
  - `internal/auth`: bcrypt password hashing, JWT Bearer token generation and context extraction.
  - `internal/database`: SQLite initialization and schema migration for `users` and `user_data`.
  - `internal/handlers`: REST API endpoints for `/api/auth/register`, `/api/auth/login`, `/api/auth/me`, `/api/auth/logout`, `/api/data` (GET/PUT), `/api/lihim/data` (GET/PUT), and `/healthz`.
  - `internal/lihim`: AES-256-GCM sealing for the lihim snippet set (`lihim_data` table, user id bound as AAD). Key from `LIHIM_KEY`; admins from `ADMIN_USERNAMES` (comma separated). Both come from Vault `secret/copy-paste-tool/lihim`. Without a key, or for non-admins, every `/api/lihim/*` request is a 404.
- **Web Frontend (`/app`)**: React SPA + Vite, Cedar design tokens in `index.css`, offline `localStorage` helper (`storage.js`) and REST sync client (`api.js`). Served by Go backend runner.
- **Android Client (`/android`)**: Native Kotlin + Jetpack Compose + Material 3 themed with Cedar (`cedar-design-system`), Room DB offline cache (`copypaste.db`), WorkManager outbox sync, OkHttp with Sablier cold-start wake handling, 1-tap copy to system clipboard with tactile haptic feedback, Quick Settings tile (`CopyPasteTileService`), Android Share Target (`ACTION_SEND`), and Galaxy Z Fold content-first adaptive layout (≥85% width to main content).

## Golden Rules
1. **Zero CGO**: only `modernc.org/sqlite`; builder image `golang:alpine`.
2. **Pure GitOps**: manifests live in `app-deployments/minipc/copy-paste-tool`; never use imperative `kubectl` mutations.
3. **Cedar Design Language**: Colors, typography, and radii come strictly from Cedar tokens. Custom in-app Light/Dark theme toggle persisted in DataStore. No Material You / dynamic color. No sparkle/AI icons.
4. **Foldable Content-First (Galaxy Z Fold 8 Ultra)**: Slim 72dp icon rail on wide screens (≥600dp), allocating ≥85% width to main snippet cards and fluid grids. Single pane with bottom navigation on compact screens (<600dp).
5. **Offline-First Resilience**: Room DB is the local source of truth. The app functions with zero network access and flushes outbox mutations to the homelab backend when connectivity is restored.
6. **One app, one icon**: All variants share `online.codyuhi.copypastetool` application ID. Always install using `install_apk.py`.

## Lihim
- Admins open it by tapping the header title 5 times within 2.5s (web and Android); tap 5 times again or use Exit to leave.
- The lihim view shows only lihim sections/favorites. Decrypted data lives in memory only: never `localStorage` (web) or Room (Android). Android drops it in `onStop`; the web drops it on refresh, exit, or sign out.

## Android Client Notes
- Build & Lint: `cd android && ./gradlew lintDebug testDebugUnitTest assembleDebug`
- Release Build: `cd android && ./gradlew assembleRelease -PappVersionName=1.0.0`
- Install: `python3 ~/dev/repos/skills/skills/create-android-app/scripts/install_apk.py copy-paste-tool 1.0.0` or `--apk android/app/build/outputs/apk/debug/app-debug.apk`
