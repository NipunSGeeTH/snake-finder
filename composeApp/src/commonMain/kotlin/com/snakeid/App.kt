@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.snakeid

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snakeid.domain.ModelCatalog
import com.snakeid.domain.Prediction
import com.snakeid.domain.SnakeSpecies
import com.snakeid.domain.parseWideLabels
import com.snakeid.history.ScanHistory
import com.snakeid.platform.ModelStore
import com.snakeid.platform.PlatformBackHandler
import com.snakeid.platform.SnakeClassifier
import com.snakeid.platform.bundledText
import com.snakeid.platform.loadBundledSample
import com.snakeid.platform.rememberImagePicker
import com.snakeid.ui.ArticlesScreen
import com.snakeid.ui.FieldGuideScreen
import com.snakeid.ui.HistoryScreen
import com.snakeid.ui.IdentifyScreen
import com.snakeid.ui.ModelScreen
import com.snakeid.ui.AppBackground
import com.snakeid.ui.MutedGreen
import com.snakeid.ui.ScreenBackground
import com.snakeid.ui.TabGlyph
import com.snakeid.ui.liquidGlass
import com.snakeid.ui.SettingsScreen
import com.snakeid.ui.SpeciesDetailDialog
import com.snakeid.ui.appleFade
import com.snakeid.ui.appleSpring
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class Place(val depth: Int) {
    IDENTIFY(0),
    ARTICLES(0),
    GUIDE(0),
    SETTINGS(0),
    HISTORY(1),
    MODEL(1)
}

private enum class RootTab(val title: String, val place: Place) {
    IDENTIFY("Identify", Place.IDENTIFY),
    ARTICLES("Articles", Place.ARTICLES),
    GUIDE("Species", Place.GUIDE),
    SETTINGS("Settings", Place.SETTINGS)
}

