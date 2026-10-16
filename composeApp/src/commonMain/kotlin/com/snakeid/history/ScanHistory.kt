package com.snakeid.history

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.snakeid.domain.Prediction
import com.snakeid.domain.ResultKind
import com.snakeid.platform.HistoryStore
import com.snakeid.platform.nowMillis
import kotlinx.serialization.Serializable

@Serializable
data class ScanRecord(
    val id: String,
    val epochMs: Long,
    val note: String,
    val scientificName: String,
    val commonName: String,
    val confidence: Float,
    val venomStatusName: String,
    val dangerLevel: Int,
    val kindName: String,
    val speciesId: String? = null
)

class ScanHistory(private val store: HistoryStore = HistoryStore()) {
    var records by mutableStateOf(safeLoad())
        private set

    fun add(prediction: Prediction, note: String) {
        if (prediction.kind == ResultKind.UNAVAILABLE) return
        val epochMs = nowMillis()
        val record = ScanRecord(
            id = "$epochMs-${records.size}",
            epochMs = epochMs,
            note = note,
            scientificName = prediction.scientificName,
            commonName = prediction.commonName,
            confidence = prediction.confidence,
            venomStatusName = prediction.venomStatus.name,
            dangerLevel = prediction.dangerLevel,
            kindName = prediction.kind.name,
            speciesId = prediction.speciesId
        )
        records = (listOf(record) + records).take(MAX_RECORDS)
        store.save(records)
    }

    fun clear() {
        records = emptyList()
        store.save(emptyList())
    }

    private fun safeLoad(): List<ScanRecord> {
        return try {
            store.load().take(MAX_RECORDS)
        } catch (error: Exception) {
            emptyList()
        }
    }

    private companion object {
        const val MAX_RECORDS = 50
    }
}
