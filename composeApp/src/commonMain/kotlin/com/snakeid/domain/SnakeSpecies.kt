package com.snakeid.domain

data class SnakeSpecies(
    val id: String,
    val scientificName: String,
    val commonName: String,
    val localNames: String = "",
    val venomStatus: VenomStatus,
    val dangerLevel: Int,
    val identifyingFeatures: List<String>,
    val habitat: String,
    val behavior: String,
    val biteSymptoms: String,
    val firstAidAdvice: String,
    val lookAlikes: String = ""
)
