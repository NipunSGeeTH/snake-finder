package com.snakeid.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import com.snakeid.domain.Identification
import com.snakeid.domain.Prediction
import com.snakeid.history.ScanRecord
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual fun nowMillis(): Long = (NSDate().timeIntervalSince1970 * 1000.0).toLong()

actual fun formatWhen(epochMs: Long): String = epochMs.toString()

actual fun dialPhone(number: String) = Unit

actual fun ByteArray.toImageBitmap(): ImageBitmap? = null

actual fun loadBundledSample(sampleId: String): ByteArray? = null

@Composable
actual fun rememberImagePicker(onImagePicked: (ByteArray) -> Unit): ImagePickerLauncher {
    return remember {
        object : ImagePickerLauncher {
            override fun launchCamera() = Unit
            override fun launchGallery() = Unit
        }
    }
}

actual class SnakeClassifier actual constructor() {
    actual fun classify(imageBytes: ByteArray): List<Prediction> {
        return listOf(
            Identification.unavailable("On-device identification is implemented for Android in this build.")
        )
    }

    actual fun activate(modelId: String): String? = "Model choice is implemented for Android in this build."

    actual fun close() = Unit
}

actual class ModelStore actual constructor() {
    actual fun activeId(): String = "quick14"
    actual fun isReady(modelId: String): Boolean = modelId == "quick14"
    actual fun modelFile(modelId: String): String = ""
    actual suspend fun download(modelId: String, onProgress: (Int) -> Unit) = Unit
    actual fun delete(modelId: String) = Unit
}

actual fun bundledText(assetPath: String): String = ""

actual class HistoryStore actual constructor() {
    private val records = mutableListOf<ScanRecord>()

    actual fun load(): List<ScanRecord> = records.toList()

    actual fun save(records: List<ScanRecord>) {
        this.records.clear()
        this.records.addAll(records)
    }
}
