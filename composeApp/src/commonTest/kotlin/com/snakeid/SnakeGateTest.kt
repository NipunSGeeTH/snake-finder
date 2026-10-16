package com.snakeid

import com.snakeid.domain.SnakeGate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SnakeGateTest {
    private val labels = listOf("cellular telephone", "Indian cobra", "pinwheel")

    @Test
    fun aPhoneIsNotASnake() {
        val decision = SnakeGate.decide(floatArrayOf(0.89f, 0.001f, 0.05f), labels)
        assertFalse(decision.isSnake)
        assertTrue(decision.topLabel == "cellular telephone")
    }

    @Test
    fun aCobraPassesTheGate() {
        val decision = SnakeGate.decide(floatArrayOf(0.01f, 0.97f, 0.01f), labels)
        assertTrue(decision.isSnake)
    }
}
