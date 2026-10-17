package com.snakeid.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snakeid.domain.ModelCatalog
import com.snakeid.domain.Prediction
import com.snakeid.domain.ResultKind
import com.snakeid.domain.SnakeSpecies
import com.snakeid.domain.SnakeSpeciesDatabase
import com.snakeid.domain.VenomStatus
import com.snakeid.domain.samplePhotos
import com.snakeid.platform.loadAssetBytes
import com.snakeid.platform.loadBundledSample
import com.snakeid.platform.toImageBitmap

@Composable
fun IdentifyScreen(
    imageBytes: ByteArray?,
    predictions: List<Prediction>,
    busy: Boolean,
    error: String?,
    activeModelId: String,
    onLaunchCamera: () -> Unit,
    onLaunchGallery: () -> Unit,
    onSelectSample: (String) -> Unit,
    onOpenModel: () -> Unit,
    onOpenSpecies: (SnakeSpecies) -> Unit
) {
    val hero = remember { loadAssetBytes("brand/hero.jpg")?.toImageBitmap() }
    val logo = remember { loadAssetBytes("brand/logo.jpg")?.toImageBitmap() }
    val photo = remember(imageBytes) { imageBytes?.toImageBitmap() }
    var showAllSamples by remember { mutableStateOf(false) }
    val samples = if (showAllSamples) samplePhotos else samplePhotos.take(3)
    val subtitle = if (activeModelId == ModelCatalog.WIDE) "Wide catalog · 135 species" else "Quick model · 14 classes"

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (logo != null) {
                    Image(
                        bitmap = logo,
                        contentDescription = "SnakeID",
                        modifier = Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Snake ID", fontWeight = FontWeight.Bold, fontSize = 26.sp)
                    Text(subtitle, color = Color(0xFF9CCC9C), fontSize = 13.sp)
                }
            }
        }
        item {
            InfoCard(Color(0xFF17241C)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("On-device model", color = MutedGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            if (activeModelId == ModelCatalog.WIDE) "135 classes · wide catalog" else "14 classes · South Asia",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    OutlinedButton(onClick = onOpenModel, shape = RoundedCornerShape(20.dp)) {
                        Text("Details")
                    }
                }
            }
        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF121A16))
            ) {
                val shown = photo ?: hero
                if (shown != null) {
                    Image(
                        bitmap = shown,
                        contentDescription = if (photo != null) "Selected photo" else "Snake",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = if (photo != null) ContentScale.Fit else ContentScale.Crop
                    )
                }
                if (photo == null) {
                    Column(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Photograph the whole snake", fontWeight = FontWeight.Bold, fontSize = 20.sp, textAlign = TextAlign.Center)
                        Text(
                            "Include the head if you can do it from a safe distance.",
                            color = Color(0xFFB0BEC5),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onLaunchCamera,
                    enabled = !busy,
                    modifier = Modifier.weight(1.2f).height(54.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) { Text("Take photo", fontWeight = FontWeight.Bold) }
                OutlinedButton(
                    onClick = onLaunchGallery,
                    enabled = !busy,
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(28.dp)
                ) { Text("Gallery", fontWeight = FontWeight.Bold) }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Try a bundled photo", fontWeight = FontWeight.SemiBold)
                TextButton(onClick = { showAllSamples = !showAllSamples }) {
                    Text(if (showAllSamples) "Show less" else "View all")
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(samples) { sample ->
                    SampleCard(sample.id, sample.title) { if (!busy) onSelectSample(sample.id) }
                }
            }
        }
        if (busy) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("Checking the photo…")
                }
            }
        }
        if (error != null) {
            item { Text(error, color = Color(0xFFFF8A80)) }
        }
        if (predictions.isNotEmpty()) {
            item { ResultBanner(predictions.first()) }
            item { Text("Model scores", fontWeight = FontWeight.Bold) }
            items(predictions) { prediction ->
                PredictionCard(prediction, onOpenSpecies)
            }
        }
    }
}

@Composable
private fun SampleCard(id: String, title: String, onClick: () -> Unit) {
    val image = remember(id) { loadBundledSample(id)?.toImageBitmap() }
    val species = SnakeSpeciesDatabase.speciesList.find { it.id == id }
    Column(
        modifier = Modifier
            .width(148.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1B2420))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(110.dp).background(Color(0xFF101614)),
            contentAlignment = Alignment.Center
        ) {
            if (image != null) {
                Image(bitmap = image, contentDescription = title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
        }
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(species?.let { venomColor(it.venomStatus) } ?: Color.Gray)
            )
            Spacer(Modifier.width(6.dp))
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
        }
    }
}

@Composable
private fun ResultBanner(prediction: Prediction) {
    val title = when (prediction.kind) {
        ResultKind.NO_SNAKE -> "Not a snake"
        ResultKind.UNRECOGNIZED -> "Species not recognized"
        ResultKind.UNAVAILABLE -> "Could not identify"
        ResultKind.SPECIES -> prediction.commonName
    }
    val color = when (prediction.kind) {
        ResultKind.SPECIES -> venomColor(prediction.venomStatus)
        ResultKind.NO_SNAKE -> Color(0xFF1B3324)
        else -> Color(0xFF3E2723)
    }
    InfoCard(color.copy(alpha = 0.35f)) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        if (prediction.kind == ResultKind.SPECIES) {
            if (prediction.scientificName.isNotEmpty()) {
                Text(prediction.scientificName, fontStyle = FontStyle.Italic, color = Color.LightGray)
            }
            Spacer(Modifier.height(6.dp))
            StatusBadge(prediction.venomStatus)
            if (prediction.uncertain) {
                Spacer(Modifier.height(6.dp))
                Text("Low confidence. Compare the field marks before you accept this name.", color = WarningAmber, fontSize = 12.sp)
            }
            if (prediction.lookAlikes.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(prediction.lookAlikes, color = Color(0xFFFFE0B2), fontSize = 12.sp)
            }
        } else {
            Spacer(Modifier.height(6.dp))
            Text(prediction.advice, color = Color(0xFFE0E0E0), fontSize = 13.sp)
        }
    }
}

@Composable
private fun PredictionCard(prediction: Prediction, onOpenSpecies: (SnakeSpecies) -> Unit) {
    val species = prediction.speciesId?.let { id -> SnakeSpeciesDatabase.speciesList.find { it.id == id } }
    InfoCard(MaterialTheme.colorScheme.surface) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(prediction.commonName, fontWeight = FontWeight.Bold)
                if (prediction.scientificName.isNotEmpty()) {
                    Text(prediction.scientificName, fontStyle = FontStyle.Italic, color = Color.LightGray, fontSize = 13.sp)
                }
            }
            if (prediction.kind == ResultKind.SPECIES) {
                Text("${(prediction.confidence * 100).toInt()}%", fontWeight = FontWeight.Bold)
            }
        }
        if (prediction.kind == ResultKind.SPECIES) {
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { prediction.confidence.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = venomColor(prediction.venomStatus)
            )
        }
        if (species != null) {
            TextButton(onClick = { onOpenSpecies(species) }) { Text("Field marks") }
        }
    }
}
