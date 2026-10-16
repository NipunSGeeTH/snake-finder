package com.snakeid

import com.snakeid.domain.ModelCatalog
import com.snakeid.domain.ResultKind
import com.snakeid.domain.VenomStatus
import com.snakeid.domain.parseWideLabels
import com.snakeid.domain.widePredictions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WideModelTest {
    @Test
    fun catalogKeepsTheQuickModelOfflineAndTheWideModelOnHttps() {
        val quick = ModelCatalog.byId(ModelCatalog.QUICK)!!
        val wide = ModelCatalog.byId(ModelCatalog.WIDE)!!
        assertTrue(quick.builtIn)
        assertTrue(wide.downloadUrl.startsWith("https://github.com/NipunSGeeTH/snake-finder/releases/download/models/"))
        assertEquals(178_576_161L, wide.sizeBytes)
        assertEquals(135, wide.speciesCount)
    }

    @Test
    fun wideLabelMapsIndianCobraToTheFieldGuide() {
        val labels = parseWideLabels("0\tNaja naja\tIndian Cobra\t1\n")
        val result = widePredictions(floatArrayOf(0.91f), labels).first()
        assertEquals(ResultKind.SPECIES, result.kind)
        assertEquals("naja_naja", result.speciesId)
        assertEquals(VenomStatus.DEADLY, result.venomStatus)
    }
}
