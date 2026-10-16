#!/usr/bin/env python3
"""Check the bundled MobileNetV2 model against the sample photos.

Species scores use MobileNet scaling, (pixel - 127.5) / 127.5.
A second pass with pixel / 255 is only a non-snake guard: if its top class
is "No snake found" at 0.45 or more, the photo is not given a species name.
That second pass is what stops the flower sample being called a Russell's viper.

Run from the repository root with a Python environment that has ai-edge-litert,
numpy, and pillow:

    python scripts/check_model.py
"""

from pathlib import Path

import numpy as np
from PIL import Image

try:
    from ai_edge_litert.interpreter import Interpreter
except ImportError:
    from tensorflow.lite.python.interpreter import Interpreter

ROOT = Path(__file__).resolve().parents[1]
MODEL = ROOT / "androidApp/src/main/assets/models/snake_model.tflite"
LABELS = ROOT / "androidApp/src/main/assets/models/labels.txt"
SAMPLES = ROOT / "androidApp/src/main/assets/samples"
NON_SNAKE_GUARD = 0.45

EXPECTED_SPECIES = {
    "cobra.jpg": "Spectacled Cobra",
    "krait.jpg": "Common Krait",
    "russell_viper.jpg": "Russell's Viper",
    "green_vine.jpg": "Green Tree Vine",
}


def scores_for(interpreter, detail, image, mobilenet):
    pixels = np.asarray(image.resize((224, 224), Image.BILINEAR), dtype=np.float32)
    values = (pixels - 127.5) / 127.5 if mobilenet else pixels / 255.0
    interpreter.set_tensor(detail["index"], values[None, ...])
    interpreter.invoke()
    output = interpreter.get_output_details()[0]
    return interpreter.get_tensor(output["index"])[0]


def top(labels, values):
    index = int(np.argmax(values))
    return labels[index], float(values[index])


def main():
    labels = [line.strip() for line in LABELS.read_text().splitlines() if line.strip()]
    if len(labels) != 14 or labels[-1] != "No snake found":
        raise SystemExit(f"labels.txt is not the expected 14-class list: {labels}")

    interpreter = Interpreter(model_path=str(MODEL))
    interpreter.allocate_tensors()
    input_detail = interpreter.get_input_details()[0]
    failures = []

    flower = Image.open(SAMPLES / "non_snake.jpg").convert("RGB")
    flower_secondary, flower_secondary_score = top(
        labels, scores_for(interpreter, input_detail, flower, mobilenet=False)
    )
    if flower_secondary != "No snake found" or flower_secondary_score < NON_SNAKE_GUARD:
        failures.append(
            f"non_snake.jpg guard failed: {flower_secondary} {flower_secondary_score:.3f}"
        )
    else:
        print(f"non_snake.jpg  guard  No snake found {flower_secondary_score:.3f}")

    rat = Image.open(SAMPLES / "rat_snake.jpg").convert("RGB")
    rat_label, rat_score = top(labels, scores_for(interpreter, input_detail, rat, mobilenet=True))
    print(f"rat_snake.jpg  uncertain  {rat_label} {rat_score:.3f}")
    if rat_score >= 0.55:
        failures.append("rat_snake.jpg became a high-confidence call; review the uncertainty threshold")

    for filename, expected in EXPECTED_SPECIES.items():
        image = Image.open(SAMPLES / filename).convert("RGB")
        label, score = top(labels, scores_for(interpreter, input_detail, image, mobilenet=True))
        mark = "ok" if label == expected else "MISS"
        print(f"{filename:22} {mark:4} {label} {score:.3f}")
        if label != expected:
            failures.append(f"{filename} expected {expected} but was {label}")

    if failures:
        raise SystemExit("\n".join(failures))
    print("sample checks passed")


if __name__ == "__main__":
    main()
