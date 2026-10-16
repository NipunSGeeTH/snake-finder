# Architecture

SnakeID is a Kotlin Multiplatform project with one Android application. Shared code is Kotlin and Compose Multiplatform. The only platform that runs the neural network is Android.

## Modules

`androidApp` is the installable application. It owns the manifest, the file-provider paths, and the assets under `src/main/assets/`:

- `models/snake_model.tflite`
- `models/labels.txt`
- `samples/*.jpg`

`composeApp` is an Android library plus iOS source stubs. The application depends on it. Keeping the model in the application module, with `noCompress` for `.tflite`, lets the runtime memory-map the file.

iOS source exists so the shared declarations have an `actual` implementation. The iOS classifier returns "not available". It does not invent a species.

## Packages

| Package | Responsibility |
| --- | --- |
| `com.snakeid` | Activity entry, theme, and tab navigation |
| `com.snakeid.domain` | Species records, label map, and the rules that turn scores into a result |
| `com.snakeid.ui` | One file per screen |
| `com.snakeid.history` | In-memory list plus the records written to disk |
| `com.snakeid.platform` | `expect`/`actual` for the camera, the model, the clock, and phone calls |

`Identification` is pure Kotlin. Unit tests in `composeApp/src/commonTest` cover label mapping and the non-snake guard without starting Android.

## What happens to a photo

1. The camera or gallery returns JPEG bytes. EXIF rotation is applied and the long side is limited before the bitmap is kept.
2. `SnakeClassifier` loads `models/snake_model.tflite` once and runs it on a background dispatcher.
3. The bitmap is scaled to 224×224. Two float inputs are built from the same pixels:
   - species pass: `(pixel - 127.5) / 127.5`, the MobileNet range
   - guard pass: `pixel / 255`
4. `Identification.fromScores` applies the rules in [model.md](model.md) and attaches the species dossier.
5. The Identify screen renders the banner, the top scores, and a dossier link.
6. The top result is appended to `filesDir/scan_history.json`, except when the model did not run.

The classifier does not guess a species from average color. If the interpreter cannot run, the screen says identification is unavailable.

## What was removed

The previous model screen downloaded URLs that are not published, then wrote a 1 KB placeholder and marked the pack ready. The classifier ignored files that small and kept using the bundled model, so the UI and the engine disagreed. Placeholder packs and the color fallback are gone.

History used to start with two invented scans. It now starts empty and survives process death.

## UI state

`App` holds the selected photo, the latest predictions, and the open dossier. Tabs are an enum, not separate navigation routes. The species dialog uses a height-capped scrolling column. A lazy list inside an alert dialog crashes Compose because the dialog measures it with unbounded height.

## Threading

TensorFlow Lite's `Interpreter` is not thread-safe. `classify` and `close` share one lock. A new photo bumps a request id so a slow result cannot overwrite a newer one.
