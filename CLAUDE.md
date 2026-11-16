# CLAUDE.md

Guidance for Claude Code when working in this repository.

## Project

KMandelbrot is an Android app, written in Kotlin, that draws the Mandelbrot fractal full screen.

Current state: the app renders the set full screen in both orientations, black inside the set and coloured outside it by escape iteration count on a red-to-yellow gradient. The view can be zoomed with a pinch and moved with a drag. A menu button opens an info/settings view on top of the image, showing the image centre and iteration limit and letting the user choose a fixed or scale-based iteration limit and the two colours of the gradient.

## Product requirements

- Full-screen graphical app: the fractal fills the whole display, with system bars hidden (immersive) and no action bar.
- Works in both portrait and landscape. Rotating must keep the same region of the fractal in view, without stretching it.
- The fractal must be zoomable (pinch to zoom, drag to pan).

### Milestones

1. **First version (done):** render a static black-and-white Mandelbrot set full screen, correct in both orientations.
2. **Colours (done):** points inside the set black; points outside coloured by escape iteration count on a 256-colour gradient from red (`#FF0000`, far from the set) to yellow (`#FFFF00`, at its edge).
3. **Zoom and pan (done):** pinch to zoom, drag to pan. The current image follows the fingers; the sharp image is recalculated when they lift. Zooming out stops when the whole set fits; the screen centre stays within the set's rectangle; the iteration limit grows with zoom depth.
4. **Info and settings view (done):** a menu button opens a view covering 90% of the screen with "Info", "Palette" and "Settings". Settings: fixed or scale-based iteration limit, fixed value 2 to 100,000.
5. **Palette editor (done):** the Palette view lets the user choose the two colours the gradient is built from (default red and yellow) with a hue bar and a brightness bar; the image is recalculated with them when the view closes.
6. Later: smooth colouring, deeper zoom, double-tap.

Build only what the current task needs. Do not add settings screens, extra palettes or other extras before they are asked for.

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
- UI is Jetpack Compose (Compose BOM; `ui`, `foundation` and `material3`), with `lifecycle-viewmodel-compose` and `lifecycle-viewmodel-savedstate` for the ViewModel. Material 3 is used for the info/settings view. There is no icons library: the menu and close icons are drawn with `drawLine`. AppCompat and the View-based Material library are not used.
- The activity theme is the platform `android:Theme.Material.NoActionBar`.

## Architecture

Keep the fractal maths separate from Android so it can be unit tested on the JVM.

- **`MainActivity`** – single `ComponentActivity`. Enables edge-to-edge, hides the system bars (shown briefly on an edge swipe) and hosts `MandelbrotScreen`. The image extends under the display cutout.
- **`MandelbrotScreen`** – composable with one full-screen `Canvas`, and on top of it either `MenuButton` or, when open, `InfoSettingsPanel`; the open state is kept with `rememberSaveable`. Reports its pixel size to the ViewModel through `onSizeChanged` (this is what handles rotation, split-screen and unfolding). Reports pinch and drag steps from `detectTransformGestures` through `onTransform`, and all fingers lifting through `onGestureEnd` (a second `pointerInput`, because `detectTransformGestures` has no end callback). Draws the ViewModel's image moved and scaled by `viewport.placementOf(imageViewport)`, so during a gesture the existing image follows the fingers.
- **`InfoSettingsPanel`** (`InfoSettingsPanel.kt`) – the info/settings view: a full-screen overlay whose outer area closes it when tapped, and a Material 3 `Surface` filling 90% of the width and height (black at 80% opacity, thin border, always the dark colour scheme) with a tab row ("Info", "Palette", "Settings") and an 'X'. Because it covers the whole screen, the canvas gets no touches while it is open. The selected tab and the unapplied edits are `rememberSaveable` state, so they survive rotation and process death and reset each time the view opens. On close it hands the iteration settings and palette colours to apply to `onClose`; an invalid "Maximum iteration" text keeps the previous value. System back is not handled. `MenuButton` in the same file is the button that opens it, placed where the 'X' appears.
- **`PaletteView`** (`PaletteView.kt`) – the content of the Palette tab: a hue bar, a brightness bar (black, pure hue, white), two colour boxes (left = far from the set, right = edge of the set), a preview of the gradient between them and a Reset button. One box is always selected, and the bars edit it. Its state (the two colours, the selected box, the bar positions) lives in `InfoSettingsPanel` with the other unapplied edits. The colours change only when a bar is moved or Reset is pressed; selecting a box only moves the bars to that box's colour.
- **`MandelbrotViewModel`** – owns the current `viewport` (what the user is looking at), the `Bitmap` with the `imageViewport` it was made for, and the render job, so all survive rotation. Centre and scale are also saved in `SavedStateHandle`, which restores the view after Android kills the process in the background; a fresh start after the app is swiped away shows the whole set.
  - `onSizeChanged`: the first call restores the saved view or fits the whole set; a new size keeps centre and scale (`resizedTo`).
  - `onTransform`: updates `viewport` and cancels a running render. `onGestureEnd`: starts a render if the view changed.
  - `iterationSettings` holds the applied `IterationSettings`, also saved in `SavedStateHandle`; `imageIterationLimit` is the limit the image was calculated with. `paletteColors` holds the applied `PaletteColors`, saved the same way. `onSettingsChanged` (called when the info/settings view closes) starts one render, and only if the limit for the current view or the colours change.
  - `startRender`: takes the iteration limit from `iterationSettings`, creates a new bitmap, draws the previous image into it moved and scaled as a preview, then renders the sharp image over it. Each finished band is written into the bitmap on the main thread and bumps a `frame` counter that the canvas reads to redraw, so the preview is replaced band by band. Areas the preview does not cover are transparent, i.e. black.
