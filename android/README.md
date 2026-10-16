# Copy-Paste Tool — Native Android Client

Native Android client for `copy-paste-tool`, providing a high-performance, offline-first snippet manager tailored for Cody's **Samsung Galaxy Z Fold 8 Ultra** (cover and inner screens) and **Galaxy S26+**, styled with the homelab **Cedar design language**.

---

## 🏛️ Architecture & Features

- **Language & Runtime**: Kotlin (K2 compiler) + Jetpack Compose with Material 3.
- **Architecture**: Single-activity MVVM + UDF pattern, manual constructor dependency injection via `AppContainer`.
- **Design System**: Strict Cedar design system mapping (`cedar-design-system`), custom in-app Light/Dark theme toggle persisted in Preferences DataStore (no Material You / dynamic color).
- **Foldable Content-First (Galaxy Z Fold 8 Ultra)**:
  - **Cover Screen (Compact 360×840 dp)**: Single-column snippet list with responsive bottom navigation bar.
  - **Inner Screen (Medium 752×834 dp)**: Slim 72dp icon navigation rail dedicating ≥85% (~680dp) of screen width to an expansive 2-column fluid snippet grid with filter chips and slide-over dialogs.
- **Data & Offline Resilience**:
  - Offline-first **Room database** (`copypaste.db`) caching sections, buttons, and favorites.
  - Outbox synchronization pattern with **WorkManager** background sync.
  - **Sablier Cold-Start Resilience**: OkHttp interceptor catches HTML waiting responses from `https://copy-paste-tool.lan.codyuhi.online/api/...`, performs exponential backoff retries, and shows a calm Cedar waking banner while keeping local snippets fully accessible.
- **Native Integrations**:
  - **Tap-to-Copy with Haptic Feedback**: 1-tap copy to system `ClipboardManager` with tactile vibration tick (`HapticFeedbackConstants.CONFIRM`).
  - **Quick Settings Tile (`CopyPasteTileService`)**: Instant 1-tap access to favorites directly from the notification shade.
  - **Android Share Target (`ACTION_SEND`)**: Save shared text from any Android app directly as a new snippet.
  - **App Launcher Shortcuts**: Static shortcuts for "Favorites", "New Snippet", and "Search".

---

## 🛠️ Local Build & Verification

### Prerequisites
- JDK 21
- Android SDK (API 33–37)

### Run Unit Tests & Lint
```bash
cd android
./gradlew testDebugUnitTest
./gradlew lint
```

### Build Debug APK
```bash
./gradlew assembleDebug
```

---

## 🚀 USB Sideloading onto Phones

Use the zero-dependency Python install script:
```bash
SCRIPT=~/dev/repos/skills/skills/create-android-app/scripts/install_apk.py

# Check connected phones
python3 $SCRIPT --device-info

# Install local debug build
python3 $SCRIPT --apk app/build/outputs/apk/debug/app-debug.apk

# Install signed release build from Harbor OCI
python3 $SCRIPT copy-paste-tool 1.0.0
```

---

## 🏗️ Argo Workflows CI/CD Pipeline

Production builds and release signing are automated via Argo Workflows:
```bash
argo submit --from workflowtemplate/android-build-apk -n argo-workflows \
  -p repo-name=copy-paste-tool -p version=1.0.0 --watch
```
The resulting signed release APK is published to Harbor at:
`harbor.minipc.local/apps/copy-paste-tool-android:1.0.0`
