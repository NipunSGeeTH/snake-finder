package com.snakeid.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import com.snakeid.domain.Prediction
import com.snakeid.history.ScanRecord

interface ImagePickerLauncher {
    fun launchCamera()
    fun launchGallery()
}

@Composable
expect fun rememberImagePicker(onImagePicked: (ByteArray) -> Unit): ImagePickerLauncher

expect class SnakeClassifier() {
    fun classify(imageBytes: ByteArray): List<Prediction>
    fun close()
}

expect class HistoryStore() {
    fun load(): List<ScanRecord>
    fun save(records: List<ScanRecord>)
}

expect fun ByteArray.toImageBitmap(): ImageBitmap?

expect fun loadBundledSample(sampleId: String): ByteArray?

expect fun nowMillis(): Long

expect fun formatWhen(epochMs: Long): String

expect fun dialPhone(number: String)
