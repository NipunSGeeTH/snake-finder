package com.snakeid.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.snakeid.platform.loadAssetBytes
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
    onOpenModel: () -> Unit,
    onOpenSpecies: (SnakeSpecies) -> Unit
) {
    val hero = remember { loadAssetBytes("brand/hero.jpg")?.toImageBitmap() }
    val photo = remember(imageBytes) { imageBytes?.toImageBitmap() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        item { OphiaHeader() }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().liquidGlass().padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("On-device model", color = MutedGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (activeModelId == ModelCatalog.WIDE) "135 classes · wide catalog" else "14 classes · South Asia",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                val detailsPress = rememberPressSource()
                TextButton(
                    onClick = onOpenModel,
                    interactionSource = detailsPress,
                    modifier = Modifier.pressScale(detailsPress).liquidGlass(RoundedCornerShape(20.dp))
                ) { Text("Details  ›", color = MutedGreen) }
            }
        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(228.dp)
                    .liquidGlass()
            ) {
                val shown = photo ?: hero
                if (shown != null) {
                    Image(
                        bitmap = shown,
                        contentDescription = if (photo != null) "Selected photo" else "Snake",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = if (photo != null) ContentScale.Fit else ContentScale.Crop
                    )
                    if (photo == null) {
                        Box(
                            Modifier.fillMaxSize().background(
                                Brush.horizontalGradient(listOf(Color(0xCC07110E), Color.Transparent))
                            )
                        )
                    }
                }
                if (photo == null) {
                    Column(modifier = Modifier.align(Alignment.CenterStart).padding(18.dp)) {
                        Text(
                            "Photograph the\nwhole snake",
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            color = Color.White,
                            lineHeight = 30.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Include the head if you can\ndo it from a safe distance.",
                            color = Color(0xFFD5DDD8),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
        item {
            val cameraPress = rememberPressSource()
            val galleryPress = rememberPressSource()
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .weight(1.25f)
                        .height(56.dp)
                        .pressScale(cameraPress)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF3DDC84), Color(0xFF14924A))))
                        .clickable(
                            interactionSource = cameraPress,
                            indication = null,
                            enabled = !busy,
                            onClick = onLaunchCamera
                        ),
                    contentAlignment = Alignment.Center
                ) { Text("Take photo", fontWeight = FontWeight.Bold, color = Color.White) }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .pressScale(galleryPress)
                        .liquidGlass(RoundedCornerShape(28.dp))
                        .clickable(
                            interactionSource = galleryPress,
                            indication = null,
                            enabled = !busy,
                            onClick = onLaunchGallery
                        ),
                    contentAlignment = Alignment.Center
                ) { Text("Gallery", fontWeight = FontWeight.Bold, color = Color.White) }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                GlassFact("On device", "Runs here", Modifier.weight(1f))
                GlassFact("Offline", "No network", Modifier.weight(1f))
                GlassFact("Wide list", "135 species", Modifier.weight(1f))
                GlassFact("Private", "Stays here", Modifier.weight(1f))
            }
        }
        if (busy) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("Checking the photo…", color = Color(0xFFE8F0EB))
                }
            }
        }
        if (error != null) {
            item { Text(error, color = Color(0xFFFF8A80)) }
        }
        if (predictions.isNotEmpty()) {
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(appleFade()) + scaleIn(appleSpring(), initialScale = 0.98f)
                ) {
                    ResultBanner(predictions.first())
                }
            }
            item { Text("Model scores", fontWeight = FontWeight.Bold, color = Color.White) }
            items(predictions) { prediction ->
                PredictionCard(prediction, onOpenSpecies)
            }
        }
    }
}

@Composable
private fun GlassFact(title: String, detail: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.liquidGlass(RoundedCornerShape(16.dp)).padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White, textAlign = TextAlign.Center, maxLines = 1)
        Text(detail, fontSize = 10.sp, color = Color(0xFF8AA394), textAlign = TextAlign.Center, maxLines = 1)
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
        Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
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
                Text(prediction.commonName, fontWeight = FontWeight.Bold, color = Color.White)
                if (prediction.scientificName.isNotEmpty()) {
                    Text(prediction.scientificName, fontStyle = FontStyle.Italic, color = Color.LightGray, fontSize = 13.sp)
                }
            }
            if (prediction.kind == ResultKind.SPECIES) {
                Text("${(prediction.confidence * 100).toInt()}%", fontWeight = FontWeight.Bold, color = Color.White)
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
            TextButton(onClick = { onOpenSpecies(species) }) { Text("Field marks", color = MutedGreen) }
        }
    }
}
