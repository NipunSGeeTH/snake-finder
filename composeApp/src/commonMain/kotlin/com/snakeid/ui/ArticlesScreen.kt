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
import androidx.compose.ui.text.font.FontWeight
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
    var error by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        loading = true
        error = null
        scope.launch {
            val result = runCatching { client.index() }
            loading = false
            result.onSuccess { articles = it }
            result.onFailure { error = it.message ?: "Could not load articles." }
        }
    }

    val listState = rememberLazyListState()

    fun closeArticle() {
        selected = null
        body = emptyList()
        error = null
    }

    fun openArticle(article: ArticleMeta) {
        selected = article
        loading = true
        error = null
        body = emptyList()
        scope.launch {
            val result = runCatching { parseArticle(client.body(article.slug)) }
            if (selected?.slug != article.slug) return@launch
            loading = false
            result.onSuccess { body = it }
            result.onFailure { error = it.message ?: "Could not open this article." }
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
                image = current.image,
                client = client,
                blocks = if (selected?.slug == current.slug) body else emptyList(),
                loading = selected?.slug == current.slug && loading,
                error = if (selected?.slug == current.slug) error else null,
                onBack = { closeArticle() }
            )
            return@AnimatedContent
        }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Articles", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Published as markdown on GitHub. Refresh to see edits.",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                TextButton(onClick = { refresh() }, enabled = !loading) { Text("Refresh") }
            }
        }
        if (loading && articles.isEmpty()) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.height(22.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.padding(6.dp))
                    Text("Loading articles…")
                }
            }
        }
        if (error != null && articles.isEmpty()) {
            item { Text(error!!, color = Color(0xFFFF8A80)) }
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
                    Text(article.title, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(article.summary, color = Color.LightGray, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(article.updated, color = Color.Gray, fontSize = 11.sp)
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
    image: String,
    client: ArticleClient,
    blocks: List<ArticleBlock>,
    loading: Boolean,
    error: String?,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            TextButton(onClick = onBack) { Text("Back") }
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(updated, color = Color.Gray, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            ArticleThumb(client, image, Modifier.fillMaxWidth().height(180.dp))
        }
        if (loading) {
            item { Text("Loading…", color = Color.Gray) }
        }
        if (error != null) {
            item { Text(error, color = Color(0xFFFF8A80)) }
        }
        val bodyBlocks = if (
            blocks.firstOrNull()?.kind == ArticleBlock.Kind.HEADING &&
            blocks.first().text.equals(title, ignoreCase = true)
        ) blocks.drop(1) else blocks
        items(bodyBlocks) { block ->
            when (block.kind) {
                ArticleBlock.Kind.HEADING -> Text(block.text, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                ArticleBlock.Kind.PARAGRAPH -> Text(block.text, color = Color(0xFFE0E0E0), fontSize = 15.sp)
                ArticleBlock.Kind.BULLET -> Text("• ${block.text}", color = Color(0xFFE0E0E0), fontSize = 15.sp)
            }
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
