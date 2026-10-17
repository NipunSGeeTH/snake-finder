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
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
        item { OphiaHeader() }
        item {
            PageTitle("Species", "Field notes kept on the phone")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Name, local name, or habitat") },
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MutedGreen,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.22f),
                    focusedContainerColor = Color.White.copy(alpha = 0.06f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = MutedGreen
                )
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                item {
                    FilterChip(
                        selected = filter == null,
                        onClick = { filter = null },
                        label = { Text("All (${SnakeSpeciesDatabase.speciesList.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.White.copy(alpha = 0.08f),
                            labelColor = Color.White,
                            selectedContainerColor = Color(0xFF1B5E20),
                            selectedLabelColor = Color.White
                        )
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
            Column(
                modifier = Modifier.fillMaxWidth().liquidGlass().clickable { onSelectSpecies(item) }.padding(14.dp)
            ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.commonName, fontWeight = FontWeight.Bold, color = Color.White)
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
                        Text("Open", color = MutedGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.White.copy(alpha = 0.08f),
            labelColor = Color.White,
            selectedContainerColor = Color(0xFF1B5E20),
            selectedLabelColor = Color.White
        )
    )
}
