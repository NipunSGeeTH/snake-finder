package com.snakeid.domain

import com.snakeid.platform.TextCache
import com.snakeid.platform.fetchText
import kotlinx.serialization.json.Json

class ArticleClient(private val cache: TextCache = TextCache()) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun index(): List<ArticleMeta> {
        val text = load("articles/index.json", "article-index.json")
        return json.decodeFromString(ArticleIndex.serializer(), text).articles
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
                if (text.isNotBlank()) {
                    cache.write(cacheKey, text)
                    return text
                }
            } catch (error: Exception) {
                lastError = error
            }
        }
        cache.read(cacheKey)?.let { return it }
        throw lastError ?: IllegalStateException("The article list could not be loaded.")
    }

    private companion object {
        val roots = listOf(
            "https://nipunsgeeth.github.io/snake-finder",
            "https://raw.githubusercontent.com/NipunSGeeTH/snake-finder/main/docs"
        )
    }
}