@Composable
fun App() {
    val classifier = remember { SnakeClassifier() }
    val modelStore = remember { ModelStore() }
    DisposableEffect(classifier) {
        onDispose { classifier.close() }
    }
    val history = remember { ScanHistory() }
    val wideLabels = remember { parseWideLabels(bundledText("models/snakesense_labels.tsv")) }
    var activeModelId by remember { mutableStateOf(modelStore.activeId()) }
    var readyIds by remember { mutableStateOf(ModelCatalog.offers.map { it.id }.filter { modelStore.isReady(it) }.toSet()) }
    var downloadId by remember { mutableStateOf<String?>(null) }
    var downloadPercent by remember { mutableStateOf(0) }
    var switchingModel by remember { mutableStateOf(false) }
    var modelError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var stack by remember { mutableStateOf(listOf(Place.IDENTIFY)) }
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var predictions by remember { mutableStateOf<List<Prediction>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var speciesDialog by remember { mutableStateOf<SnakeSpecies?>(null) }
    val requestIds = remember { intArrayOf(0) }
    val place = stack.last()

    fun open(next: Place) {
        if (stack.last() == next) return
        stack = stack + next
    }

    fun pop() {
        if (stack.size > 1) stack = stack.dropLast(1)
    }

    fun showRoot(next: Place) {
        stack = listOf(next)
    }

    fun identify(bytes: ByteArray, note: String) {
        val requestId = ++requestIds[0]
        imageBytes = bytes
        busy = true
        error = null
        predictions = emptyList()
        scope.launch {
            try {
                val list = withContext(Dispatchers.Default) { classifier.classify(bytes) }
                if (requestId != requestIds[0]) return@launch
                predictions = list
                list.firstOrNull()?.let { history.add(it, note) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                if (requestId == requestIds[0]) {
                    error = "Identification failed. If someone was bitten, go to a hospital."
                }
            } finally {
                if (requestId == requestIds[0]) busy = false
            }
        }
    }

    LaunchedEffect(Unit) {
        val saved = modelStore.activeId()
        if (saved == ModelCatalog.QUICK) return@LaunchedEffect
        switchingModel = true
        val message = withContext(Dispatchers.Default) { classifier.activate(saved) }
        switchingModel = false
        activeModelId = modelStore.activeId()
        if (message != null) modelError = message
    }

    val picker = rememberImagePicker { bytes -> identify(bytes, "Photo") }

    val colors = darkColorScheme(
        primary = Color(0xFF4CAF50),
        onPrimary = Color.White,
        primaryContainer = Color(0xFF1B5E20),
        onPrimaryContainer = Color.White,
        secondary = Color(0xFF81C784),
        onSecondary = Color.White,
        background = ScreenBackground,
        onBackground = Color(0xFFE8F0EB),
        surface = Color(0xFF1B1E1C),
        onSurface = Color(0xFFE8F0EB),
        surfaceVariant = Color(0xFF262C28),
        onSurfaceVariant = Color(0xFFB7C4BC),
        error = Color(0xFFEF5350),
        onError = Color.White
    )

    MaterialTheme(colorScheme = colors) {
        PlatformBackHandler(enabled = stack.size > 1, onBack = { pop() })
        Scaffold(
            containerColor = Color(0xFF1C4030),
            contentColor = Color(0xFFE8F0EB),
            bottomBar = {
                val selected = stack.firstOrNull { it.depth == 0 } ?: Place.IDENTIFY
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .liquidGlass(androidx.compose.foundation.shape.RoundedCornerShape(28.dp))
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RootTab.entries.forEach { item ->
                        val on = selected == item.place
                        val tint = if (on) MutedGreen else Color(0xFFE4F2EA)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showRoot(item.place) }
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (on) MutedGreen.copy(alpha = 0.18f) else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                TabGlyph(
                                    when (item.place) {
                                        Place.IDENTIFY -> "identify"
                                        Place.ARTICLES -> "articles"
                                        Place.GUIDE -> "species"
                                        else -> "settings"
                                    },
                                    tint
                                )
                            }
                            Text(
                                item.title,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = tint,
                                fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        ) { padding ->
            CompositionLocalProvider(LocalContentColor provides Color(0xFFE8F0EB)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(AppBackground)
            ) {
                AnimatedContent(
                    targetState = place,
                    transitionSpec = {
                        val forward = targetState.depth > initialState.depth ||
                            (targetState.depth == initialState.depth && targetState.ordinal > initialState.ordinal)
                        val enter = fadeIn(appleFade()) + slideInHorizontally(appleSpring()) { width ->
                            if (forward) width / 5 else -width / 5
                        }
                        val exit = fadeOut(appleFade()) + slideOutHorizontally(appleFade()) { width ->
                            if (forward) -width / 8 else width / 8
                        }
                        enter togetherWith exit
                    },
                    label = "screen"
                ) { current ->
                    when (current) {
                        Place.IDENTIFY -> IdentifyScreen(
                            imageBytes = imageBytes,
                            predictions = predictions,
                            busy = busy,
                            error = error,
                            activeModelId = activeModelId,
                            onLaunchCamera = { picker.launchCamera() },
                            onLaunchGallery = { picker.launchGallery() },
                            onSelectSample = { sampleId ->
                                val bytes = loadBundledSample(sampleId)
                                if (bytes == null) {
                                    error = "That sample photo is missing from the app."
                                } else {
                                    val title = com.snakeid.domain.samplePhotos.find { it.id == sampleId }?.title ?: sampleId
                                    identify(bytes, "Sample: $title")
                                }
                            },
                            onOpenModel = { open(Place.MODEL) },
                            onOpenSpecies = { speciesDialog = it }
                        )
                        Place.ARTICLES -> ArticlesScreen()
                        Place.SETTINGS -> SettingsScreen(
                            onGuide = { showRoot(Place.GUIDE) },
                            onHistory = { open(Place.HISTORY) },
                            onModel = { open(Place.MODEL) }
                        )
                        Place.GUIDE -> FieldGuideScreen(onSelectSpecies = { speciesDialog = it })
                        Place.HISTORY -> HistoryScreen(
                            records = history.records,
                            onBack = { pop() },
                            onClear = { history.clear() },
                            onOpenSpecies = { speciesDialog = it }
                        )
                        Place.MODEL -> ModelScreen(
                            activeId = activeModelId,
                            readyIds = readyIds,
                            downloadId = downloadId,
                            downloadPercent = downloadPercent,
                            switching = switchingModel,
                            error = modelError,
                            wideLabels = wideLabels,
                            onDownload = { modelId ->
                                modelError = null
                                downloadId = modelId
                                downloadPercent = 0
                                scope.launch {
                                    val result = runCatching {
                                        modelStore.download(modelId) { percent -> downloadPercent = percent }
                                    }
                                    downloadId = null
                                    readyIds = ModelCatalog.offers.map { it.id }.filter { modelStore.isReady(it) }.toSet()
                                    result.onFailure { failure ->
                                        modelError = failure.message ?: "The download failed."
                                    }
                                }
                            },
                            onSelect = { modelId ->
                                modelError = null
                                switchingModel = true
                                scope.launch {
                                    val message = withContext(Dispatchers.Default) { classifier.activate(modelId) }
                                    switchingModel = false
                                    if (message == null) {
                                        activeModelId = modelStore.activeId()
                                    } else {
                                        modelError = message
                                        activeModelId = modelStore.activeId()
                                    }
                                }
                            },
                            onBack = { pop() },
                            onDelete = { modelId ->
                                modelStore.delete(modelId)
                                readyIds = ModelCatalog.offers.map { it.id }.filter { modelStore.isReady(it) }.toSet()
                                scope.launch {
                                    classifier.activate(ModelCatalog.QUICK)
                                    activeModelId = ModelCatalog.QUICK
                                }
                            }
                        )
                    }
                }
                speciesDialog?.let { species ->
                    SpeciesDetailDialog(
                        species = species,
                        onDismiss = { speciesDialog = null }
                    )
                }
            }
            }
        }
    }
}
