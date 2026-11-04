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
- Target: Android app, a multimedia hub for compatible and authorized sources.
- Priority: balanced UX, features, and performance.
- Visual direction: AMOLED-first dark interface.
- Reference device: Samsung Galaxy A06 / entry-level hardware.

## Implemented
- Kotlin + Jetpack Compose foundation using AGP built-in Kotlin support.
- AMOLED-first theme and bottom navigation (Home, Explore, Downloads, Library).
- Android system document picker for user-selected videos through Storage Access Framework.
- Media3 ExoPlayer local playback with built-in PlayerView controls.
- Persistable URI read permission requested when supported by the document provider; no broad storage permission.
- Player pauses on app background and releases resources when the player screen closes.
- Android Back action closes the local player screen.
- User confirmed the debug APK installs and works on the Samsung Galaxy A06.
- Provider abstraction with explicit provider IDs/capabilities and a shared media model.
- PeerTube public search against the configured instance, with result playback via the instance embed player.
- Internet Archive movie catalog search and result playback via the official Archive.org embedded player.
- Optional YouTube Data API search connector, gated by a local/Gradle API key. It checks video status and excludes made-for-kids or non-embeddable/unknown-status items before playback.
- Remote playback screen delegates to source-provided embedded players; YouTube WebView requests include the app Referer identity.
- Bounded JSON HTTP client with connect/read timeouts and a response-size ceiling.
- Unit tests for duration formatting and PeerTube instance URL validation.
- GitHub Actions workflow for unit tests, lint, debug APK assembly, and artifact upload.
- Provider setup and compliance notes in `docs/PROVIDERS.md`.

## Not implemented / limitations
- YouTube remains inactive until a restricted `YOUTUBE_API_KEY` is provided locally; CI does not package a key.
- No account-based provider login or OAuth flow.
- No provider downloads, including YouTube or Internet Archive.
- No persistent playback history, favorites, or playlists.
- No release signing.
- Live connector search/playback has not yet been verified on the physical Galaxy A06; only the earlier local player build was confirmed by the user.

## Build and validation history
- CI previously required fixes for retired SDK packages, Android API 37 availability, AGP 9 built-in Kotlin, and PlayerView/ExoPlayer name resolution.
- CI run #44 for application commit `430bebc68b9b7f38c78439db87f93256a11bc3c9` passed `testDebugUnitTest`, `lintDebug`, `assembleDebug`, and artifact upload.
- Debug APK artifact: `youvideo-debug`, artifact ID `13365276407`, 21,133,756 bytes, SHA-256 `21a6a177abac915bccee2a3709a02d07643c389309d9118e34bc7dbbe1a15b38`; it was installed and user-tested on the A06 successfully.
- Connector implementation is on the current PR branch and must pass a fresh CI run before a new APK is considered ready.
- The YouTube API key must never be committed or embedded in a public CI artifact.

## Next actions
1. Verify the connector branch with unit tests, lint, and debug assembly in CI.
2. Install the new connector APK on the A06 and test PeerTube search/playback and Internet Archive search/playback.
3. Configure a restricted YouTube Data API key locally and test only eligible embeddable videos.
4. Add Room-backed history/favorites after connector runtime behavior is stable.
5. Add user-configurable PeerTube instance selection and source preferences.
