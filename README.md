# SnakeID

SnakeID is an offline Android app for a first look at a snake photograph. It names the animal with a small on-device model, shows whether that species is venomous, and lists the first-aid steps to use while getting to a hospital.

It is not a medical device. A bite is an emergency even when the app says the snake is harmless or that no snake was found. Read [docs/safety.md](docs/safety.md) before relying on any screen.

## What this build actually does

- Identifies a photo with a bundled MobileNetV2 model: 14 classes, float32, about 2 MB, no network.
- Covers the South Asian snakes in that label list, including cobra, krait, Russell's viper, and saw-scaled viper, plus a "no snake" class.
- Opens the camera or the gallery, and can run six bundled sample photos with no signal.
- Shows venom status, look-alikes, and a field guide for 19 species. The guide is larger than the model. The camera cannot name every species in the guide.
- Stores identification history on the phone.

It does not download extra model packs. Earlier builds pretended a failed download was a 100-species or 1,784-species model. Those files were not real weights, so that path was removed.

## Project layout

```
androidApp/          Android application, manifest, and bundled model assets
composeApp/          Shared Compose UI and Android TensorFlow Lite runtime
  src/commonMain/    UI, species data, and the identification rules
  src/androidMain/   Camera, gallery, model loading, history file
  src/iosMain/       Stubs. Identification is not implemented on iOS
docs/                Architecture, model card, user guide, and safety notes
scripts/check_model.py
```

The package map and the path a photo takes are in [docs/architecture.md](docs/architecture.md). The model card is in [docs/model.md](docs/model.md). How to use the screens is in [docs/user-guide.md](docs/user-guide.md).

## Build

- JDK 21
- Android SDK 34 (`sdk.dir` in `local.properties`)
- The Gradle wrapper in this repository (Gradle 8.7)

```bash
./gradlew :composeApp:testDebugUnitTest :androidApp:assembleDebug
```

The debug APK is written to `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.

```bash
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

The model file must stay uncompressed in the APK. `androidApp/build.gradle.kts` sets `noCompress` for `tflite`. Compressing it makes `AssetManager.openFd` fail and the identifier will report that the model is not loaded.

## Check the bundled model

From a Python environment with `ai-edge-litert`, `numpy`, and `pillow`:

```bash
python scripts/check_model.py
```

The script checks the six sample photos. It does not train anything.

## Safety boundary

Show the result, the confidence, and the look-alikes. Do not decide treatment from the percentage. If someone was bitten, keep them still and go to a hospital. Antivenom is a hospital treatment.
