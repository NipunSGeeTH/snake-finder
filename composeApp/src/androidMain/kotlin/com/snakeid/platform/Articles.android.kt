package com.snakeid.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

actual suspend fun fetchText(url: String): String = withContext(Dispatchers.IO) {
    val connection = URL(url).openConnection() as HttpURLConnection
    connection.instanceFollowRedirects = true
    connection.connectTimeout = 20_000
    connection.readTimeout = 20_000
    connection.setRequestProperty("User-Agent", "SnakeID")
    connection.setRequestProperty("Accept", "application/json, text/markdown, text/plain")
    try {
        connection.connect()
        if (connection.responseCode !in 200..299) {
            error("Article request returned HTTP ${connection.responseCode}")
        }
        connection.inputStream.bufferedReader().use { it.readText() }
    } finally {
        connection.disconnect()
    }
}

actual class TextCache actual constructor() {
    private val dir: File
        get() {
            val folder = File(appContext.filesDir, "articles")
            if (!folder.exists()) folder.mkdirs()
            return folder
        }

    actual fun read(key: String): String? {
        val file = File(dir, key)
        return if (file.exists()) file.readText() else null
    }

    actual fun write(key: String, text: String) {
        File(dir, key).writeText(text)
    }
}
