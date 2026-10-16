package com.snakeid.domain

data class SamplePhoto(
    val id: String,
    val title: String
)

/** Real bundled photos. Synthetic color patterns are not offered as identifications. */
val samplePhotos: List<SamplePhoto> = listOf(
    SamplePhoto("naja_naja", "Spectacled Cobra"),
    SamplePhoto("daboia_russelii", "Russell's Viper"),
    SamplePhoto("bungarus_caeruleus", "Common Krait"),
    SamplePhoto("ptyas_mucosa", "Rat Snake"),
    SamplePhoto("ahaetulla_nasuta", "Green Vine"),
    SamplePhoto("non_snake", "Not a snake")
)
