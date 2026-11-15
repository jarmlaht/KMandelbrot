# CLAUDE.md

Guidance for Claude Code when working in this repository.

## Project

KMandelbrot is an Android app, written in Kotlin, that draws the Mandelbrot fractal full screen.

Current state: milestone 1 is implemented (static black-and-white render, full screen, both orientations). Zoom and pan are not implemented yet.

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
| Single test class | `.\gradlew.bat testDebugUnitTest --tests "com.example.kmandelbrot.ViewportTest"` |
| Instrumented tests (needs a device) | `.\gradlew.bat connectedDebugAndroidTest` |
| Lint | `.\gradlew.bat lintDebug` |

## Tech stack

- Single module `app`, package and namespace `com.example.kmandelbrot`.
- AGP 9.5.1 with Gradle Kotlin DSL; `minSdk` 34, `targetSdk`/`compileSdk` 37, Java 11.
- AGP 9 has built-in Kotlin support. Do not add the `org.jetbrains.kotlin.android` plugin.
- The Compose compiler plugin version must equal the Kotlin version bundled with AGP (`kotlin` in the version catalog, currently 2.2.10; check with `.\gradlew.bat buildEnvironment`). Avoid libraries built against a newer Kotlin than that; this is why coroutines is pinned to 1.10.2 (1.11.0 is built against Kotlin 2.2.20).
- Dependencies are declared in the version catalog `gradle/libs.versions.toml`. Add new ones there and reference them as `libs.…`; never hard-code versions in `build.gradle.kts`. Ask before adding a dependency.
- UI is Jetpack Compose (Compose BOM; `ui` and `foundation` only). There is no Material library yet; add Material 3 when the first real UI component is needed. AppCompat and the View-based Material library are not used.
- The activity theme is the platform `android:Theme.Material.NoActionBar`.

## Architecture

Keep the fractal maths separate from Android so it can be unit tested on the JVM.

- **`MainActivity`** – single `ComponentActivity`. Enables edge-to-edge, hides the system bars (shown briefly on an edge swipe) and hosts `MandelbrotScreen`. The image extends under the display cutout.
- **`MandelbrotScreen`** – composable with one full-screen `Canvas`. Reports its pixel size to the ViewModel through `onSizeChanged` (this is what handles rotation, split-screen and unfolding) and draws the ViewModel's image.
- **`MandelbrotViewModel`** – owns the `Bitmap` and the render job, so both survive rotation. A size change cancels the running render, creates a new bitmap and starts a new render. Each finished band is written into the bitmap on the main thread and bumps a `frame` counter that the canvas reads to redraw, which makes the image appear progressively.
- **`renderMandelbrot`** (`MandelbrotRenderer.kt`) – plain Kotlin + coroutines, no Android imports. Calculates the image in bands of rows, in parallel, one worker per CPU core on `Dispatchers.Default`, and hands each band to a callback.
- **`Viewport`** – plain Kotlin class holding the centre of the view in the complex plane and a scale (complex units per pixel). Converts pixel coordinates to complex coordinates. Using centre + a single scale keeps the image undistorted. `Viewport.fitWholeSet` gives the view that shows the whole set with a margin for any image size.
- **`isInMandelbrotSet`** (`Mandelbrot.kt`) – plain Kotlin: iterate `z = z² + c` up to `MAX_ITERATIONS` (256) and report whether the point escapes (`|z| > 2`).

### Rules that are easy to get wrong

- Never compute the fractal on the main thread or inside a draw lambda. Render off the main thread, and cancel a render that is in progress when a new one starts.
- Only modify the bitmap on the main thread, so it is never changed while it is being drawn.
- Write pixels in bulk (`IntArray` + `Bitmap.setPixels`), not one `setPixel` call at a time.
- Today the viewport is always derived from the image size with `fitWholeSet`. Once zoom and pan exist, the viewport (centre and scale) must be preserved across rotation and process death.
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
