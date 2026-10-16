# Model card

## Identity

| | |
| --- | --- |
| File | `androidApp/src/main/assets/models/snake_model.tflite` |
| Graph | Keras MobileNetV2 (`model1`) plus global average pooling and two dense layers |
| Input | `sequential_1_input`, float32, shape `[1, 224, 224, 3]` |
| Output | `sequential_3/dense_Dense2/Softmax`, float32, shape `[1, 14]` |
| Size | about 2.1 MB |
| Runtime | TensorFlow Lite 2.14 on CPU, 4 threads |

The first operator is a pad into the MobileNet stem. Normalization is not inside the graph, so the app scales pixels before `Interpreter.run`.

The input buffer is rewound after it is filled. Leaving the buffer position at the end makes TensorFlow Lite reject the input, which is how older builds skipped the network entirely.

## Labels

Order in `models/labels.txt`:

| Index | Model label | Field-guide species |
| --- | --- | --- |
| 0 | Common Krait | *Bungarus caeruleus* |
| 1 | King Cobra | *Ophiophagus hannah* |
| 2 | Monocled Cobra | *Naja kaouthia* |
| 3 | Russell's Viper | *Daboia russelii* |
| 4 | Saw-scaled Viper | *Echis carinatus* |
| 5 | Spectacled Cobra | *Naja naja* |
| 6 | Banded Racer | *Argyrogena fasciolata* |
| 7 | Checkered Keelback | *Fowlea piscator* |
| 8 | Common Rat Snake | *Ptyas mucosa* |
| 9 | Common Sand Boa | *Eryx conicus* |
| 10 | Common Trinket | *Coelognathus helena* |
| 11 | Green Tree Vine | *Ahaetulla nasuta* |
| 12 | Unknown Snake | no species; treated as unrecognized |
| 13 | No snake found | no species |

Names are matched through `ModelLabels`. A label is not attached to a species because one string contains the other. "Cobra" alone matches nothing. An unknown label is shown as unrecognized and is not marked mildly venomous.

The field guide also describes hump-nosed pit viper, green pit viper, rock python, wolf snake, cat snake, black mamba, and eastern diamondback. Those animals are not outputs of this model. A photo of one of them will be forced into one of the 14 classes.

## Two scalings

Species scores use MobileNet scaling, `(channel - 127.5) / 127.5`. On the bundled samples that scaling is the one that names the cobra, krait, Russell's viper, and green vine correctly.

The same scaling scores the bundled flower as Russell's viper at about 0.99. A second pass, `channel / 255`, scores that flower as "No snake found" at about 0.55, and scores the bundled snake photos near zero for that class. The app therefore trusts the second pass only as a gate:

- If the `[0, 1]` pass tops out at "No snake found" with confidence at least 0.45, the result is "no snake".
- Otherwise the `[-1, 1]` pass supplies the ranking.
- If the top species score is below 0.55, the result is marked low confidence and the next species scores are shown.
- "Unknown Snake", a failed interpreter, or an unreadable image never becomes a harmless species.

`scripts/check_model.py` locks the sample behavior:

| Photo | Result with the current rule |
| --- | --- |
| `cobra.jpg` | Spectacled cobra, about 1.00 |
| `krait.jpg` | Common krait, about 1.00 |
| `russell_viper.jpg` | Russell's viper, about 1.00 |
| `green_vine.jpg` | Green vine, about 0.84 |
| `non_snake.jpg` | No snake, guard about 0.55 |
| `rat_snake.jpg` | Low confidence, top class monocled cobra at about 0.43 |

The rat-snake sample is two snakes with raised forebodies. The model spreads the score across cobra classes and stays under the confidence line. The dossier for those cobras warns about rat snakes. That is a model limit, not a color guess.

Perfect scores on a few samples do not mean the model is reliable in the field. Similar photos may have been in its training set. A new phone photo can be wrong at high confidence. The non-snake gate was measured on one flower, not on every background.

## Replacing the model

A replacement has to be a float32 TFLite file with input `[1, 224, 224, 3]` and a softmax over the same label lines, in the same order. Put it at `androidApp/src/main/assets/models/snake_model.tflite`, keep the asset uncompressed, and rerun `scripts/check_model.py`. If the training pipeline used a different scale than MobileNet `[-1, 1]`, change `SnakeClassifier` and the check script together. The non-snake guard is specific to this file; a new model needs its own measurement before that gate is kept.
