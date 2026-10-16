# Native Capability Evaluation: Copy-Paste Tool

- **Date**: 2026-10-15
- **Web app**: https://copy-paste-tool.lan.codyuhi.online (backend-backed with Go Chi + SQLite, Sablier scale-to-zero enabled)
- **Primary users / devices**: Cody (Galaxy Z Fold 8 Ultra), Lena (Galaxy S26+)

## How the app is used today
`copy-paste-tool` is an efficient snippet dashboard designed for organizing, searching, and copying repetitive text responses, canned replies, links, code snippets, and customer support messages. On mobile, Cody and Lena need rapid, 1-tap access to paste values into messaging apps, email, browsers, and terminal sessions, while keeping their personal library organized into categorized sections and starred favorites.

## Web limitations that hurt
- **Mobile clipboard friction**: On mobile browsers, copying text requires navigating to the web app, waiting for cold start if Sablier is sleeping, tapping the button, and hoping the browser's asynchronous clipboard API doesn't fail due to transient user gesture timeouts.
- **No system-wide integration**: Inability to quickly grab a snippet from anywhere in Android (e.g. via a Quick Settings tile) without leaving the active conversation or app.
- **No Android share receiver**: Cannot easily save a snippet or link directly into a section from the Android system Sharesheet (`ACTION_SEND`).
- **No haptic feedback**: Web button taps lack tactile confirmation that the text snippet was successfully written to the clipboard.
- **Offline vulnerability**: If away from home Wi-Fi and Tailscale is temporarily disconnected, or during backend cold-starts, the web client cannot reliably fetch or sync snippet modifications without local database persistence.

## Capability scores
| Capability | Value (0–3) | Cost (S/M/L) | Verdict | Notes |
| :--- | :---: | :---: | :--- | :--- |
| Tap-to-Copy with Haptic Feedback | 3 | S | `v1` | Core function: 1-tap copy to system `ClipboardManager` with tactile vibration confirmation and toast feedback. |
| Offline Room DB & Outbox Sync | 3 | M | `v1` | Offline-first Room database storing sections, buttons, and favorites, with WorkManager background outbox sync to Go backend. |
| Sablier Cold-Start Resilience | 3 | S | `v1` | Automatic retry with exponential backoff on HTML responses from backend, with calm Cedar status banner while keeping cached snippets accessible. |
| Android Share Target (`ACTION_SEND`) | 3 | S | `v1` | Receive text shared from any Android app directly into a "Save Snippet" bottom sheet with section picker. |
| Quick Settings Tile (`TileService`) | 2 | S | `v1` | `CopyPasteTileService` enabling instant 1-tap launch into search/favorites overlay from the Android notification shade. |
| App Launcher Shortcuts | 2 | S | `v1` | Static launcher shortcuts on app icon long-press: "Favorites", "New Snippet", "Search". |
| Cedar Design System & Theme Toggle | 3 | S | `v1` | Full Cedar typography (Source Serif 4, Inter, JetBrains Mono) and Spruce/Ecru palette with in-app light/dark toggle persisted in DataStore. |
| Foldable Content-First Adaptive Layout | 3 | S | `v1` | Slim 72dp icon rail on Fold unfolded (752dp) dedicating ≥85% width to expansive 2-column card grid; bottom nav on cover/S26+. |
| Home Screen Glance Widget | 2 | M | `later` | Glance Compose home screen widget for 1-tap snippet copying directly from the home screen. |
| BiometricPrompt App Lock | 1 | S | `later` | Optional fingerprint/face biometric lock for sensitive sections (passwords, auth tokens). |
| Camera / OCR Text Scanning | 1 | M | `later` | Scan text from physical paper, labels, or whiteboards into a new snippet via CameraX and ML Kit. |
| Background Clipboard Monitoring Service | 0 | L | `no` | Modern Android restricts background clipboard access; accessibility workarounds drain battery and create security risks. |
| Audio Recording / Voice Notes | 0 | M | `no` | Text snippet repository does not require audio recording or playback. |

