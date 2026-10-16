package com.snakeid.platform

import android.graphics.Bitmap
import android.util.Log
import com.snakeid.domain.Identification
import com.snakeid.domain.ModelCatalog
import com.snakeid.domain.ModelLabels
import com.snakeid.domain.Prediction
import com.snakeid.domain.ResultKind
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

actual class SnakeClassifier actual constructor() {
    private val interpreter: Interpreter?
    private val labels: List<String>
    private val inputBuffer: ByteBuffer?
    private val inputSize = 224
    private val store = ModelStore()
    private var wideModel: WideSnakeModel? = null
    private var activeModelId = ModelCatalog.QUICK
    private var closed = false

    init {
        val loaded = loadInterpreter()
        interpreter = loaded?.first
        labels = loaded?.second ?: emptyList()
        inputBuffer = if (interpreter != null) {
            ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3).order(ByteOrder.nativeOrder())
        } else {
            null
        }
    }

    actual fun classify(imageBytes: ByteArray): List<Prediction> = synchronized(this) {
        val active = interpreter
        val buffer = inputBuffer
        if (closed || active == null || buffer == null || labels.isEmpty()) {
            return listOf(Identification.unavailable("The on-device model is not loaded."))
        }
        val decoded = decodeOrientedBitmap(imageBytes, maxSide = 1024)
            ?: return listOf(Identification.unreadableImage())
        val scaled = if (decoded.width == inputSize && decoded.height == inputSize) {
            decoded
        } else {
            Bitmap.createScaledBitmap(decoded, inputSize, inputSize, true)
        }
        return try {
            val pixels = IntArray(inputSize * inputSize)
            scaled.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)
            val outputCount = active.getOutputTensor(0).numElements()
            if (outputCount != labels.size) {
                Identification.unavailable(
                    "The model has $outputCount outputs and the label file has ${labels.size} lines."
                ).let { listOf(it) }
            } else {
                val primary = infer(active, buffer, pixels, mobilenetScaling = true)
                val guardBitmap = letterbox(decoded, 127)
                val guardScaled = Bitmap.createScaledBitmap(guardBitmap, inputSize, inputSize, true)
                val guardPixels = IntArray(inputSize * inputSize)
                guardScaled.getPixels(guardPixels, 0, inputSize, 0, 0, inputSize, inputSize)
                if (guardScaled !== guardBitmap) guardScaled.recycle()
                guardBitmap.recycle()
                // Gray letterbox, pixels / 255. On the M21 this is the pass that
                // rejects the bundled flower. Stretch /255 does not.
                val secondary = infer(active, buffer, guardPixels, mobilenetScaling = false)
                val quick = Identification.fromScores(primary, secondary, labels)
                val wide = wideModel
                if (wide == null || activeModelId != ModelCatalog.WIDE || quick.first().kind == ResultKind.NO_SNAKE) {
                    quick
                } else {
                    wide.classify(pixels)
                }
            }
        } catch (error: Exception) {
            Log.e(TAG, "Inference failed", error)
            listOf(Identification.unavailable("The model failed while reading this photo."))
        } finally {
            if (scaled !== decoded) scaled.recycle()
            decoded.recycle()
        }
    }

    actual fun activate(modelId: String): String? = synchronized(this) {
        if (closed) return "The identifier is closed."
        if (modelId == ModelCatalog.QUICK) {
            wideModel?.close()
            wideModel = null
            activeModelId = ModelCatalog.QUICK
            store.rememberActive(ModelCatalog.QUICK)
            return null
        }
        val offer = ModelCatalog.byId(modelId) ?: return "That model is not in the catalog."
        if (!store.isReady(offer.id)) return "Download the wide catalog before choosing it."
        return try {
            val labelText = appContext.assets.open("models/snakesense_labels.tsv").bufferedReader().use { it.readText() }
            val loaded = WideSnakeModel(store.modelFile(offer.id), labelText)
            wideModel?.close()
            wideModel = loaded
            activeModelId = offer.id
            store.rememberActive(offer.id)
            null
        } catch (error: Exception) {
            Log.e(TAG, "Could not open the wide model", error)
            wideModel?.close()
            wideModel = null
            activeModelId = ModelCatalog.QUICK
            store.rememberActive(ModelCatalog.QUICK)
            "The wide model could not be opened. The quick model is still selected."
        }
    }

    actual fun close() {
        synchronized(this) {
            if (closed) return
            closed = true
            wideModel?.close()
            wideModel = null
            interpreter?.close()
        }
    }

    private fun letterbox(source: Bitmap, pad: Int): Bitmap {
        val side = maxOf(source.width, source.height)
        val canvas = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888)
        val draw = android.graphics.Canvas(canvas)
        draw.drawColor(android.graphics.Color.rgb(pad, pad, pad))
        draw.drawBitmap(source, (side - source.width) / 2f, (side - source.height) / 2f, null)
        return canvas
    }

    private fun infer(
        active: Interpreter,
        buffer: ByteBuffer,
        pixels: IntArray,
        mobilenetScaling: Boolean
    ): FloatArray {
        buffer.rewind()
        for (pixel in pixels) {
            val red = ((pixel shr 16) and 0xFF).toFloat()
            val green = ((pixel shr 8) and 0xFF).toFloat()
            val blue = (pixel and 0xFF).toFloat()
            if (mobilenetScaling) {
                buffer.putFloat((red - 127.5f) / 127.5f)
                buffer.putFloat((green - 127.5f) / 127.5f)
                buffer.putFloat((blue - 127.5f) / 127.5f)
            } else {
                buffer.putFloat(red / 255f)
                buffer.putFloat(green / 255f)
                buffer.putFloat(blue / 255f)
            }
        }
        buffer.rewind()
        val output = Array(1) { FloatArray(labels.size) }
        active.run(buffer, output)
        return output[0]
    }

    private fun loadInterpreter(): Pair<Interpreter, List<String>>? {
        if (!snakeIdReady()) {
            Log.e(TAG, "Application context was not initialized")
            return null
        }
        val labels = loadLabels()
        if (labels.isEmpty()) return null
        val model = mapModel() ?: return null
        return try {
            val options = Interpreter.Options().apply { setNumThreads(4) }
            Interpreter(model, options) to labels
        } catch (error: Exception) {
            Log.e(TAG, "Interpreter could not open the model", error)
            null
        }
    }

    private fun loadLabels(): List<String> {
        val fromAsset = try {
            appContext.assets.open("models/labels.txt").bufferedReader().useLines { lines ->
                lines.map { it.trim() }.filter { it.isNotEmpty() }.toList()
            }
        } catch (error: Exception) {
            Log.e(TAG, "Could not read models/labels.txt", error)
            emptyList()
        }
        return fromAsset.ifEmpty { ModelLabels.labels }
    }

    private fun mapModel(): ByteBuffer? {
        try {
            appContext.assets.openFd(MODEL_ASSET).use { descriptor ->
                FileInputStream(descriptor.fileDescriptor).channel.use { channel ->
                    return channel.map(
                        FileChannel.MapMode.READ_ONLY,
                        descriptor.startOffset,
                        descriptor.declaredLength
                    )
                }
            }
        } catch (error: Exception) {
            Log.w(TAG, "Could not map the model asset directly. Copying it into app storage.", error)
        }
        return try {
            val file = File(appContext.filesDir, "snake_model.tflite")
            if (!file.exists() || file.length() < 100_000) {
                appContext.assets.open(MODEL_ASSET).use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
            }
            FileInputStream(file).channel.use { channel ->
                channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length())
            }
        } catch (error: Exception) {
            Log.e(TAG, "Could not load models/snake_model.tflite", error)
            null
        }
    }

    private companion object {
        const val TAG = "SnakeID"
        const val MODEL_ASSET = "models/snake_model.tflite"
    }
}
