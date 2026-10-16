package com.snakeid.domain

/**
 * Turns two model score vectors into a result the UI can show safely.
 *
 * Species scores use a stretched photo scaled to [-1, 1]. That pass is what
 * names the bundled cobra, krait, Russell's viper, and green vine on a phone.
 * It also calls the bundled flower a Russell's viper. A second pass, the same
 * photo letterboxed on gray and scaled to [0, 1], returns "No snake found"
 * for that flower and does not do so for those snake photos. Measured on a
 * Galaxy M21. The second pass is only a non-snake guard.
 */
object Identification {
    const val NON_SNAKE_GUARD = 0.45f
    const val LOW_CONFIDENCE = 0.55f
    const val TOP_RESULTS = 3

    const val PHOTO_LIMIT =
        "A photo can miss the snake or name the wrong species. If someone was bitten, go to a hospital even when this screen says harmless or no snake."

    fun fromScores(
        primary: FloatArray,
        secondary: FloatArray,
        labels: List<String>
    ): List<Prediction> {
        if (labels.isEmpty() || primary.size != labels.size || secondary.size != labels.size) {
            return listOf(unavailable("The model output did not match its label list."))
        }
        val secondaryTop = rank(secondary, labels).first()
        if (ModelLabels.isNoSnake(secondaryTop.label) && secondaryTop.confidence >= NON_SNAKE_GUARD) {
            return listOf(noSnake(secondaryTop.confidence))
        }
        val ranked = rank(primary, labels)
        val top = ranked.first()
        if (ModelLabels.isNoSnake(top.label) && top.confidence >= NON_SNAKE_GUARD) {
            return listOf(noSnake(top.confidence))
        }
        if (ModelLabels.isUnknown(top.label)) {
            return listOf(unrecognized(top.confidence))
        }
        val speciesScores = ranked.filter { score ->
            !ModelLabels.isNoSnake(score.label) && !ModelLabels.isUnknown(score.label)
        }.take(TOP_RESULTS)
        if (speciesScores.isEmpty()) {
            return listOf(unrecognized(top.confidence))
        }
        val uncertain = speciesScores.first().confidence < LOW_CONFIDENCE
        return speciesScores.map { score -> speciesPrediction(score, uncertain) }
    }

    fun unreadableImage(): Prediction {
        return unavailable("This image could not be read. Use a JPEG or PNG photo of the whole snake.")
    }

    fun unavailable(reason: String): Prediction {
        return Prediction(
            kind = ResultKind.UNAVAILABLE,
            speciesId = null,
            scientificName = "",
            commonName = "Identification unavailable",
            confidence = 0f,
            venomStatus = VenomStatus.NON_VENOMOUS,
            dangerLevel = 0,
            uncertain = true,
            advice = "$reason $PHOTO_LIMIT"
        )
    }

    private fun rank(scores: FloatArray, labels: List<String>): List<ClassScore> {
        return scores.mapIndexed { index, score ->
            ClassScore(labels[index], score)
        }.sortedByDescending { it.confidence }
    }

    private fun noSnake(confidence: Float): Prediction {
        return Prediction(
            kind = ResultKind.NO_SNAKE,
            speciesId = null,
            scientificName = "",
            commonName = "No snake found",
            confidence = confidence.coerceIn(0f, 1f),
            venomStatus = VenomStatus.NON_VENOMOUS,
            dangerLevel = 0,
            uncertain = confidence < LOW_CONFIDENCE,
            advice = "No snake was recognized in this photo. $PHOTO_LIMIT"
        )
    }

    private fun unrecognized(confidence: Float): Prediction {
        return Prediction(
            kind = ResultKind.UNRECOGNIZED,
            speciesId = null,
            scientificName = "",
            commonName = "Species not recognized",
            confidence = confidence.coerceIn(0f, 1f),
            venomStatus = VenomStatus.VENOMOUS,
            dangerLevel = 3,
            uncertain = true,
            advice = "The photo does not match a species this model knows. Treat it as potentially dangerous until a clinician or an experienced handler says otherwise. $PHOTO_LIMIT",
            lookAlikes = "This model has no class for several medically important snakes, including the hump-nosed pit viper."
        )
    }

    private fun speciesPrediction(score: ClassScore, uncertain: Boolean): Prediction {
        val species = ModelLabels.speciesIdFor(score.label)?.let { id ->
            SnakeSpeciesDatabase.speciesList.find { it.id == id }
        } ?: SnakeSpeciesDatabase.findSpecies(score.label)
        if (species == null) {
            return Prediction(
                kind = ResultKind.UNRECOGNIZED,
                speciesId = null,
                scientificName = score.label,
                commonName = "Unlisted class: ${score.label}",
                confidence = score.confidence.coerceIn(0f, 1f),
                venomStatus = VenomStatus.VENOMOUS,
                dangerLevel = 3,
                uncertain = true,
                advice = "This class is not in the field guide. Do not assume it is harmless. $PHOTO_LIMIT"
            )
        }
        val caution = if (uncertain) {
            " Low confidence: compare the head, eyes, and scale pattern before you trust the name."
        } else {
            ""
        }
        return Prediction(
            kind = ResultKind.SPECIES,
            speciesId = species.id,
            scientificName = species.scientificName,
            commonName = species.commonName,
            confidence = score.confidence.coerceIn(0f, 1f),
            venomStatus = species.venomStatus,
            dangerLevel = species.dangerLevel,
            uncertain = uncertain,
            advice = species.firstAidAdvice + caution,
            lookAlikes = species.lookAlikes,
            symptoms = species.biteSymptoms
        )
    }
}
