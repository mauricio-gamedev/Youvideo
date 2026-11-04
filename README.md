# Youvideo

**Youvideo** is an Android-first multimedia hub focused on a clean viewing experience, an organized library, and efficient playback on modest devices.

> Status: early MVP foundation. The current app is a UI and navigation scaffold; provider integrations, playback, and downloads are not implemented yet.

## Product direction

- Discover and organize content from compatible providers.
- Play local media and media from sources that explicitly permit playback.
- Manage authorized downloads with clear status and storage controls.
- Keep the interface responsive and memory-conscious, including on entry-level Android devices.

YouTube support will use official, permitted integration paths. The app will not block YouTube ads, circumvent its player, extract unauthorized streams, or download content outside the permissions provided by YouTube.

## Technology

- Kotlin 2.4.21
- Android Gradle Plugin 9.3.1
- Jetpack Compose (BOM 2026.08.00)
- Material 3
- Gradle 9.5.0
- JDK 17

## Build

Open the repository in a recent Android Studio version with Android SDK 37 installed, or use Gradle 9.5.0 with JDK 17:

```bash
gradle assembleDebug
gradle test
gradle lint
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Development workflow

Read [AGENTS.md](AGENTS.md), [the roadmap](docs/ROADMAP.md), and [the session checkpoint](docs/SESSION_CHECKPOINT.md) before making changes. Application work belongs on a feature branch and should be submitted through a Pull Request.

## Project status

See [docs/SESSION_CHECKPOINT.md](docs/SESSION_CHECKPOINT.md) for the current implementation state and validation evidence.
