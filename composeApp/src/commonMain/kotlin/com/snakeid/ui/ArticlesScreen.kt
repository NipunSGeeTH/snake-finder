package com.snakeid.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snakeid.domain.ArticleBlock
import com.snakeid.domain.ArticleClient
import com.snakeid.domain.ArticleMeta
import com.snakeid.domain.parseArticle
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

    LaunchedEffect(Unit) { refresh() }

    if (selected != null) {
        ArticleReader(
            title = selected!!.title,
            updated = selected!!.updated,
            blocks = body,
            loading = loading,
            error = error,
            onBack = {
                selected = null
                body = emptyList()
                error = null
            }
        )
        return
    }

    LazyColumn(
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
        items(articles) { article ->
            InfoCard(MaterialTheme.colorScheme.surface) {
                Text(article.title, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(article.summary, color = Color.LightGray, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(article.updated, color = Color.Gray, fontSize = 11.sp)
                    Button(onClick = {
                        selected = article
                        loading = true
                        error = null
                        scope.launch {
                            val result = runCatching { parseArticle(client.body(article.slug)) }
                            loading = false
                            result.onSuccess { body = it }
                            result.onFailure { error = it.message ?: "Could not open this article." }
                        }
                    }) { Text("Read") }
                }
            }
        }
    }
}

@Composable
private fun ArticleReader(
    title: String,
    updated: String,
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
        }
        if (loading) {
            item { Text("Loading…", color = Color.Gray) }
        }
        if (error != null) {
            item { Text(error, color = Color(0xFFFF8A80)) }
        }
        items(blocks) { block ->
            when (block.kind) {
                ArticleBlock.Kind.HEADING -> Text(block.text, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                ArticleBlock.Kind.PARAGRAPH -> Text(block.text, color = Color(0xFFE0E0E0), fontSize = 15.sp)
                ArticleBlock.Kind.BULLET -> Text("• ${block.text}", color = Color(0xFFE0E0E0), fontSize = 15.sp)
            }
        }
    }
}
