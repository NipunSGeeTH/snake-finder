package com.snakeid.domain

enum class ResultKind {
    SPECIES,
    NO_SNAKE,
    UNRECOGNIZED,
    UNAVAILABLE
}

data class Prediction(
    val kind: ResultKind,
    val speciesId: String?,
    val scientificName: String,
    val commonName: String,
    val confidence: Float,
    val venomStatus: VenomStatus,
    val dangerLevel: Int,
    val uncertain: Boolean,
    val advice: String,
    val lookAlikes: String = "",
    val symptoms: String = ""
) {
    val isVenomous: Boolean
        get() = kind == ResultKind.SPECIES && venomStatus != VenomStatus.NON_VENOMOUS
}

data class ClassScore(val label: String, val confidence: Float)
