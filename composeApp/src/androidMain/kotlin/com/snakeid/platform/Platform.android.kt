package com.snakeid.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

lateinit var appContext: Context
    private set

fun initSnakeId(context: Context) {
    appContext = context.applicationContext
}

internal fun snakeIdReady(): Boolean = ::appContext.isInitialized

actual fun nowMillis(): Long = System.currentTimeMillis()

actual fun formatWhen(epochMs: Long): String {
    val format = SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault())
    return format.format(Date(epochMs))
}

actual fun dialPhone(number: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        appContext.startActivity(intent)
    } catch (error: Exception) {
        Toast.makeText(appContext, "No phone app available", Toast.LENGTH_SHORT).show()
    }
}

actual fun ByteArray.toImageBitmap(): ImageBitmap? {
    return decodeOrientedBitmap(this, maxSide = 1280)?.asImageBitmap()
}

actual fun loadAssetBytes(assetPath: String): ByteArray? {
    return try {
        appContext.assets.open(assetPath).use { it.readBytes() }
    } catch (error: Exception) {
        null
    }
}

actual fun loadBundledSample(sampleId: String): ByteArray? {
    val filename = when (sampleId) {
        "naja_naja" -> "samples/cobra.jpg"
        "daboia_russelii" -> "samples/russell_viper.jpg"
        "bungarus_caeruleus" -> "samples/krait.jpg"
        "ptyas_mucosa" -> "samples/rat_snake.jpg"
        "ahaetulla_nasuta" -> "samples/green_vine.jpg"
        "non_snake" -> "samples/non_snake.jpg"
        else -> return null
    }
    return try {
        appContext.assets.open(filename).use { it.readBytes() }
    } catch (error: Exception) {
        null
    }
}
