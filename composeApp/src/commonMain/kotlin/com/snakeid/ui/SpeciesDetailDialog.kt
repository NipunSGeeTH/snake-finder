package com.snakeid.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.snakeid.domain.SnakeSpecies
import com.snakeid.domain.VenomStatus

@Composable
fun SpeciesDetailDialog(
    species: SnakeSpecies,
    onDismiss: () -> Unit,
    onOpenEmergency: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(species.commonName, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                    StatusBadge(species.venomStatus)
                }
                Text(species.scientificName, fontStyle = FontStyle.Italic, color = Color.LightGray, fontSize = 13.sp)
                if (species.localNames.isNotEmpty()) {
                    Text(species.localNames, color = MutedGreen, fontSize = 12.sp)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DangerLine(species.dangerLevel)
                Text("Field marks", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                species.identifyingFeatures.forEach { feature ->
                    Text("• $feature", fontSize = 12.sp, color = Color.LightGray)
                }
                Text("Habitat and behavior", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(species.habitat, fontSize = 12.sp, color = Color.LightGray)
                Text(species.behavior, fontSize = 12.sp, color = Color.LightGray)
                Text("If bitten", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFFF8A80))
                Text(species.biteSymptoms, fontSize = 12.sp, color = Color.LightGray)
                Text(species.firstAidAdvice, fontSize = 12.sp, color = Color.LightGray)
                if (species.lookAlikes.isNotEmpty()) {
                    Text("Look-alikes", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFFFCC80))
                    Text(species.lookAlikes, fontSize = 12.sp, color = Color(0xFFFFE0B2), fontStyle = FontStyle.Italic)
                }
            }
        },
        confirmButton = {
            if (species.venomStatus != VenomStatus.NON_VENOMOUS) {
                Button(
                    onClick = onOpenEmergency,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Emergency steps") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
