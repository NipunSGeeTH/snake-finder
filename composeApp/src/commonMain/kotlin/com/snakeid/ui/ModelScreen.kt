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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snakeid.domain.ModelCatalog
import com.snakeid.domain.ModelLabels
import com.snakeid.domain.ModelOffer
import com.snakeid.domain.SnakeSpeciesDatabase
import com.snakeid.domain.WideLabel

@Composable
fun ModelScreen(
    activeId: String,
    readyIds: Set<String>,
    downloadId: String?,
    downloadPercent: Int,
    switching: Boolean,
    error: String?,
    wideLabels: List<WideLabel>,
    onDownload: (String) -> Unit,
    onSelect: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "Choose the network used for the next photo. The quick model is already on the phone. The wide catalog is a separate file, downloaded once.",
                color = Color.LightGray,
                fontSize = 13.sp
            )
        }
        if (error != null) {
            item { Text(error, color = Color(0xFFFF8A80), fontSize = 13.sp) }
        }
        items(ModelCatalog.offers) { offer ->
            ModelCard(
                offer = offer,
                active = activeId == offer.id,
                ready = offer.id in readyIds,
                downloading = downloadId == offer.id,
                percent = downloadPercent,
                switching = switching,
                onDownload = { onDownload(offer.id) },
                onSelect = { onSelect(offer.id) },
                onDelete = { onDelete(offer.id) }
            )
        }
        item {
            Text(
                if (activeId == ModelCatalog.WIDE) "Wide catalog classes" else "Quick model classes",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
        if (activeId == ModelCatalog.WIDE) {
            items(wideLabels) { label ->
                val known = SnakeSpeciesDatabase.findSpecies(label.scientific)
                InfoCard(MaterialTheme.colorScheme.surface) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(known?.commonName ?: label.common, fontWeight = FontWeight.Bold)
                            Text(label.scientific, fontStyle = FontStyle.Italic, color = Color.LightGray, fontSize = 12.sp)
                        }
                        if (known != null) {
                            StatusBadge(known.venomStatus)
                        } else if (label.poisonous) {
                            Text("VENOMOUS", color = Color(0xFFFF8A80), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(ModelLabels.labels) { label ->
                val species = ModelLabels.speciesIdFor(label)?.let { id ->
                    SnakeSpeciesDatabase.speciesList.find { it.id == id }
                }
                InfoCard(MaterialTheme.colorScheme.surface) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(species?.commonName ?: label, fontWeight = FontWeight.Bold)
                            if (species != null) {
                                Text(species.scientificName, fontStyle = FontStyle.Italic, color = Color.LightGray, fontSize = 12.sp)
                            }
                        }
                        if (species != null) StatusBadge(species.venomStatus)
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelCard(
    offer: ModelOffer,
    active: Boolean,
    ready: Boolean,
    downloading: Boolean,
    percent: Int,
    switching: Boolean,
    onDownload: () -> Unit,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    InfoCard(if (active) Color(0xFF1B3324) else MaterialTheme.colorScheme.surfaceVariant) {
        Text(offer.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        if (active) {
            Text("In use", color = MutedGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(6.dp))
        Text(offer.summary, color = Color.LightGray, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        Text("${offer.speciesCount} classes · about ${offer.sizeMb} MB", fontSize = 12.sp)
        Text(offer.credit, color = Color.Gray, fontSize = 11.sp)
        Spacer(Modifier.height(10.dp))
        when {
            downloading -> {
                LinearProgressIndicator(
                    progress = { percent / 100f },
                    modifier = Modifier.fillMaxWidth().height(8.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text("Downloading $percent%", fontSize = 12.sp)
            }
            switching && active -> Text("Opening the model…", fontSize = 12.sp)
            !ready -> {
                Button(onClick = onDownload, shape = RoundedCornerShape(10.dp)) {
                    Text("Download ${offer.sizeMb} MB")
                }
            }
            else -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!active) {
                        Button(onClick = onSelect, enabled = !switching, shape = RoundedCornerShape(10.dp)) {
                            Text("Use this model")
                        }
                    }
                    if (!offer.builtIn) {
                        OutlinedButton(onClick = onDelete, enabled = !switching, shape = RoundedCornerShape(10.dp)) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}
