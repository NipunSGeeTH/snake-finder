package com.snakeid.domain

enum class VenomStatus(val label: String, val badge: String) {
    DEADLY("Deadly", "DEADLY"),
    VENOMOUS("Venomous", "VENOMOUS"),
    MILDLY_VENOMOUS("Mildly venomous", "MILD VENOM"),
    NON_VENOMOUS("Non-venomous", "NON-VENOMOUS")
}

fun dangerStars(level: Int): String {
    val filled = level.coerceIn(0, 5)
    return "★".repeat(filled) + "☆".repeat(5 - filled)
}
