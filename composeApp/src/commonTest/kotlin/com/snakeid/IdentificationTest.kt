package com.snakeid

import com.snakeid.domain.Identification
import com.snakeid.domain.ModelLabels
import com.snakeid.domain.ResultKind
import com.snakeid.domain.SnakeSpeciesDatabase
import com.snakeid.domain.VenomStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class IdentificationTest {
    private val labels = ModelLabels.labels

    @Test
    fun nonSnakeGuardOverridesAConfidentFalseSpecies() {
        val primary = FloatArray(labels.size)
        val secondary = FloatArray(labels.size)
        primary[labels.indexOf("Russell's Viper")] = 0.99f
        secondary[labels.indexOf(ModelLabels.NO_SNAKE)] = 0.55f
        val result = Identification.fromScores(primary, secondary, labels)
        assertEquals(1, result.size)
        assertEquals(ResultKind.NO_SNAKE, result.first().kind)
        assertFalse(result.first().isVenomous)
    }

    @Test
    fun spectacledCobraMapsToTheDeadlySpecies() {
        val scores = peaked("Spectacled Cobra", 0.97f)
        val result = Identification.fromScores(scores, scores, labels).first()
        assertEquals(ResultKind.SPECIES, result.kind)
        assertEquals("naja_naja", result.speciesId)
        assertEquals(VenomStatus.DEADLY, result.venomStatus)
        assertFalse(result.uncertain)
    }

    @Test
    fun lowConfidenceStaysUncertainAndKeepsThreeScores() {
        val primary = FloatArray(labels.size)
        primary[labels.indexOf("Monocled Cobra")] = 0.43f
        primary[labels.indexOf("Spectacled Cobra")] = 0.28f
        primary[labels.indexOf("King Cobra")] = 0.25f
        val secondary = peaked("Monocled Cobra", 0.40f)
        val result = Identification.fromScores(primary, secondary, labels)
        assertEquals(3, result.size)
        assertTrue(result.first().uncertain)
        assertEquals("naja_kaouthia", result.first().speciesId)
    }

    @Test
    fun ratSnakeLabelIsNonVenomous() {
        val scores = peaked("Common Rat Snake", 0.86f)
        val result = Identification.fromScores(scores, scores, labels).first()
        assertEquals("ptyas_mucosa", result.speciesId)
        assertEquals(VenomStatus.NON_VENOMOUS, result.venomStatus)
        assertFalse(result.isVenomous)
    }

    @Test
    fun unknownClassIsNotDescribedAsMildlyVenomous() {
        val scores = peaked(ModelLabels.UNKNOWN, 0.80f)
        val result = Identification.fromScores(scores, scores, labels).first()
        assertEquals(ResultKind.UNRECOGNIZED, result.kind)
        assertTrue(result.uncertain)
    }

    @Test
    fun unmatchedLabelIsNotForcedOntoAnotherSnake() {
        assertNull(SnakeSpeciesDatabase.findSpecies("cobra"))
        assertEquals("naja_naja", SnakeSpeciesDatabase.findSpecies("Spectacled Cobra")?.id)
        assertEquals("ptyas_mucosa", SnakeSpeciesDatabase.findSpecies("Common Rat Snake")?.id)
        assertEquals("Argyrogena fasciolata", SnakeSpeciesDatabase.findSpecies("Banded Racer")?.scientificName)
    }

    @Test
    fun mismatchedOutputIsUnavailable() {
        val result = Identification.fromScores(floatArrayOf(1f), floatArrayOf(1f), labels)
        assertEquals(ResultKind.UNAVAILABLE, result.first().kind)
    }

    private fun peaked(label: String, confidence: Float): FloatArray {
        val scores = FloatArray(labels.size)
        scores[labels.indexOf(label)] = confidence
        return scores
    }
}
