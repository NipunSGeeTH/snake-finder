package com.snakeid.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.snakeid.domain.Identification
import com.snakeid.domain.Prediction
import com.snakeid.domain.ResultKind
import com.snakeid.domain.SnakeSpecies
import com.snakeid.domain.SnakeSpeciesDatabase
import com.snakeid.domain.VenomStatus
import com.snakeid.domain.samplePhotos
import com.snakeid.platform.toImageBitmap

@Composable
fun IdentifyScreen(
    imageBytes: ByteArray?,
    predictions: List<Prediction>,
    busy: Boolean,
    error: String?,
    onLaunchCamera: () -> Unit,
    onLaunchGallery: () -> Unit,
    onSelectSample: (String) -> Unit,
    onOpenEmergency: () -> Unit,
    onOpenModel: () -> Unit,
    onOpenSpecies: (SnakeSpecies) -> Unit
) {
    val image = remember(imageBytes) { imageBytes?.toImageBitmap() }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("On-device model", color = MutedGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("14 classes · South Asia", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(onClick = onOpenModel, contentPadding = PaddingValues(horizontal = 10.dp)) {
                        Text("Details")
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().height(240.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2320))
            ) {
                if (image != null) {
                    Image(
                        bitmap = image,
                        contentDescription = "Selected photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("🐍", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Photograph the whole snake", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text(
                            "Include the head if you can do it from a safe distance.",
                            color = Color.Gray,
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
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Take photo", fontWeight = FontWeight.Bold) }
                OutlinedButton(
                    onClick = onLaunchGallery,
                    enabled = !busy,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Gallery", fontWeight = FontWeight.Bold) }
            }
        }
        item {
            Text("Try a bundled photo", color = Color.LightGray, fontSize = 13.sp)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                items(samplePhotos) { sample ->
                    val species = SnakeSpeciesDatabase.speciesList.find { it.id == sample.id }
                    AssistChip(
                        onClick = { if (!busy) onSelectSample(sample.id) },
                        label = {
                            SampleDotRow(
                                color = species?.let { venomColor(it.venomStatus) } ?: Color.Gray,
                                label = sample.title
                            )
                        }
                    )
                }
            }
        }
        if (busy) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("Running the on-device model…")
                }
            }
        }
        if (error != null) {
            item { Text(error, color = Color(0xFFFF8A80)) }
        }
        if (predictions.isNotEmpty()) {
            item { ResultBanner(predictions.first(), onOpenEmergency) }
            item { Text("Model scores", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) }
            items(predictions) { prediction ->
                PredictionCard(prediction, onOpenSpecies)
            }
            item {
                Text(
                    Identification.PHOTO_LIMIT + " This model has no class for the hump-nosed pit viper.",
                    color = Color(0xFFFFCC80),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun ResultBanner(prediction: Prediction, onOpenEmergency: () -> Unit) {
    val (background, border, title) = when (prediction.kind) {
        ResultKind.NO_SNAKE -> Triple(Color(0xFF243028), Color(0xFF81C784), "No snake recognized")
        ResultKind.UNRECOGNIZED -> Triple(Color(0xFF42280A), Color(0xFFF57C00), "Species not recognized — treat as dangerous")
        ResultKind.UNAVAILABLE -> Triple(Color(0xFF2C1515), Color(0xFFEF5350), "Model did not produce a result")
        ResultKind.SPECIES -> when (prediction.venomStatus) {
            VenomStatus.DEADLY -> Triple(Color(0xFF4A1010), Color(0xFFD32F2F), "Deadly venom possible")
            VenomStatus.VENOMOUS -> Triple(Color(0xFF42280A), Color(0xFFF57C00), "Venomous species")
            VenomStatus.MILDLY_VENOMOUS -> Triple(Color(0xFF383508), Color(0xFFFBC02D), "Mildly venomous")
            VenomStatus.NON_VENOMOUS -> Triple(Color(0xFF103615), Color(0xFF4CAF50), "Listed as non-venomous")
        }
    }
    Card(colors = CardDefaults.cardColors(containerColor = background), shape = RoundedCornerShape(14.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontWeight = FontWeight.ExtraBold, color = Color.White)
            if (prediction.kind == ResultKind.SPECIES) {
                Spacer(Modifier.height(4.dp))
                DangerLine(prediction.dangerLevel)
            }
            if (prediction.uncertain && prediction.kind == ResultKind.SPECIES) {
                Spacer(Modifier.height(6.dp))
                val cobraLike = prediction.speciesId in setOf("naja_naja", "naja_kaouthia", "ophiophagus_hannah")
                Text(
                    if (cobraLike) {
                        "Low confidence. A raised rat snake is often mistaken for a cobra. Compare eyes, lip bars, and whether a real hood is spread."
                    } else {
                        "Low confidence. Compare the head, eyes, and scale pattern in the dossier before you trust this name."
                    },
                    color = WarningAmber,
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(prediction.advice, color = Color.White.copy(alpha = 0.92f), fontSize = 13.sp)
            if (prediction.lookAlikes.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(prediction.lookAlikes, color = Color(0xFFFFE0B2), fontSize = 12.sp, fontStyle = FontStyle.Italic)
            }
            if (prediction.isVenomous) {
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = onOpenEmergency,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = border),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Emergency steps") }
            }
        }
    }
}

@Composable
private fun PredictionCard(prediction: Prediction, onOpenSpecies: (SnakeSpecies) -> Unit) {
    val species = prediction.speciesId?.let { id -> SnakeSpeciesDatabase.speciesList.find { it.id == id } }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(prediction.commonName, fontWeight = FontWeight.Bold)
                    if (prediction.scientificName.isNotEmpty()) {
                        Text(prediction.scientificName, fontStyle = FontStyle.Italic, color = Color.LightGray, fontSize = 13.sp)
                    }
                }
                Text(
                    "${(prediction.confidence * 100).toInt()}%",
                    fontWeight = FontWeight.ExtraBold,
                    color = if (prediction.isVenomous) Color(0xFFEF5350) else MutedGreen
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { prediction.confidence.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = if (prediction.kind == ResultKind.SPECIES) venomColor(prediction.venomStatus) else Color.Gray
            )
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                if (prediction.kind == ResultKind.SPECIES) {
                    StatusBadge(prediction.venomStatus)
                } else {
                    Surface(color = Color(0xFF37474F), shape = RoundedCornerShape(6.dp)) {
                        Text(
                            prediction.kind.name.replace('_', ' '),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (species != null) {
                    TextButton(onClick = { onOpenSpecies(species) }) { Text("Dossier") }
                }
            }
        }
    }
}
