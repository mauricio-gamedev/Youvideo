# Youvideo roadmap

## Product principles
1. A single, coherent library for content from compatible providers.
2. Clear separation between provider playback and the app's own media player.
3. Downloads only where the source grants permission.
4. Low and predictable memory use; the Galaxy A06 is the reference test device.
5. Honest UI states: no fake providers, playback, or download progress.

## SDK baseline
- Stable Android API 36 (Android 16) is the MVP compile/target baseline.
- Compose BOM 2026.04.01 is used to stay on the stable Compose 1.11 line, avoiding the Compose 1.12/API 37 preview dependency.

## MVP sequence

### 0.1 — Android foundation
- [x] Initialize Kotlin + Compose application.
- [x] Establish AMOLED-first design tokens and bottom navigation.
- [x] Add CI for debug compilation and Android lint.
- [x] Verify the first GitHub Actions build: unit tests, lint, debug APK assembly, and artifact upload passed.
- [ ] Install the debug APK and verify it on a physical device.

### 0.2 — Local media
- [x] Add a system document picker for user-selected media using the Storage Access Framework.
- [x] Build a Media3-based local player with built-in playback controls.
- [x] Pause playback when the app moves to the background and release the player when the screen closes.
- [ ] Add tests around media-source and playback-state handling.

### 0.3 — Library and persistence
- [ ] Add Room entities/DAO for history and favorites.
- [ ] Add DataStore for app preferences.
- [ ] Add bounded thumbnail caching and paging where appropriate.

### 0.4 — Authorized sources
- [ ] Define a provider interface and capability model.
- [ ] Integrate sources only through documented and permitted methods.
- [ ] Evaluate official YouTube integration constraints before implementation.
- [ ] Show provider-specific limitations clearly.

### 0.5 — Authorized downloads
- [ ] Define a download-source contract that requires download permission.
- [ ] Add resumable downloads for compatible sources.
- [ ] Add queue, progress, retry, and storage management.
- [ ] Test cancellation and process recreation.

### 0.6 — Device optimization and release
- [ ] Profile startup, scrolling, playback, and memory use on the Galaxy A06.
- [ ] Fix regressions before adding optional features.
- [ ] Add release signing through GitHub Actions secrets.
- [ ] Document privacy, data use, permissions, licenses, and release steps.

## MVP non-goals
- Blocking or removing advertisements from YouTube.
- Bypassing official players or extracting protected/unauthorized stream URLs.
- Downloading videos from providers that do not grant download permission.
- Always-on background services or aggressive RAM cleanup.
