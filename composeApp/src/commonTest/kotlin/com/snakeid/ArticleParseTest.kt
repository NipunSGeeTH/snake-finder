package com.snakeid

import com.snakeid.domain.ArticleBlock
import com.snakeid.domain.parseArticle
import kotlin.test.Test
import kotlin.test.assertEquals

class ArticleParseTest {
    @Test
    fun titlesSubtitlesBulletsAndEmphasisStaySeparate() {
        val blocks = parseArticle(
            """
            # How to photograph a snake

            Stand back. Use **distance**.

            ## What to include

            - The whole snake
            - A *second* photo of the head

            ### At night

            1. Keep the light brief.
            > Stay back.
            """.trimIndent()
        )
        assertEquals(ArticleBlock.Kind.TITLE, blocks[0].kind)
        assertEquals("How to photograph a snake", blocks[0].text)
        assertEquals(ArticleBlock.Kind.PARAGRAPH, blocks[1].kind)
        assertEquals("Stand back. Use **distance**.", blocks[1].text)
        assertEquals(ArticleBlock.Kind.SUBTITLE, blocks[2].kind)
        assertEquals("What to include", blocks[2].text)
        assertEquals(ArticleBlock.Kind.BULLET, blocks[3].kind)
        assertEquals(ArticleBlock.Kind.BULLET, blocks[4].kind)
        assertEquals("A *second* photo of the head", blocks[4].text)
        assertEquals(ArticleBlock.Kind.HEADING, blocks[5].kind)
        assertEquals(ArticleBlock.Kind.NUMBER, blocks[6].kind)
        assertEquals("1. Keep the light brief.", blocks[6].text)
        assertEquals(ArticleBlock.Kind.QUOTE, blocks[7].kind)
        assertEquals("Stay back.", blocks[7].text)
    }
}
