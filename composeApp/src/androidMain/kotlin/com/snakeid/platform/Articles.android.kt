package com.snakeid.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

actual suspend fun fetchBytes(url: String): ByteArray = withContext(Dispatchers.IO) {
    val connection = open(url)
    try {
        connection.inputStream.use { it.readBytes() }
    } finally {
        connection.disconnect()
    }
}

actual suspend fun fetchText(url: String): String = withContext(Dispatchers.IO) {
    val connection = open(url)
    try {
        connection.inputStream.bufferedReader().use { it.readText() }
    } finally {
        connection.disconnect()
    }
}

private fun open(url: String): HttpURLConnection {
    val connection = URL(url).openConnection() as HttpURLConnection
    connection.instanceFollowRedirects = true
    connection.connectTimeout = 20_000
    connection.readTimeout = 20_000
    connection.setRequestProperty("User-Agent", "Ophia")
    connection.setRequestProperty("Accept", "application/json, text/markdown, text/plain, image/*")
    try {
        connection.connect()
        if (connection.responseCode !in 200..299) {
            connection.disconnect()
            throw IOException("The article server did not answer")
        }
    } catch (failure: IOException) {
        connection.disconnect()
        throw failure
    }
    return connection
}

actual class TextCache actual constructor() {
    private val dir: File
        get() {
            val folder = File(appContext.filesDir, "articles")
            if (!folder.exists()) folder.mkdirs()
            return folder
        }

    actual fun read(key: String): String? {
        val file = File(dir, safe(key))
        return if (file.exists()) file.readText() else null
    }

    actual fun write(key: String, text: String) {
        File(dir, safe(key)).writeText(text)
    }

    actual fun readBytes(key: String): ByteArray? {
        val file = File(dir, safe(key))
        return if (file.exists()) file.readBytes() else null
    }

    actual fun writeBytes(key: String, bytes: ByteArray) {
        File(dir, safe(key)).writeBytes(bytes)
    }

    private fun safe(key: String) = key.replace('/', '_')
}
