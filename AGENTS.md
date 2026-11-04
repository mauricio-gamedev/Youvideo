# Youvideo — Agent Guidelines

## Start every session
1. Read this file.
2. Read `docs/SESSION_CHECKPOINT.md`.
3. Read the relevant sections of `docs/ROADMAP.md`.
4. Inspect the current branch, working tree, affected files, and existing tests before changing anything.

## Git workflow
- Never commit application work directly to `main`.
- Work on a focused branch named with `feat/`, `fix/`, `chore/`, or `docs/`.
- Keep commits small and descriptive.
- Open a Pull Request for review; do not merge without an explicit request.
- Do not overwrite unrelated user changes.

## Product and platform constraints
- Youvideo is an Android-first multimedia app. The current reference device is a low-memory Samsung Galaxy A06.
- Prefer Kotlin, Jetpack Compose, AndroidX Media3, Room, DataStore, and WorkManager when their use is justified.
- Keep the app responsive under memory pressure. Avoid unnecessary background services, eager image loading, unbounded caches, and heavy dependencies.
- YouTube integration must use official, permitted APIs and player experiences. Do not implement ad blocking, player circumvention, unauthorized stream extraction, or downloads that YouTube does not explicitly permit.
- Downloads must be limited to files the user owns or sources that expressly authorize downloading.
- Clearly label mock data and unfinished integrations. Never present a placeholder as a working provider.

## Engineering standards
- Keep UI, domain, data, and provider integration responsibilities separated.
- Use stable dependencies and pin versions. Avoid dynamic version selectors.
- Handle loading, empty, error, and permission states.
- Add tests for meaningful business logic and regressions.
- Before handoff, run the applicable unit tests, lint, and debug build in CI or a reproducible local environment.
- Update `docs/SESSION_CHECKPOINT.md` with implemented work, validation evidence, known issues, and the next concrete task.
- Do not claim a build passed unless the workflow or a local build confirms it.
