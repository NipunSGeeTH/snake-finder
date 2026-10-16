package com.snakeid.domain

data class GateDecision(
    val isSnake: Boolean,
    val snakeMass: Float,
    val topLabel: String,
    val topScore: Float
)

/**
 * First stage, before any species name. These are the snake classes in the
 * ImageNet MobileNet label list. A photo of a phone or a flower puts almost
 * no probability on them.
 */
object SnakeGate {
    const val MIN_SNAKE_MASS = 0.15f

    val snakeNames: Set<String> = setOf(
        "thunder snake",
        "ringneck snake",
        "hognose snake",
        "green snake",
        "king snake",
        "garter snake",
        "water snake",
        "vine snake",
        "night snake",
        "boa constrictor",
        "rock python",
        "Indian cobra",
        "green mamba",
        "sea snake",
        "horned viper",
        "diamondback",
        "sidewinder"
    )

    fun decide(scores: FloatArray, labels: List<String>): GateDecision {
        if (scores.isEmpty() || labels.size != scores.size) {
            return GateDecision(false, 0f, "", 0f)
        }
        var topIndex = 0
        var topScore = scores[0]
        var snakeMass = 0f
        for (index in scores.indices) {
            val score = scores[index]
            if (score > topScore) {
                topScore = score
                topIndex = index
            }
            if (labels[index] in snakeNames) snakeMass += score
        }
        return GateDecision(
            isSnake = snakeMass >= MIN_SNAKE_MASS,
            snakeMass = snakeMass,
            topLabel = labels[topIndex],
            topScore = topScore
        )
    }
}
