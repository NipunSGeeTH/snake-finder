package com.snakeid.domain

/**
 * Class order of the bundled MobileNetV2 model in assets/models/labels.txt.
 * Names are matched exactly after light normalization. There is no fuzzy
 * "contains" match, because that can attach a deadly dossier to the wrong snake.
 */
object ModelLabels {
    const val NO_SNAKE = "No snake found"
    const val UNKNOWN = "Unknown Snake"

    val labels: List<String> = listOf(
        "Common Krait",
        "King Cobra",
        "Monocled Cobra",
        "Russell's Viper",
        "Saw-scaled Viper",
        "Spectacled Cobra",
        "Banded Racer",
        "Checkered Keelback",
        "Common Rat Snake",
        "Common Sand Boa",
        "Common Trinket",
        "Green Tree Vine",
        UNKNOWN,
        NO_SNAKE
    )

    private val speciesIdByLabel: Map<String, String> = mapOf(
        "common krait" to "bungarus_caeruleus",
        "king cobra" to "ophiophagus_hannah",
        "monocled cobra" to "naja_kaouthia",
        "russell's viper" to "daboia_russelii",
        "russells viper" to "daboia_russelii",
        "saw-scaled viper" to "echis_carinatus",
        "saw scaled viper" to "echis_carinatus",
        "spectacled cobra" to "naja_naja",
        "indian spectacled cobra" to "naja_naja",
        "banded racer" to "argyrogena_fasciolata",
        "checkered keelback" to "fowlea_piscator",
        "common rat snake" to "ptyas_mucosa",
        "oriental rat snake" to "ptyas_mucosa",
        "common sand boa" to "eryx_conicus",
        "rough-scaled sand boa" to "eryx_conicus",
        "rough scaled sand boa" to "eryx_conicus",
        "common trinket" to "coelognathus_helena",
        "trinket snake" to "coelognathus_helena",
        "green tree vine" to "ahaetulla_nasuta",
        "green vine snake" to "ahaetulla_nasuta",
        "green vine snake / long-nosed whip snake" to "ahaetulla_nasuta",
        "long-nosed whip snake" to "ahaetulla_nasuta",
        "long nosed whip snake" to "ahaetulla_nasuta"
    )

    fun normalize(raw: String): String {
        return raw.trim()
            .lowercase()
            .replace(Regex("^\\d+[\\s:._-]*"), "")
            .replace('_', ' ')
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun speciesIdFor(label: String): String? {
        val normalized = normalize(label)
        return speciesIdByLabel[normalized]
            ?: speciesIdByLabel[normalized.replace("-", " ")]
    }

    fun isNoSnake(label: String): Boolean = normalize(label) == normalize(NO_SNAKE)

    fun isUnknown(label: String): Boolean = normalize(label) == normalize(UNKNOWN)
}