- **`renderMandelbrot`** (`MandelbrotRenderer.kt`) – plain Kotlin + coroutines, no Android imports. Calculates the image for a viewport, an iteration limit and the palette colours in bands of rows, in parallel, one worker per CPU core on `Dispatchers.Default`, and hands each band to a callback. Each pixel's colour is looked up from the `iterationColors` table by its escape iteration count.
- **`Viewport`** – plain Kotlin class holding the centre of the view in the complex plane and a scale (complex units per pixel). Converts pixel coordinates to complex coordinates. Using centre + a single scale keeps the image undistorted. `Viewport.fitWholeSet` gives the view that shows the whole set with a margin for any image size. `transformedBy` applies one pinch/drag step, keeping the point under the fingers fixed; `resizedTo` keeps centre and scale for a new image size; `clamped` enforces the limits; `placementOf` says where an image made for another viewport must be drawn to line up with this one.
- **`escapeIterations`** (`Mandelbrot.kt`) – plain Kotlin: iterate `z = z² + c` up to an iteration limit and return the iteration on which the point escapes (`|z| > 2`), or `NOT_ESCAPED` (0) for a point treated as inside the set. Points in the main cardioid or the period-2 bulb are recognised by formula and return `NOT_ESCAPED` without iterating; this changes no result, only the time taken.
- **`iterationLimitForScale`** (`Mandelbrot.kt`) – the iteration limit for a scale: `BASE_ITERATION_LIMIT` (256) at a scale of 0.0025 and above, plus 940 for every tenfold magnification, capped at `ITERATION_LIMIT_CAP` (10,000), which is reached just before the deepest zoom. It depends on the scale only, not on the screen size.
- **`IterationSettings`** (`IterationSettings.kt`) – plain Kotlin: the iteration mode (`SCALE_BASED`, the default, or `FIXED`) and the fixed limit (default 256, allowed 2 to 100,000). `limitFor(scale)` gives the limit for an image: `iterationLimitForScale` or the fixed limit. `parseFixedIterationLimit` validates the text of the "Maximum iteration" field.
- **`formatCoordinate`** (`CoordinateFormat.kt`) – plain Kotlin: a coordinate as plain decimal text with 16 decimals, for the Info view.
- **`Palette.kt`** – plain Kotlin: `PaletteColors` holds the two colours of the palette (far from the set, default red; edge of the set, default yellow). `colorGradient` builds the 256 gradient colours between two colours as a straight RGB blend, and `iterationColors` builds the lookup table from the result of `escapeIterations` to an ARGB colour. Entry 0 is black (inside the set, not editable); entry n is the gradient colour at position ln(n) / ln(limit), so the colours come in stepped bands and the gradient always spans 1 iteration (far colour) to the current limit (edge colour). A given iteration count therefore gets a colour slightly nearer the far colour as the limit grows with zoom.
- **`PickerColor.kt`** – plain Kotlin: `pickerColor` turns a position of the colour picker (hue 0..360, tone 0 = black, 0.5 = pure hue, 1 = white; the HSL model at full saturation) into an ARGB colour, and `pickerPositionOf` does the reverse.
- The canvas and window backgrounds are black, and a new bitmap starts transparent, so the area that is not yet calculated is black.

### Rules that are easy to get wrong

- Never compute the fractal on the main thread or inside a draw lambda. Render off the main thread, and cancel a render that is in progress when a new one starts.
- Only modify the bitmap on the main thread, so it is never changed while it is being drawn.
- Write pixels in bulk (`IntArray` + `Bitmap.setPixels`), not one `setPixel` call at a time.
- The viewport (centre and scale) must be preserved across rotation and process death: keep it in the ViewModel and in `SavedStateHandle`, never re-derive it from the image size except on a fresh start.
- Every new viewport goes through `clamped`: scale between `Viewport.MIN_SCALE` (1e-13, the `Double` precision limit) and the `fitWholeSet` scale, centre inside the set's rectangle.
- Do not recycle the previous bitmap when replacing it; a frame that is still being drawn may reference it.
- Use `Double` for complex-plane coordinates. Zoom depth is limited by `Double` precision (`Viewport.MIN_SCALE`); that is acceptable for now.
- Do not lock the orientation in the manifest.

## Conventions

- Kotlin code follows `.claude/rules/kotlin-coding-standards.md` and the official Kotlin code style (`kotlin.code.style=official`).
- Kotlin sources live under `app/src/main/java/com/example/kmandelbrot/`, with tests mirroring them in `app/src/test/` (JVM) and `app/src/androidTest/` (device).
- Unit test the viewport mapping, the escape-time calculation, the palette, the picker colours, the iteration settings and the coordinate format; they are pure Kotlin.
- User-visible strings go in `res/values/strings.xml`.

## Git

- The default branch is `main`, tracking `origin/main` on GitHub.
- Commit or push only when asked.
- Commit messages, branch names and pull requests follow `.claude/rules/git-standards.md`.
- `tmp/` holds working files for the task skills (analysis, plans) and is not committed.
