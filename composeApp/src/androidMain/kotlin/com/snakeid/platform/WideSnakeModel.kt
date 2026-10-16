package com.snakeid.platform

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.graphics.Bitmap
import com.snakeid.domain.Prediction
import com.snakeid.domain.WideLabel
import com.snakeid.domain.parseWideLabels
import com.snakeid.domain.widePredictions
import java.nio.FloatBuffer
import kotlin.math.exp

internal class WideSnakeModel(modelPath: String, labelText: String) : AutoCloseable {
    private val labels: List<WideLabel> = parseWideLabels(labelText)
    private val environment = OrtEnvironment.getEnvironment()
    private val session: OrtSession

    init {
        val options = OrtSession.SessionOptions().apply { setIntraOpNumThreads(4) }
        session = environment.createSession(modelPath, options)
        check(labels.isNotEmpty()) { "The wide label file is empty" }
    }

    fun classify(pixels: IntArray): List<Prediction> {
        val input = imagenetNchw(pixels)
        val tensor = OnnxTensor.createTensor(environment, FloatBuffer.wrap(input), longArrayOf(1, 3, 224, 224))
        tensor.use {
            session.run(mapOf("input" to tensor)).use { result ->
                val logits = result[0].value as Array<FloatArray>
                return widePredictions(softmax(logits[0]), labels)
            }
        }
    }

    override fun close() {
        session.close()
    }

    private fun imagenetNchw(pixels: IntArray): FloatArray {
        val plane = 224 * 224
        val data = FloatArray(3 * plane)
        val mean = floatArrayOf(0.485f, 0.456f, 0.406f)
        val std = floatArrayOf(0.229f, 0.224f, 0.225f)
        for (y in 0 until 224) {
            for (x in 0 until 224) {
                val pixel = pixels[y * 224 + x]
                val channels = intArrayOf((pixel shr 16) and 0xFF, (pixel shr 8) and 0xFF, pixel and 0xFF)
                val offset = y * 224 + x
                for (c in 0..2) {
                    data[c * plane + offset] = (channels[c] / 255f - mean[c]) / std[c]
                }
            }
        }
        return data
    }

    private fun softmax(logits: FloatArray): FloatArray {
        val max = logits.maxOrNull() ?: 0f
        var sum = 0.0
        val shifted = DoubleArray(logits.size) { index ->
            val value = exp((logits[index] - max).toDouble())
            sum += value
            value
        }
        return FloatArray(logits.size) { index -> (shifted[index] / sum).toFloat() }
    }
}
