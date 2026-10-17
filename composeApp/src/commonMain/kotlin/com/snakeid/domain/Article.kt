package com.snakeid.domain

import kotlinx.serialization.Serializable

@Serializable
data class ArticleMeta(
    val slug: String,
    val title: String,
    val summary: String,
    val updated: String
)

@Serializable
data class ArticleIndex(
    val articles: List<ArticleMeta> = emptyList()
)

data class ArticleBlock(val kind: Kind, val text: String) {
    enum class Kind { HEADING, PARAGRAPH, BULLET }
}

fun parseArticle(markdown: String): List<ArticleBlock> {
    val blocks = mutableListOf<ArticleBlock>()
    val paragraph = StringBuilder()
    fun flushParagraph() {
        val text = paragraph.toString().trim()
        if (text.isNotEmpty()) blocks += ArticleBlock(ArticleBlock.Kind.PARAGRAPH, text)
        paragraph.clear()
    }
    markdown.lineSequence().forEach { raw ->
        val line = raw.trim()
        when {
            line.isEmpty() -> flushParagraph()
            line.startsWith("### ") -> {
                flushParagraph()
                blocks += ArticleBlock(ArticleBlock.Kind.HEADING, line.removePrefix("### "))
            }
            line.startsWith("## ") -> {
                flushParagraph()
                blocks += ArticleBlock(ArticleBlock.Kind.HEADING, line.removePrefix("## "))
            }
            line.startsWith("# ") -> {
                flushParagraph()
                blocks += ArticleBlock(ArticleBlock.Kind.HEADING, line.removePrefix("# "))
            }
            line.startsWith("- ") -> {
                flushParagraph()
                blocks += ArticleBlock(ArticleBlock.Kind.BULLET, line.removePrefix("- "))
            }
            else -> {
                if (paragraph.isNotEmpty()) paragraph.append(' ')
                paragraph.append(line)
            }
        }
    }
    flushParagraph()
    return blocks
}
