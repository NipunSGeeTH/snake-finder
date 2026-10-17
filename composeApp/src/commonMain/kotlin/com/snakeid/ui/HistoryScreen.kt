package com.snakeid.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snakeid.domain.ResultKind
import com.snakeid.domain.SnakeSpecies
import com.snakeid.domain.SnakeSpeciesDatabase
import com.snakeid.domain.VenomStatus
import com.snakeid.history.ScanRecord
import com.snakeid.platform.formatWhen

@Composable
fun HistoryScreen(
    records: List<ScanRecord>,
    onBack: () -> Unit,
    onClear: () -> Unit,
    onOpenSpecies: (SnakeSpecies) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { OphiaHeader(onBack = onBack) }
        item {
            PageTitle(
                title = "History",
                subtitle = "Saved on this phone (${records.size})",
                trailing = {
                    if (records.isNotEmpty()) {
                        TextButton(onClick = onClear) { Text("Clear", color = Color(0xFFEF5350)) }
                    }
                }
            )
        }
        if (records.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                    Text("No identifications yet.", color = Color.Gray)
                }
            }
        }
        items(records, key = { it.id }) { record ->
            val species = record.speciesId?.let { id -> SnakeSpeciesDatabase.speciesList.find { it.id == id } }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass()
                    .clickable(enabled = species != null) {
                        if (species != null) onOpenSpecies(species)
                    }
                    .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(record.commonName, fontWeight = FontWeight.Bold, color = Color.White)
                        if (record.scientificName.isNotEmpty()) {
                            Text(record.scientificName, fontStyle = FontStyle.Italic, color = Color.LightGray, fontSize = 12.sp)
                        }
                        Text("${formatWhen(record.epochMs)} · ${record.note}", color = Color.Gray, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${(record.confidence * 100).toInt()}%", fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.height(4.dp))
                        val status = runCatching { VenomStatus.valueOf(record.venomStatusName) }.getOrNull()
                        val kind = runCatching { ResultKind.valueOf(record.kindName) }.getOrNull()
                        if (kind == ResultKind.SPECIES && status != null) {
                            StatusBadge(status)
                        } else {
                            Text(kind?.name?.replace('_', ' ') ?: record.kindName, fontSize = 11.sp, color = Color.LightGray)
                        }
                    }
            }
        }
    }
}
