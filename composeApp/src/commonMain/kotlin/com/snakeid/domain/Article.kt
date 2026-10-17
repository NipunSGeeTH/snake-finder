package com.snakeid.domain

import kotlinx.serialization.Serializable

@Serializable
data class ArticleMeta(
    val slug: String,
    val title: String,
    val summary: String,
    val updated: String,
    val author: String = "",
    val image: String = ""
)

@Serializable
data class ArticleIndex(
    val articles: List<ArticleMeta> = emptyList()
)

data class ArticleBlock(val kind: Kind, val text: String) {
    enum class Kind { TITLE, SUBTITLE, HEADING, PARAGRAPH, BULLET, NUMBER, QUOTE }
}

class ArticleOffline : Exception("Connect to the internet, then try again.")

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
        val number = NUMBERED.matchEntire(line)
        when {
            line.isEmpty() -> flushParagraph()
            line == "---" || line == "***" -> flushParagraph()
            line.startsWith("### ") -> {
                flushParagraph()
                blocks += ArticleBlock(ArticleBlock.Kind.HEADING, line.removePrefix("### "))
            }
            line.startsWith("## ") -> {
                flushParagraph()
                blocks += ArticleBlock(ArticleBlock.Kind.SUBTITLE, line.removePrefix("## "))
            }
            line.startsWith("# ") -> {
                flushParagraph()
                blocks += ArticleBlock(ArticleBlock.Kind.TITLE, line.removePrefix("# "))
            }
            line.startsWith("> ") -> {
                flushParagraph()
                blocks += ArticleBlock(ArticleBlock.Kind.QUOTE, line.removePrefix("> "))
            }
            line.startsWith("- ") || (line.startsWith("* ") && !line.startsWith("**")) -> {
                flushParagraph()
                blocks += ArticleBlock(ArticleBlock.Kind.BULLET, line.drop(2))
            }
            number != null -> {
                flushParagraph()
                blocks += ArticleBlock(ArticleBlock.Kind.NUMBER, "${number.groupValues[1]}. ${number.groupValues[2]}")
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

private val NUMBERED = Regex("""^(\d+)\.\s+(.+)$""")
