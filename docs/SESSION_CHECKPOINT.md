# Session checkpoint — Youvideo

Last updated: 2026-11-04

## Repository
- GitHub: https://github.com/mauricio-gamedev/Youvideo
- Default branch: `main`
- Work branch: `feat/android-mvp-foundation`
- Pull Request: https://github.com/mauricio-gamedev/Youvideo/pull/1 (draft; not merged)
- All application changes remain on the feature branch.

## Product brief
- Name: Youvideo.
- Target: Android app, starting as a complete multimedia hub for compatible sources.
- Priority: balanced UX, features, and performance.
- Visual direction: AMOLED-first dark interface.
- Reference device: Samsung Galaxy A06 / entry-level hardware.

## Implemented
- Kotlin + Jetpack Compose foundation using AGP built-in Kotlin support.
- AMOLED-first theme and bottom navigation (Home, Explore, Downloads, Library).
- Android system document picker for user-selected videos through Storage Access Framework.
- Media3 ExoPlayer local playback with the built-in PlayerView controls.
- Persistable URI read permission requested when supported by the document provider; no broad storage permission.
- Player pauses on app background and releases its resources when the player screen closes.
- Android Back action closes the local player screen.
- GitHub Actions workflow for unit tests, lint, and debug APK assembly/upload.
- Contributor guidelines, product roadmap, and project documentation.

## Not implemented
- YouTube/provider integrations.
- Search against live providers.
- Persistent playback history, favorites, or playlists.
- Authorized download manager.
- Release signing.
- Physical-device validation on the Galaxy A06.

## Build and validation history
- CI initially failed because the Android setup action requested the retired SDK package `tools`; workflow was corrected.
- CI then failed because API 37 was not available as the stable SDK package; the project was moved to Android API 36.
- AGP 9's built-in Kotlin support conflicted with the legacy `org.jetbrains.kotlin.android` plugin; the obsolete plugin and legacy `kotlinOptions` were removed.
- Media3 1.11.1 is used for local playback. The player observes the Activity LifecycleOwner supplied by Activity Compose, avoiding a Lifecycle Compose dependency that required compileSdk 37.
- Fixed a PlayerView/ExoPlayer name-resolution compile error by capturing the ExoPlayer instance and explicitly assigning it to PlayerView.
- Latest completed validation for application commit `430bebc68b9b7f38c78439db87f93256a11bc3c9`: GitHub Actions PR run #44, run ID `41915746930`, completed successfully on 2026-11-04.
- Confirmed successful steps: `testDebugUnitTest`, `lintDebug`, `assembleDebug`, and upload of the debug APK artifact.
- Artifact: `youvideo-debug`, artifact ID `13365276407`, 21,133,756 bytes, SHA-256 `21a6a177abac915bccee2a3709a02d07643c389309d9118e34bc7dbbe1a15b38`; created 2026-11-04 21:27 UTC and expires 2026-11-18 21:27 UTC.
- Workflow run: https://github.com/mauricio-gamedev/Youvideo/actions/runs/41915746930
- The CI result confirms a successful debug build and uploaded artifact. It does not prove installation or playback on a physical Galaxy A06; that test remains outstanding.
- Documentation follow-up: the README Compose BOM version was aligned with the actual version catalog (2026.04.01). A documentation-only commit will trigger a fresh PR workflow.

## Next actions
1. Download the `youvideo-debug` artifact from GitHub Actions and install the APK on the Galaxy A06.
2. Validate picker permissions, local video playback, pause/resume after backgrounding, and Back navigation on-device.
3. Record device-test results and address any reproducible issues.
4. Add focused tests for media-source and playback-state handling.
5. Only after device validation, add Room-backed playback history and favorites.
6. Continue with the provider interface and official YouTube integration research, respecting the documented platform rules.
