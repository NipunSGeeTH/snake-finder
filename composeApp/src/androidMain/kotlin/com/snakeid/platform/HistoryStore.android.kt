package com.snakeid.platform

import android.util.Log
import com.snakeid.history.ScanRecord
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

actual class HistoryStore actual constructor() {
    private val json = Json { ignoreUnknownKeys = true }

    actual fun load(): List<ScanRecord> {
        val file = historyFile() ?: return emptyList()
        if (!file.exists() || file.length() == 0L) return emptyList()
        return try {
            json.decodeFromString(ListSerializer(ScanRecord.serializer()), file.readText())
        } catch (error: Exception) {
            Log.e(TAG, "Could not read scan history", error)
            emptyList()
        }
    }

    actual fun save(records: List<ScanRecord>) {
        val file = historyFile() ?: return
        try {
            file.parentFile?.mkdirs()
            val text = json.encodeToString(ListSerializer(ScanRecord.serializer()), records)
            file.writeText(text)
        } catch (error: Exception) {
            Log.e(TAG, "Could not save scan history", error)
        }
    }

    private fun historyFile(): File? {
        if (!snakeIdReady()) return null
        return File(appContext.filesDir, "scan_history.json")
    }

    private companion object {
        const val TAG = "SnakeID"
    }
}
