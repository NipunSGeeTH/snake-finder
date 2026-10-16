package com.snakeid.platform

import android.graphics.Bitmap
import android.util.Log
import com.snakeid.domain.Identification
import com.snakeid.domain.ModelCatalog
import com.snakeid.domain.ModelLabels
import com.snakeid.domain.Prediction
import com.snakeid.domain.ResultKind
import com.snakeid.domain.SnakeGate
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

actual class SnakeClassifier actual constructor() {
    private val interpreter: Interpreter?
    private val labels: List<String>
    private val gateInterpreter: Interpreter?
    private val gateLabels: List<String>
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
        val gate = loadGate()
        gateInterpreter = gate?.first
        gateLabels = gate?.second ?: emptyList()
        inputBuffer = if (interpreter != null) {
            ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3).order(ByteOrder.nativeOrder())
        } else {
            null
        }
    }

    actual fun classify(imageBytes: ByteArray): List<Prediction> = synchronized(this) {
        val active = interpreter
        val buffer = inputBuffer
        val gate = gateInterpreter
        if (closed || active == null || buffer == null || labels.isEmpty() || gate == null || gateLabels.isEmpty()) {
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
            val presence = SnakeGate.decide(
                infer(gate, buffer, pixels, mobilenetScaling = true, outputSize = gateLabels.size),
                gateLabels
            )
            Log.i(TAG, "snake gate mass=${presence.snakeMass} top=${presence.topLabel} ${presence.topScore}")
            if (!presence.isSnake) {
                return listOf(Identification.notASnake(presence.topLabel))
            }
            val outputCount = active.getOutputTensor(0).numElements()
            if (outputCount != labels.size) {
                Identification.unavailable(
                    "The model has $outputCount outputs and the label file has ${labels.size} lines."
                ).let { listOf(it) }
            } else {
                val primary = infer(active, buffer, pixels, mobilenetScaling = true, outputSize = labels.size)
                // The ImageNet gate already decided this is a snake. Do not let the
                // small model's "no snake" class overrule that and hide a real animal.
                val quick = Identification.fromScores(primary, primary, labels)
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
            gateInterpreter?.close()
            interpreter?.close()
        }
    }

    private fun loadGate(): Pair<Interpreter, List<String>>? {
        val labels = try {
            appContext.assets.open("models/imagenet_labels.txt").bufferedReader().useLines { lines ->
                lines.map { it.trim() }.filter { it.isNotEmpty() }.toList()
            }
        } catch (error: Exception) {
            Log.e(TAG, "Could not read the snake-or-not labels", error)
            return null
        }
        val model = mapAsset("models/snake_gate.tflite") ?: return null
        return try {
            Interpreter(model, Interpreter.Options().apply { setNumThreads(4) }) to labels
        } catch (error: Exception) {
            Log.e(TAG, "Could not open the snake-or-not model", error)
            null
        }
    }

    private fun infer(
        active: Interpreter,
        buffer: ByteBuffer,
        pixels: IntArray,
        mobilenetScaling: Boolean,
        outputSize: Int
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
        val output = Array(1) { FloatArray(outputSize) }
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
        val model = mapAsset(MODEL_ASSET) ?: return null
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

    private fun mapAsset(assetPath: String): ByteBuffer? {
        try {
            appContext.assets.openFd(assetPath).use { descriptor ->
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
            val file = File(appContext.filesDir, assetPath.substringAfterLast('/'))
            if (!file.exists() || file.length() < 100_000) {
                appContext.assets.open(assetPath).use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
            }
            FileInputStream(file).channel.use { channel ->
                channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length())
            }
        } catch (error: Exception) {
            Log.e(TAG, "Could not load $assetPath", error)
            null
        }
    }

    private companion object {
        const val TAG = "SnakeID"
        const val MODEL_ASSET = "models/snake_model.tflite"
    }
}
