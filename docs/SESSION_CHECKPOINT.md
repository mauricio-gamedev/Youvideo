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
- GitHub Actions workflow for unit tests, lint, and debug APK assembly.
- Contributor guidelines, product roadmap, and project documentation.

## Not implemented
- YouTube/provider integrations.
- Search against live providers.
- Persistent playback history, favorites, or playlists.
- Authorized download manager.
- Release signing.

## Build and validation history
- CI initially failed because the Android setup action requested the retired SDK package `tools`; workflow was corrected.
- CI then failed because API 37 was not available as the stable SDK package; the project was moved to Android API 36.
- The next CI run reached Gradle and exposed the AGP 9 built-in Kotlin incompatibility with the legacy `org.jetbrains.kotlin.android` plugin.
- Removed the obsolete Kotlin Android plugin and migrated to AGP built-in Kotlin; removed legacy `kotlinOptions` so Kotlin JVM target follows Java target compatibility.
- Media3 1.11.1 added for local playback; the player observes the Activity LifecycleOwner already supplied by Activity Compose, avoiding Lifecycle 2.11.0's API 37 compileSdk requirement.
- Latest player CI run failed at AAR metadata validation because Lifecycle 2.11.0 requires compileSdk 37; that dependency was removed without changing the stable API 36 baseline.
- CI for the latest player commit is pending/running; do not claim the APK is build-validated until Actions succeeds.
- No successful APK artifact has been confirmed yet.

## Next actions
1. Verify the latest GitHub Actions run (tests, lint, debug APK).
2. Fix any compiler or lint failures found in the CI logs.
3. Install the generated APK on the Galaxy A06 and test local video selection, playback, pause/resume, and closing.
4. Add Room-backed playback history only after the first device test.
5. Continue with the provider interface and official YouTube integration research.
