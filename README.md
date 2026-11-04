# Youvideo

**Youvideo** is an Android-first multimedia hub focused on a clean viewing experience, an organized library, and efficient playback on modest devices.

> Status: connector MVP in development. Local videos play on the Galaxy A06. PeerTube and Internet Archive have catalog search and embedded playback connectors; YouTube is optional and requires a restricted API key.

## Product direction

- Discover and organize content from compatible providers.
- Play local media and media from sources that explicitly permit playback.
- Manage authorized downloads only when a source expressly allows them.
- Keep the interface responsive and memory-conscious, including on entry-level Android devices.

YouTube support uses the official Data API and embedded player only. The app does not block YouTube ads, circumvent its player, extract streams, or download content outside YouTube's permissions.

## Technology

- Kotlin 2.4.21
- Android Gradle Plugin 9.3.1
- Jetpack Compose (BOM 2026.04.01)
- AndroidX Media3 1.11.1
- Gradle 9.5.0
- JDK 17

## Provider connectors

See [docs/PROVIDERS.md](docs/PROVIDERS.md) for provider setup, API references, and limitations.

- PeerTube: public instance search and official embedded playback.
- Internet Archive: movie catalog search and official embedded playback.
- YouTube: optional Data API search, status checks, and official embedded playback when a restricted key is configured.

No connector exposes downloads in this MVP.

## Build

Open the repository in a recent Android Studio version with Android SDK 36 installed, or use Gradle 9.5.0 with JDK 17:

```bash
gradle --no-daemon testDebugUnitTest
gradle --no-daemon lintDebug
gradle --no-daemon assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## YouTube API key

Set `YOUTUBE_API_KEY` in your ignored local `local.properties` file or pass it as a Gradle property. Never commit the key or attach a key-bearing APK to a public CI artifact. See [the provider guide](docs/PROVIDERS.md).

## Development workflow

Read [AGENTS.md](AGENTS.md), [the roadmap](docs/ROADMAP.md), and [the session checkpoint](docs/SESSION_CHECKPOINT.md) before making changes. Application work belongs on a feature branch and should be submitted through a Pull Request.

## Local video playback

The Home and Library screens can open the Android system document picker for a user-selected video. Youvideo retains read access when the provider supports a persistable URI grant and plays the selected file with AndroidX Media3. The player pauses when the app moves to the background and releases its resources when closed. No broad storage permission is requested.
