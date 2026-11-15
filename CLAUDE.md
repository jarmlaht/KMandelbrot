# CLAUDE.md

Guidance for Claude Code when working in this repository.

## Project

KMandelbrot is an Android app, written in Kotlin, that draws the Mandelbrot fractal full screen.

Current state: a bare Android Studio template. There is no Activity, no Kotlin source under `app/src/main`, and no launcher entry in `AndroidManifest.xml` yet.

## Product requirements

- Full-screen graphical app: the fractal fills the whole display, with system bars hidden (immersive) and no action bar.
- Works in both portrait and landscape. Rotating must keep the same region of the fractal in view, without stretching it.
- The fractal must be zoomable (pinch to zoom, drag to pan).

### Milestones

1. **First version:** render a static black-and-white Mandelbrot set (points inside the set black, outside white) full screen, correct in both orientations.
2. **Zoom and pan** with touch gestures.
3. Later: colour palettes, smoother/progressive rendering, deeper zoom.

Build only what the current milestone needs. Do not add colouring, settings screens or other extras before they are asked for.

## Build and test

Run from the project root (PowerShell on Windows; use `./gradlew` in Bash).

| Task | Command |
| --- | --- |
| Debug build | `.\gradlew.bat assembleDebug` |
| Install on a connected device/emulator | `.\gradlew.bat installDebug` |
| Unit tests (JVM) | `.\gradlew.bat testDebugUnitTest` |
| Single test class | `.\gradlew.bat testDebugUnitTest --tests "com.example.kmandelbrot.ExampleUnitTest"` |
| Instrumented tests (needs a device) | `.\gradlew.bat connectedDebugAndroidTest` |
| Lint | `.\gradlew.bat lintDebug` |

## Tech stack

- Single module `app`, package and namespace `com.example.kmandelbrot`.
- AGP 9.5.1 with Gradle Kotlin DSL; `minSdk` 34, `targetSdk`/`compileSdk` 37, Java 11.
- AGP 9 has built-in Kotlin support. Do not add the `org.jetbrains.kotlin.android` plugin.
- Dependencies are declared in the version catalog `gradle/libs.versions.toml`. Add new ones there and reference them as `libs.…`; never hard-code versions in `build.gradle.kts`. Ask before adding a dependency.
- UI is the Android View system (AppCompat + Material), not Jetpack Compose.

## Architecture

Keep the fractal maths separate from Android so it can be unit tested on the JVM.

- **`MainActivity`** – single activity. Sets up edge-to-edge/immersive mode and hosts the fractal view. Needs an `<activity>` entry with the `MAIN`/`LAUNCHER` intent filter in the manifest, and a `NoActionBar` theme.
- **`MandelbrotView`** – custom `View` that renders into a `Bitmap` sized to the view and draws it in `onDraw`. Re-render on `onSizeChanged` (this is what handles rotation) and when the viewport changes.
- **Viewport** – plain Kotlin class holding the centre of the view in the complex plane and a scale (complex units per pixel). Converts pixel coordinates to complex coordinates. Using centre + a single scale, rather than fixed min/max bounds, keeps the aspect ratio correct in both orientations.
- **Mandelbrot calculation** – plain Kotlin function(s) with no Android imports: iterate `z = z² + c` up to a maximum iteration count and report whether the point escapes (`|z| > 2`).

### Rules that are easy to get wrong

- Never compute the fractal on the main thread or inside `onDraw`. Render off the main thread (coroutines on `Dispatchers.Default`), cancel a render that is in progress when a new one starts, and post the finished bitmap back to the view.
- Write pixels in bulk (`IntArray` + `Bitmap.setPixels`), not one `setPixel` call at a time.
- Preserve the viewport (centre and scale) across rotation and process death, using a `ViewModel` or saved instance state.
- Use `Double` for complex-plane coordinates. Zoom depth is limited by `Double` precision; that is acceptable for now.
- Do not lock the orientation in the manifest.

## Conventions

- Kotlin official code style (`kotlin.code.style=official`).
- Kotlin sources live under `app/src/main/java/com/example/kmandelbrot/`, with tests mirroring them in `app/src/test/` (JVM) and `app/src/androidTest/` (device).
- Unit test the viewport mapping and the escape-time calculation; they are pure Kotlin.
- User-visible strings go in `res/values/strings.xml`.

## Git

- The default branch is `main`, tracking `origin/main` on GitHub.
- Commit or push only when asked.
- Commit messages use the Conventional Commits format (`feat: …`, `fix: …`, `chore: …`) and end with the `Co-Authored-By` trailer for Claude.
- Task work happens on `feature/task-<number>` branches created from `main`, merged through pull requests.
- `tmp/` holds working files for the task skills (analysis, plans) and is not committed.
