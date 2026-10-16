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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snakeid.domain.ModelLabels
import com.snakeid.domain.SnakeSpeciesDatabase

@Composable
fun ModelScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            InfoCard(MaterialTheme.colorScheme.surfaceVariant) {
                Text("Built-in offline model", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text("MobileNetV2, float32, 224×224 input, 14 outputs, about 2 MB.", fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    "It runs entirely on the phone. There is no account and no photo upload. Larger species packs are not included: this build does not download placeholder weights or pretend a file is a 1,784-species model.",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            }
        }
        item {
            InfoCard(Color(0xFF2C2416)) {
                SectionTitle("What it cannot name", WarningAmber)
                Spacer(Modifier.height(6.dp))
                Text(
                    "The field guide has more species than the camera model. The model has no class for the hump-nosed pit viper, green pit viper, rock python, wolf snake, or cat snake. A photo of those animals will be forced into one of the 14 classes and can be wrong.",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
        }
        item {
            Text("Classes in label order", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
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
                        val scientific = species?.scientificName
                        if (!scientific.isNullOrEmpty() && scientific != label) {
                            Text(scientific, fontStyle = FontStyle.Italic, color = Color.LightGray, fontSize = 12.sp)
                        } else if (species == null) {
                            Text(
                                if (ModelLabels.isNoSnake(label)) "Background class" else "Not given a species name",
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                        }
                    }
                    if (species != null) StatusBadge(species.venomStatus)
                }
            }
        }
    }
}
