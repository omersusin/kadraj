# Kadraj

**A photo-first collage studio for screenshots and photos.**

Kadraj reads the shape of your images. Phone captures, desktop captures, and mixed selections each get a layout that understands their ratios. Negative space, rhythm, and framing follow from there. Everything stays on your device.

[**Download the Android APK**](https://github.com/omersusin/kadraj/releases/latest) ·
Android 10 and up · About 3 MB

## Why Kadraj?

Most collage apps squeeze every photo into the same square. Kadraj first studies the dominant orientation and the mix of ratios in your selection, then builds the composition around the images themselves. Phone screenshots stay tall, desktop captures stretch wide, and mixed sets find their own rhythm.

- **Three appearance themes** — Cinema, Paper, and Contact, selectable in Settings and remembered between launches.
- **Smart ratio analysis** — portrait, landscape, and mixed selections are treated differently.
- **Six layouts** — Auto, Grid, Flow, Hero, Strip, and Stack.
- **Nine output ratios plus Auto** — 1:1, 3:4, 4:5, 9:16, 2:3, 3:2, 16:9, 16:10, and 2:1.
- **Per-photo looks** — Original, Warm, Cool, Mono, Fade, Vivid, and Film with adjustable strength.
- **Tone adjustments** — light, contrast, color, and warmth for every photo, with one-tap Auto Enhance and Sync to All.
- **Frame controls** — drag to set the crop, zoom, tilt, flip horizontally or vertically, duplicate, or reset a photo.
- **Composition guides** — a rule-of-thirds overlay with center mark, toggled from the preview.
- **Captions** — serif or sans, adjustable size, three placements, three alignments, auto/paper/accent tones, an optional plate, and quick starter lines.
- **Undo and redo** — up to 40 steps, including continuous slider adjustments.
- **Compact control deck** — a collapsible, scrollable bottom sheet keeps the composition in view; the active tab, template, palette, photo, and look stay in reach.
- **Ten palettes** — Paper, Night, Clay, Sage, Mist, Rose, Ivory, Olive, Sunset, and Lagoon.
- **Fine tuning** — spacing, margin, corners, mat, shadow, grain, and a one-tap style randomizer.
- **On-device output** — JPEG or PNG at Auto, 2K, or 4K; saved to the `Kadraj` album in Photos.
- **Safe project flow** — leave or start over with an explicit confirmation, so a finished edit is never lost by accident.
- **No account, no cloud** — no internet permission; your photos never leave the device.

## Layouts

| Layout | When to use it |
| --- | --- |
| **Auto** | Reads the selection's orientation and variety, then picks the most balanced arrangement. |
| **Grid** | For photos with a similar ratio and an even rhythm. |
| **Flow** | A free column composition that preserves different heights. |
| **Hero** | One lead image supported by smaller companions. |
| **Strip** | A cinematic horizontal or vertical sequence. |
| **Stack** | Layered pieces with slight rotations, as if placed by hand. |

## How to use it

1. Choose 1–30 images from your gallery. Some Android photo pickers cap a
   single selection pass at 10; tap Add again for the rest.
2. Pick a template and output ratio; visual template previews and real ratios make the choice immediate.
3. Tap a photo to select it, then drag to move the crop.
4. Open the Photo tab for looks, tone adjustments, zoom, tilt, flips, duplication, or resets. Swipe the photo strip to move between images.
5. Open the Text tab to add a caption, start from an idea, and tune its type and placement.
6. Choose a size and format, save to Photos, and share directly if you like.

## Install

1. [Download the latest APK](https://github.com/omersusin/kadraj/releases/latest).
2. Allow app installs from this source in Android settings.
3. Open the APK and install.

> The APK is signed for the `com.omersusin.kadraj` package. Future releases use
> the same signing key, so updates install without losing data.

## Demo artwork

The start-screen collage and built-in layout previews use three original
illustrated scenes made for Kadraj: a sunlit window, a seaside loggia, and a
morning still life. They share the app's ink, peach, and sage palette and
contain no third-party stock photography. The demo art is illustrative only;
user photos are never uploaded or included in the app.

## Development

Requirements: JDK 17, Android SDK 35, and Gradle 8.9.

```bash
git clone https://github.com/omersusin/kadraj.git
cd kadraj
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

A signed release build needs these environment variables:

```bash
KADRAJ_KEYSTORE=/path/to/release.p12
KADRAJ_STORE_PASSWORD=...
KADRAJ_KEY_ALIAS=kadraj
KADRAJ_KEY_PASSWORD=...
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`.

GitHub Actions runs the unit tests and an R8 release build on every push to
`main`, then verifies the signed APK with `zipalign` and `apksigner`. Tags in
the `v*` format publish the APK to GitHub Releases.

## Technical notes

- Kotlin 2.0 and Jetpack Compose; no Material library.
- A pure Kotlin layout engine with JVM unit tests.
- Preview and export share the same renderer, including tone and look filters.
- A photo-first interface with three appearance themes, compact collapsible controls, and subtle motion, without a Material dependency. Cinema is the default dark stage; Paper is a warm editorial skin; Contact is a monochrome contact-sheet skin.
- R8 minification, resource shrinking, and a signed release configuration.
- `minSdk 29`, `targetSdk 35`.

## Typefaces

Headlines are set in **Fraunces**; the interface uses **Manrope**. Both are
licensed under the SIL Open Font License 1.1, with licenses in
[`licenses/`](licenses/).

## License

[MIT](LICENSE) · Ömer SÜSİN
