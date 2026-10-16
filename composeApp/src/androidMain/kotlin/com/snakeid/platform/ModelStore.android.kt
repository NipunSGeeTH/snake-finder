package com.snakeid.platform

import android.util.Log
import com.snakeid.domain.ModelCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

actual class ModelStore actual constructor() {
    actual fun activeId(): String {
        val saved = activeFile().takeIf { it.exists() }?.readText()?.trim().orEmpty()
        val offer = ModelCatalog.byId(saved)
        return if (offer != null && isReady(offer.id)) offer.id else ModelCatalog.QUICK
    }

    actual fun isReady(modelId: String): Boolean {
        val offer = ModelCatalog.byId(modelId) ?: return false
        if (offer.builtIn) return true
        val file = File(modelFile(modelId))
        return file.exists() && file.length() == offer.sizeBytes
    }

    actual fun modelFile(modelId: String): String {
        return File(modelsDir(), "$modelId.onnx").absolutePath
    }

    actual suspend fun download(modelId: String, onProgress: (Int) -> Unit) {
        val offer = ModelCatalog.byId(modelId) ?: error("Unknown model")
        if (offer.builtIn) return
        if (!offer.downloadUrl.startsWith("https://")) error("The model address is not a secure download")
        withContext(Dispatchers.IO) {
            val destination = File(modelFile(modelId))
            val partial = File(destination.parentFile, destination.name + ".part")
            destination.parentFile?.mkdirs()
            if (destination.exists() && destination.length() == offer.sizeBytes) {
                onProgress(100)
                return@withContext
            }
            val connection = open(offer.downloadUrl)
            try {
                val expected = connection.contentLengthLong
                if (expected > 0 && expected != offer.sizeBytes) {
                    error("The download size does not match the published model")
                }
                connection.inputStream.use { input ->
                    FileOutputStream(partial).use { output ->
                        val buffer = ByteArray(64 * 1024)
                        var readTotal = 0L
                        while (true) {
                            val count = input.read(buffer)
                            if (count < 0) break
                            output.write(buffer, 0, count)
                            readTotal += count
                            val percent = if (offer.sizeBytes > 0) {
                                ((readTotal * 100) / offer.sizeBytes).toInt().coerceIn(0, 99)
                            } else {
                                0
                            }
                            onProgress(percent)
                        }
                    }
                }
            } finally {
                connection.disconnect()
            }
            if (partial.length() != offer.sizeBytes) {
                partial.delete()
                error("The download stopped early. The model was not saved.")
            }
            if (destination.exists()) destination.delete()
            if (!partial.renameTo(destination)) {
                partial.copyTo(destination, overwrite = true)
                partial.delete()
            }
            onProgress(100)
        }
    }

    actual fun delete(modelId: String) {
        val offer = ModelCatalog.byId(modelId) ?: return
        if (offer.builtIn) return
        File(modelFile(modelId)).delete()
        File(modelFile(modelId) + ".part").delete()
        if (activeId() == modelId) {
            activeFile().writeText(ModelCatalog.QUICK)
        }
    }

    fun rememberActive(modelId: String) {
        activeFile().parentFile?.mkdirs()
        activeFile().writeText(modelId)
    }

    private fun open(address: String): HttpURLConnection {
        val connection = URL(address).openConnection() as HttpURLConnection
        connection.instanceFollowRedirects = true
        connection.connectTimeout = 30_000
        connection.readTimeout = 120_000
        connection.setRequestProperty("User-Agent", "SnakeID")
        connection.connect()
        val code = connection.responseCode
        if (code !in 200..299) {
            connection.disconnect()
            error("The model download returned HTTP $code")
        }
        return connection
    }

    private fun modelsDir(): File {
        val dir = File(appContext.filesDir, "models")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun activeFile(): File = File(appContext.filesDir, "active_model.txt")

    private companion object {
        const val TAG = "SnakeID"
    }
}

actual fun bundledText(assetPath: String): String {
    return try {
        appContext.assets.open(assetPath).bufferedReader().use { it.readText() }
    } catch (error: Exception) {
        Log.e("SnakeID", "Could not read $assetPath", error)
        ""
    }
}
