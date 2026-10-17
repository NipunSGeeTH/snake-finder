package com.snakeid.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snakeid.domain.ArticleBlock
import com.snakeid.domain.ArticleClient
import com.snakeid.domain.ArticleMeta
import com.snakeid.domain.parseArticle
import com.snakeid.platform.PlatformBackHandler
import com.snakeid.platform.toImageBitmap
import kotlinx.coroutines.launch

@Composable
fun ArticlesScreen() {
    val client = remember { ArticleClient() }
    val scope = rememberCoroutineScope()
    var articles by remember { mutableStateOf<List<ArticleMeta>>(emptyList()) }
    var selected by remember { mutableStateOf<ArticleMeta?>(null) }
    var body by remember { mutableStateOf<List<ArticleBlock>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var needsInternet by remember { mutableStateOf(false) }

    fun refresh() {
        loading = true
        needsInternet = false
        scope.launch {
            val result = runCatching { client.index() }
            loading = false
            result.onSuccess {
                articles = it
                needsInternet = false
            }
            result.onFailure {
                if (articles.isEmpty()) needsInternet = true
            }
        }
    }

    val listState = rememberLazyListState()

    fun closeArticle() {
        selected = null
        body = emptyList()
        needsInternet = false
    }

    fun openArticle(article: ArticleMeta) {
        selected = article
        loading = true
        needsInternet = false
        body = emptyList()
        scope.launch {
            val result = runCatching { parseArticle(client.body(article.slug)) }
            if (selected?.slug != article.slug) return@launch
            loading = false
            result.onSuccess { body = it }
            result.onFailure { needsInternet = true }
        }
    }

    LaunchedEffect(Unit) { refresh() }
    PlatformBackHandler(enabled = selected != null, onBack = { closeArticle() })

    AnimatedContent(
        targetState = selected,
        transitionSpec = {
            val opening = targetState != null
            val enter = fadeIn(appleFade()) + slideInHorizontally(appleSpring()) { width ->
                if (opening) width / 5 else -width / 5
            }
            val exit = fadeOut(appleFade()) + slideOutHorizontally(appleFade()) { width ->
                if (opening) -width / 8 else width / 8
            }
            enter togetherWith exit
        },
        label = "article"
    ) { current ->
        if (current != null) {
            ArticleReader(
                title = current.title,
                updated = current.updated,
                author = current.author,
                image = current.image,
                client = client,
                blocks = if (selected?.slug == current.slug) body else emptyList(),
                loading = selected?.slug == current.slug && loading,
                needsInternet = selected?.slug == current.slug && needsInternet,
                onBack = { closeArticle() },
                onRetry = { openArticle(current) }
            )
            return@AnimatedContent
        }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { OphiaHeader() }
        item {
            PageTitle(
                title = "Articles",
                subtitle = "Published separately from the app. Refresh to see edits.",
                trailing = { TextButton(onClick = { refresh() }, enabled = !loading) { Text("Refresh", color = MutedGreen) } }
            )
        }
        if (loading && articles.isEmpty()) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.height(22.dp), strokeWidth = 2.dp, color = MutedGreen)
                    Spacer(Modifier.padding(6.dp))
                    Text("Loading articles…", color = Color(0xFFB7C4BC))
                }
            }
        }
        if (needsInternet && articles.isEmpty()) {
            item { ConnectCard(onRetry = { refresh() }) }
        }
        items(articles, key = { it.slug }) { article ->
            val press = remember(article.slug) { MutableInteractionSource() }
            InfoCard(MaterialTheme.colorScheme.surface) {
                Column(
                    modifier = Modifier
                        .pressScale(press)
                        .clickable(
                            interactionSource = press,
                            indication = null,
                            onClick = { openArticle(article) }
                        )
                ) {
                    ArticleThumb(client, article.image, Modifier.fillMaxWidth().height(140.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(article.title, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(Modifier.height(4.dp))
                    Text(article.summary, color = Color.LightGray, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(articleCredit(article.author, article.updated), color = Color(0xFFB7C4BC), fontSize = 12.sp)
                }
            }
        }
    }
    }
}

@Composable
private fun ArticleReader(
    title: String,
    updated: String,
    author: String,
    image: String,
    client: ArticleClient,
    blocks: List<ArticleBlock>,
    loading: Boolean,
    needsInternet: Boolean,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            OphiaHeader(onBack = onBack)
            Spacer(Modifier.height(8.dp))
            PageTitle(title = title, subtitle = articleCredit(author, updated))
            Spacer(Modifier.height(8.dp))
            ArticleThumb(client, image, Modifier.fillMaxWidth().height(180.dp))
        }
        if (loading) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.height(22.dp), strokeWidth = 2.dp, color = MutedGreen)
                    Spacer(Modifier.padding(6.dp))
                    Text("Loading this note…", color = Color(0xFFB7C4BC))
                }
            }
        }
        if (needsInternet) {
            item { ConnectCard(onRetry = onRetry) }
        }
        val bodyBlocks = if (
            blocks.firstOrNull()?.kind == ArticleBlock.Kind.TITLE &&
            blocks.first().text.equals(title, ignoreCase = true)
        ) blocks.drop(1) else blocks
        items(bodyBlocks) { block ->
            when (block.kind) {
                ArticleBlock.Kind.TITLE -> MarkdownLine(block.text, Color.White, 26.sp, FontWeight.Bold)
                ArticleBlock.Kind.SUBTITLE -> MarkdownLine(block.text, MutedGreen, 20.sp, FontWeight.SemiBold)
                ArticleBlock.Kind.HEADING -> MarkdownLine(block.text, Color.White, 16.sp, FontWeight.SemiBold)
                ArticleBlock.Kind.PARAGRAPH -> MarkdownLine(block.text, Color(0xFFE4EDE8), 15.sp, FontWeight.Normal)
                ArticleBlock.Kind.BULLET -> MarkdownLine("•  ${block.text}", Color(0xFFE4EDE8), 15.sp, FontWeight.Normal)
                ArticleBlock.Kind.NUMBER -> MarkdownLine(block.text, Color(0xFFE4EDE8), 15.sp, FontWeight.Normal)
                ArticleBlock.Kind.QUOTE -> MarkdownLine(block.text, Color(0xFFB7C4BC), 15.sp, FontWeight.Normal, italic = true)
            }
        }
    }
}

