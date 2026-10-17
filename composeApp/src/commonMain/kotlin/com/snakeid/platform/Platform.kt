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
    fun activate(modelId: String): String?
    fun close()
}

expect class ModelStore() {
    fun activeId(): String
    fun isReady(modelId: String): Boolean
    fun modelFile(modelId: String): String
    suspend fun download(modelId: String, onProgress: (Int) -> Unit)
    fun delete(modelId: String)
}

expect fun bundledText(assetPath: String): String

expect class HistoryStore() {
    fun load(): List<ScanRecord>
    fun save(records: List<ScanRecord>)
}

expect fun ByteArray.toImageBitmap(): ImageBitmap?

expect fun loadBundledSample(sampleId: String): ByteArray?

expect fun loadAssetBytes(assetPath: String): ByteArray?

expect fun nowMillis(): Long

expect fun formatWhen(epochMs: Long): String

expect fun dialPhone(number: String)

expect suspend fun fetchText(url: String): String

expect suspend fun fetchBytes(url: String): ByteArray

expect class TextCache() {
    fun read(key: String): String?
    fun write(key: String, text: String)
    fun readBytes(key: String): ByteArray?
    fun writeBytes(key: String, bytes: ByteArray)
}
