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
- User confirmed the local-player debug APK installs and works on the Samsung Galaxy A06.
- Provider abstraction with explicit provider IDs/capabilities and a shared media model.
- PeerTube public search against the configured instance, with result playback via the instance embed player.
- PeerTube result URLs are restricted to the configured instance host.
- Internet Archive movie catalog search and result playback via the official Archive.org embedded player.
- Optional YouTube Data API search connector, gated by a local/Gradle API key. It checks video status and excludes made-for-kids, non-embeddable, and unknown-status items before playback.
- Remote playback delegates to source-provided embedded players; YouTube WebView requests include the app Referer identity.
- Bounded JSON HTTP client with connect/read timeouts and a response-size ceiling.
- Unit tests for duration formatting and provider URL validation.
- GitHub Actions workflow for unit tests, lint, debug APK assembly, and artifact upload.
- Provider setup and compliance notes in `docs/PROVIDERS.md`.

## Not implemented / limitations
- YouTube remains inactive until a restricted `YOUTUBE_API_KEY` is provided locally; CI does not package a key.
- No account-based provider login or OAuth flow.
- No provider downloads, including YouTube or Internet Archive.
- No persistent playback history, favorites, or playlists.
- No release signing.
- Live connector search/playback has not yet been verified on the physical Galaxy A06; only the earlier local-player build was confirmed by the user.

## Build and validation history
- CI previously required fixes for retired SDK packages, Android API 37 availability, AGP 9 built-in Kotlin, PlayerView/ExoPlayer name resolution, and Gradle Properties import.
- CI run #44 passed for the local-player build; the user installed and tested that APK successfully on the Galaxy A06.
- Connector implementation at commit `5997eeadde2f7edb9954990949ae8380e6dfe8f2` passed CI run #49: `testDebugUnitTest`, `lintDebug`, `assembleDebug`, and artifact upload.
- Connector debug artifact: `youvideo-debug`, artifact ID `13367081246`, 21,185,250 bytes, SHA-256 `b8aa790e29f3819877ad441c9e763fac35061ccec8a1b451e94d4fe73f5f88c4`; created 2026-11-04 21:52 UTC and expires 2026-11-18 21:52 UTC.
- The successful CI build confirms compilation and packaging, but it is not yet a physical-device test of connector search/playback.
- The YouTube API key must never be committed or embedded in a public CI artifact.

## Next actions
1. Complete the final embedded-player viewport adjustment and verify it in CI.
2. Install the latest connector APK on the A06 and test PeerTube search/playback and Internet Archive search/playback.
3. Configure a restricted YouTube Data API key locally and test only eligible embeddable videos.
4. Add Room-backed history/favorites after connector runtime behavior is stable.
5. Add user-configurable PeerTube instance selection and provider preferences.
