package com.snakeid.domain

import com.snakeid.platform.TextCache
import com.snakeid.platform.fetchText
import kotlinx.serialization.json.Json

class ArticleClient(private val cache: TextCache = TextCache()) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun index(): List<ArticleMeta> {
        var lastError: Exception? = null
        var withoutImages: List<ArticleMeta>? = null
        for (root in roots) {
            try {
                val text = fetchText("$root/articles/index.json")
                if (!text.trimStart().startsWith("{")) continue
                val articles = json.decodeFromString(ArticleIndex.serializer(), text).articles
                if (articles.isEmpty() || articles.any { it.image.isNotBlank() }) {
                    cache.write("article-index.json", text)
                    return articles
                }
                withoutImages = articles
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                lastError = error
            }
        }
        withoutImages?.let { return it }
        cache.read("article-index.json")?.let { cached ->
            val saved = runCatching { json.decodeFromString(ArticleIndex.serializer(), cached).articles }.getOrNull()
            if (saved != null) return saved
        }
        throw ArticleOffline().also { if (lastError != null) it.initCause(lastError) }
    }

    fun imageUrl(image: String): String? {
        if (image.isBlank()) return null
        return "${roots.first()}/articles/$image"
    }

    suspend fun imageBytes(image: String): ByteArray? {
        val path = image.trim().trimStart('/')
        if (path.isEmpty() || path.contains("..")) return null
        val cached = cache.readBytes("img-$path")
        if (cached != null && cached.isNotEmpty()) return cached
        for (root in roots) {
            try {
                val bytes = com.snakeid.platform.fetchBytes("$root/articles/$path")
                if (bytes.isNotEmpty()) {
                    cache.writeBytes("img-$path", bytes)
                    return bytes
                }
            } catch (_: Exception) {
            }
        }
        return null
    }

    suspend fun body(slug: String): String {
        if (!slug.matches(Regex("[a-z0-9-]+"))) error("Unknown article")
        return load("articles/$slug.md", "article-$slug.md")
    }

    private suspend fun load(path: String, cacheKey: String): String {
        var lastError: Exception? = null
        for (root in roots) {
            try {
                val text = fetchText("$root/$path")
                if (text.isNotBlank() && !text.trimStart().startsWith("<")) {
                    cache.write(cacheKey, text)
                    return text
                }
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                lastError = error
            }
        }
        cache.read(cacheKey)?.let { return it }
        throw ArticleOffline().also { if (lastError != null) it.initCause(lastError) }
    }

    private companion object {
        val roots = listOf(
            "https://nipunsgeeth.github.io/ophia-articles",
            "https://raw.githubusercontent.com/NipunSGeeTH/ophia-articles/main"
        )
    }
}