private fun articleCredit(author: String, updated: String): String {
    return listOf(author, updated).filter { it.isNotBlank() }.joinToString(" · ")
}

@Composable
private fun ConnectCard(onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().liquidGlass().padding(16.dp)) {
        Text("Connect to the internet", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            "These notes are published online. Connect, then try again.",
            color = Color(0xFFB7C4BC),
            fontSize = 14.sp
        )
        TextButton(onClick = onRetry) { Text("Try again", color = MutedGreen) }
    }
}

@Composable
private fun MarkdownLine(
    text: String,
    color: Color,
    fontSize: TextUnit,
    weight: FontWeight,
    italic: Boolean = false
) {
    Text(markdownInline(text, weight, italic), color = color, fontSize = fontSize)
}

private fun markdownInline(source: String, weight: FontWeight, italic: Boolean): AnnotatedString {
    return buildAnnotatedString {
        var index = 0
        while (index < source.length) {
            when {
                source.startsWith("**", index) -> {
                    val end = source.indexOf("**", index + 2)
                    if (end < 0) {
                        append(source.substring(index))
                        return@buildAnnotatedString
                    }
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(source.substring(index + 2, end))
                    }
                    index = end + 2
                }
                source.startsWith("`", index) -> {
                    val end = source.indexOf('`', index + 1)
                    if (end < 0) {
                        append(source.substring(index))
                        return@buildAnnotatedString
                    }
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                        append(source.substring(index + 1, end))
                    }
                    index = end + 1
                }
                source.startsWith("[", index) -> {
                    val labelEnd = source.indexOf("](", index + 1)
                    val urlEnd = if (labelEnd > 0) source.indexOf(')', labelEnd + 2) else -1
                    if (labelEnd < 0 || urlEnd < 0) {
                        append(source[index])
                        index += 1
                    } else {
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(source.substring(index + 1, labelEnd))
                        }
                        index = urlEnd + 1
                    }
                }
                source.startsWith("*", index) || source.startsWith("_", index) -> {
                    val mark = source[index]
                    val end = source.indexOf(mark, index + 1)
                    if (end < 0) {
                        append(source.substring(index))
                        return@buildAnnotatedString
                    }
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(source.substring(index + 1, end))
                    }
                    index = end + 1
                }
                else -> {
                    append(source[index])
                    index += 1
                }
            }
        }
        if (italic) {
            addStyle(SpanStyle(fontStyle = FontStyle.Italic, fontWeight = weight), 0, length)
        } else if (weight != FontWeight.Normal) {
            addStyle(SpanStyle(fontWeight = weight), 0, length)
        }
    }
}

@Composable
private fun ArticleThumb(client: ArticleClient, image: String, modifier: Modifier) {
    var bitmap by remember(image) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(image) {
        if (image.isBlank()) return@LaunchedEffect
        bitmap = runCatching { client.imageBytes(image)?.toImageBitmap() }.getOrNull()
    }
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1B2420))
    ) {
        Crossfade(targetState = bitmap, animationSpec = appleFade(), label = "thumb") { frame ->
            if (frame != null) {
                Image(
                    bitmap = frame,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
