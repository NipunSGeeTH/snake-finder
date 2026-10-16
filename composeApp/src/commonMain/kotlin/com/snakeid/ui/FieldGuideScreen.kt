package com.snakeid.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snakeid.domain.SnakeSpecies
import com.snakeid.domain.SnakeSpeciesDatabase
import com.snakeid.domain.VenomStatus

@Composable
fun FieldGuideScreen(onSelectSpecies: (SnakeSpecies) -> Unit) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf<VenomStatus?>(null) }
    val species = remember(query, filter) {
        val searched = if (query.isBlank()) SnakeSpeciesDatabase.speciesList else SnakeSpeciesDatabase.search(query)
        if (filter == null) searched else searched.filter { it.venomStatus == filter }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Name, local name, or habitat") },
                shape = RoundedCornerShape(12.dp)
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                item {
                    FilterChip(
                        selected = filter == null,
                        onClick = { filter = null },
                        label = { Text("All (${SnakeSpeciesDatabase.speciesList.size})") }
                    )
                }
                item { StatusFilter(VenomStatus.DEADLY, "Deadly", filter) { filter = it } }
                item { StatusFilter(VenomStatus.VENOMOUS, "Venomous", filter) { filter = it } }
                item { StatusFilter(VenomStatus.MILDLY_VENOMOUS, "Mild", filter) { filter = it } }
                item { StatusFilter(VenomStatus.NON_VENOMOUS, "Harmless", filter) { filter = it } }
            }
        }
        if (species.isEmpty()) {
            item { Text("No species match that search.", color = Color.Gray) }
        }
        items(species) { item ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onSelectSpecies(item) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.commonName, fontWeight = FontWeight.Bold)
                            Text(item.scientificName, fontStyle = FontStyle.Italic, color = Color.LightGray, fontSize = 13.sp)
                        }
                        StatusBadge(item.venomStatus)
                    }
                    if (item.localNames.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(item.localNames, color = MutedGreen, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(item.habitat, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        DangerLine(item.dangerLevel)
                        Text("Open", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusFilter(
    status: VenomStatus,
    label: String,
    selected: VenomStatus?,
    onSelect: (VenomStatus?) -> Unit
) {
    FilterChip(
        selected = selected == status,
        onClick = { onSelect(if (selected == status) null else status) },
        label = { Text(label) }
    )
}
