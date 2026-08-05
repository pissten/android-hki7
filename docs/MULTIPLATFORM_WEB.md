# HKI 7 Compose Multiplatform web migration

This branch ports HKI 7 to the browser without recreating its UI in HTML, CSS, React, or another design system.

## Non-negotiable migration rule

The final Android and browser clients must render the same shared Compose composables. Platform-specific source sets may replace operating-system services, storage, WebView/HTML interop, permissions, notifications, location, and app lifecycle behavior, but they must not introduce a second visual implementation of HKI 7.

## Baseline

`multiplatform-web` was reset to the upstream `jimz011/android-hki7` main commit `d9f9a324f08d7b7b5453704de89e43264cbae445` before web work started.

The existing `app` module remains the Android application during the migration. The new `webApp` module is initially an isolated Kotlin/Wasm entry point so Gradle, browser packaging, CI artifacts, and static deployment can be validated before moving production UI files.

## Migration sequence

1. Establish a green Kotlin/Wasm browser build and static deployment artifact.
2. Extract platform-neutral theme tokens, models, and UI primitives from `app` into shared Compose source sets.
3. Move Home Assistant protocol models and browser-compatible Ktor communication into shared code.
4. Introduce platform interfaces for storage, URLs/intents, lifecycle, notifications, location, and embedded web content.
5. Move complete screens and dialogs to shared Compose code in dependency order.
6. Replace the temporary web bootstrap with the same root HKI 7 composable used by Android.
7. Add browser persistence, routing, full-screen behavior, and deployment configuration.
8. Remove migration-only scaffolding after UI parity and functional tests pass.

## Build

```bash
./gradlew :webApp:wasmJsBrowserDistribution
```

The static website is generated in:

```text
webApp/build/dist/wasmJs/productionExecutable
```

## Docker runtime

After building the web distribution:

```bash
docker build -f webApp/Dockerfile -t hki7-web .
docker run --rm -p 8080:80 hki7-web
```

Then open `http://localhost:8080`.

## Current status

The browser target, entry point, CI workflow, artifact output, and Nginx runtime are being validated. `Hki7WebBootstrap` is explicitly temporary and is not intended to become a parallel UI implementation.
