@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.snakeid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snakeid.domain.ModelCatalog
import com.snakeid.domain.Prediction
import com.snakeid.domain.SnakeSpecies
import com.snakeid.domain.parseWideLabels
import com.snakeid.history.ScanHistory
import com.snakeid.platform.ModelStore
import com.snakeid.platform.SnakeClassifier
import com.snakeid.platform.bundledText
import com.snakeid.platform.loadBundledSample
import com.snakeid.platform.rememberImagePicker
import com.snakeid.ui.EmergencyScreen
import com.snakeid.ui.FieldGuideScreen
import com.snakeid.ui.HistoryScreen
import com.snakeid.ui.IdentifyScreen
import com.snakeid.ui.ModelScreen
import com.snakeid.ui.ScreenBackground
import com.snakeid.ui.SpeciesDetailDialog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class AppTab(val title: String, val icon: String) {
    IDENTIFY("Identify", "🐍"),
    GUIDE("Guide", "📖"),
    EMERGENCY("First aid", "🚨"),
    MODEL("Model", "📦"),
    HISTORY("History", "🕒")
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
    var tab by remember { mutableStateOf(AppTab.IDENTIFY) }
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var predictions by remember { mutableStateOf<List<Prediction>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var speciesDialog by remember { mutableStateOf<SnakeSpecies?>(null) }
    val requestIds = remember { intArrayOf(0) }

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
        onPrimary = Color(0xFF00390E),
        primaryContainer = Color(0xFF1B5E20),
        secondary = Color(0xFF81C784),
        background = ScreenBackground,
        surface = Color(0xFF1B1E1C),
        surfaceVariant = Color(0xFF262C28),
        error = Color(0xFFEF5350),
        onError = Color.White
    )

    MaterialTheme(colorScheme = colors) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(tab.icon, fontSize = 22.sp)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(tabTitle(tab), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    if (activeModelId == ModelCatalog.WIDE) "Wide catalog · 135 species" else "Offline · 14-class model",
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                    actions = {
                        if (tab != AppTab.EMERGENCY) {
                            FilledTonalButton(
                                onClick = { tab = AppTab.EMERGENCY },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFFB71C1C),
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.padding(end = 8.dp)
                            ) { Text("SOS", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    AppTab.entries.forEach { item ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = { Text(item.icon, fontSize = 18.sp) },
                            label = {
                                Text(
                                    item.title,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = if (tab == item) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (tab) {
                    AppTab.IDENTIFY -> IdentifyScreen(
                        imageBytes = imageBytes,
                        predictions = predictions,
                        busy = busy,
                        error = error,
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
                        onOpenEmergency = { tab = AppTab.EMERGENCY },
                        onOpenModel = { tab = AppTab.MODEL },
                        onOpenSpecies = { speciesDialog = it }
                    )
                    AppTab.GUIDE -> FieldGuideScreen(onSelectSpecies = { speciesDialog = it })
                    AppTab.EMERGENCY -> EmergencyScreen()
                    AppTab.MODEL -> ModelScreen(
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
                        onDelete = { modelId ->
                            modelStore.delete(modelId)
                            readyIds = ModelCatalog.offers.map { it.id }.filter { modelStore.isReady(it) }.toSet()
                            scope.launch {
                                classifier.activate(ModelCatalog.QUICK)
                                activeModelId = ModelCatalog.QUICK
                            }
                        }
                    )
                    AppTab.HISTORY -> HistoryScreen(
                        records = history.records,
                        onClear = { history.clear() },
                        onOpenSpecies = { speciesDialog = it }
                    )
                }
                speciesDialog?.let { species ->
                    SpeciesDetailDialog(
                        species = species,
                        onDismiss = { speciesDialog = null },
                        onOpenEmergency = {
                            speciesDialog = null
                            tab = AppTab.EMERGENCY
                        }
                    )
                }
            }
        }
    }
}

private fun tabTitle(tab: AppTab): String = when (tab) {
    AppTab.IDENTIFY -> "SnakeID"
    AppTab.GUIDE -> "Field guide"
    AppTab.EMERGENCY -> "Emergency first aid"
    AppTab.MODEL -> "On-device model"
    AppTab.HISTORY -> "History"
}