## Recommended v1 (beyond parity)
1. **Tap-to-Copy with Haptic Feedback** — Primary interaction: tapping any snippet button instantly copies its `pasteValue` into Android's system `ClipboardManager`, accompanied by a crisp tactile tick (`HapticFeedbackConstants.CONFIRM`) and a Cedar toast notification.
2. **Offline Room Cache & Outbox Sync** — Complete offline resilience: all sections, buttons, and favorites are stored in a local Room database. Modifications made offline are queued in an outbox table and flushed to the Go backend via WorkManager when network connectivity is established.
3. **Sablier Cold-Start Wake Handling** — When the scale-to-zero backend is waking, OkHttp detects the HTML loading page, exhibits exponential backoff retries, and presents a calm Cedar waking banner while keeping local snippets fully usable.
4. **Android Share Target (`ACTION_SEND`)** — Register an `ACTION_SEND` intent filter for `text/plain` so users can share text or URLs directly from any Android app (browser, email, chat) into a "New Snippet" sheet, choosing an existing section or creating a new one.
5. **Quick Settings Tile (`CopyPasteTileService`)** — Quick Settings tile in One UI pull-down shade providing immediate access to snippets without losing place in current foreground applications.
6. **App Launcher Shortcuts** — App icon long-press shortcuts: "Favorites", "New Snippet", and "Search" for rapid 1-tap deep linking.
7. **Cedar Design System & Theme Toggle** — Full Cedar styling with light/dark toggle stored in DataStore, available on every screen, with bundled OFL fonts (Source Serif 4, Inter, JetBrains Mono).
8. **Foldable Content-First Adaptive Layout** — Tuned for Galaxy Z Fold 8 Ultra: Compact (360dp cover, S26+) uses bottom navigation bar; Medium (752dp unfolded) uses a slim 72dp icon rail dedicating ~680dp (≥85%) to an expansive fluid multi-column snippet grid.

## Deferred (file tickets)
- **Home Screen Glance Widget**: Glance Compose widget displaying favorite snippets with direct 1-tap copy action on the Android home screen.
- **BiometricPrompt App Lock**: Optional biometric lock for sensitive snippets or password categories.
- **Camera OCR Text Extraction**: CameraX scanning to convert document photos into text snippets.

## Rejected
- **Background Clipboard Monitoring Service**: Background clipboard listeners on Android 10+ require invasive Accessibility or IME services, which degrade battery life and trigger security warnings.
- **Audio / Voice Notes**: Out of scope for a structured text copy-paste clipboard manager.

## Permissions requested in v1
- `android.permission.INTERNET` (Standard backend REST API communication)
- `android.permission.ACCESS_NETWORK_STATE` (Network connectivity status & offline detection)
- `android.permission.ACCESS_LOCAL_NETWORK` (Required on Android 17+ for connecting to homelab `*.lan.codyuhi.online` on home Wi-Fi)
- `android.permission.VIBRATE` (Tactile haptic feedback ticks on snippet copy and action events)

## Open Design Decisions & Recommendations
1. **Unfolded Foldable Layout (Galaxy Z Fold 8 Ultra: 752dp wide)**:
   - *Option A*: Persistent 300dp sidebar with 452dp snippet column (wastes screen real estate).
   - *Option B (Recommended)*: Strict Foldable Content-First (≥85% width to main focus). Slim 72dp icon rail on the left, dedicating ~680dp to a fluid 2-column grid of section cards and snippet buttons, with filter chips and search at the top, and edit forms opening in an anchored slide-over panel.
2. **Local Snippets vs. Cloud Account Synchronization**:
   - *Recommendation*: Full offline-first design. Works immediately out of the box with default or local snippets stored in Room DB without requiring login. If user logs in with their homelab account, Room DB seamlessly merges or syncs with the Go backend via JWT token stored in Encrypted DataStore (AES-GCM Android Keystore).
3. **Quick Settings Tile Action**:
   - *Recommendation*: Tapping the Quick Settings tile unlocks/launches `MainActivity` directly into the "Favorites" tab or an overlay Quick Search sheet so the user can copy any snippet in 1 tap and return to their current app.
