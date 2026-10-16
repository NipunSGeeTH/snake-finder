package com.snakeid.domain

enum class ModelEngine {
    TFLITE,
    ONNX
}

data class ModelOffer(
    val id: String,
    val name: String,
    val summary: String,
    val speciesCount: Int,
    val sizeMb: Int,
    val sizeBytes: Long,
    val engine: ModelEngine,
    val builtIn: Boolean,
    val downloadUrl: String,
    val credit: String
)

object ModelCatalog {
    const val QUICK = "quick14"
    const val WIDE = "snakesense135"

    val offers: List<ModelOffer> = listOf(
        ModelOffer(
            id = QUICK,
            name = "Quick South Asia",
            summary = "Built into the app. Fourteen classes, including the common deadly snakes this small network was trained on, plus a class for photos with no snake. Works with no network.",
            speciesCount = 14,
            sizeMb = 2,
            sizeBytes = 0,
            engine = ModelEngine.TFLITE,
            builtIn = true,
            downloadUrl = "",
            credit = "Bundled MobileNetV2."
        ),
        ModelOffer(
            id = WIDE,
            name = "Wide catalog, 135 species",
            summary = "A larger ResNet trained as SnakeSense. It can name 135 species from several continents, including Indian cobra, Russell's viper, king cobra, and the long-nosed whip snake. The author reported about 66 percent training accuracy. The common krait is not in this list. A flower can still be given a snake name by this network, so the app checks the quick model first and can still say no snake was found.",
            speciesCount = 135,
            sizeMb = 171,
            sizeBytes = 178_576_161,
            engine = ModelEngine.ONNX,
            builtIn = false,
            downloadUrl = "https://github.com/NipunSGeeTH/snake-finder/releases/download/models/snakesense-v1.onnx",
            credit = "SnakeSense by Stephen Li, MIT license. Weights hosted with that notice on this project's models release."
        )
    )

    fun byId(id: String): ModelOffer? = offers.find { it.id == id }
}

data class WideLabel(
    val index: Int,
    val scientific: String,
    val common: String,
    val poisonous: Boolean
)

fun parseWideLabels(text: String): List<WideLabel> {
    return text.lineSequence().mapNotNull { line ->
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("index\t")) return@mapNotNull null
        val parts = trimmed.split('\t')
        if (parts.size < 4) return@mapNotNull null
        val index = parts[0].toIntOrNull() ?: return@mapNotNull null
        WideLabel(
            index = index,
            scientific = parts[1],
            common = parts[2].ifBlank { parts[1] },
            poisonous = parts[3] == "1"
        )
    }.sortedBy { it.index }.toList()
}

fun widePredictions(scores: FloatArray, labels: List<WideLabel>): List<Prediction> {
    if (labels.isEmpty() || scores.size != labels.size) {
        return listOf(Identification.unavailable("The wide model output did not match its label file."))
    }
    val order = scores.indices.sortedByDescending { scores[it] }.take(3)
    val topScore = scores[order.first()]
    val uncertain = topScore < Identification.LOW_CONFIDENCE
    return order.map { index ->
        val label = labels[index]
        val confidence = scores[index].coerceIn(0f, 1f)
        val known = SnakeSpeciesDatabase.findSpecies(label.scientific)
        if (known != null) {
            Prediction(
                kind = ResultKind.SPECIES,
                speciesId = known.id,
                scientificName = known.scientificName,
                commonName = known.commonName,
                confidence = confidence,
                venomStatus = known.venomStatus,
                dangerLevel = known.dangerLevel,
                uncertain = uncertain,
                advice = known.firstAidAdvice,
                lookAlikes = known.lookAlikes,
                symptoms = known.biteSymptoms
            )
        } else {
            val venomous = label.poisonous
            Prediction(
                kind = ResultKind.SPECIES,
                speciesId = null,
                scientificName = label.scientific,
                commonName = label.common,
                confidence = confidence,
                venomStatus = if (venomous) VenomStatus.VENOMOUS else VenomStatus.NON_VENOMOUS,
                dangerLevel = if (venomous) 4 else 1,
                uncertain = true,
                advice = if (venomous) {
                    "This species is outside the field guide. The model table marks it venomous. Keep the person still and go to a hospital. ${Identification.PHOTO_LIMIT}"
                } else {
                    "This species is outside the field guide. The model table marks it non-venomous, and that flag can be wrong. ${Identification.PHOTO_LIMIT}"
                }
            )
        }
    }
}
