package com.snakeid.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snakeid.platform.dialPhone

@Composable
fun EmergencyScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            InfoCard(Color(0xFFB71C1C)) {
                SectionTitle("Get to a hospital", Color.White)
                Spacer(Modifier.height(8.dp))
                Text(
                    "This screen is general public guidance, not a personal medical order. Antivenom is given in hospital. Do not buy or inject it yourself.",
                    color = Color.White,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(8.dp))
                Text("Sri Lanka ambulance (Suwa Seriya): 1990", color = Color.White, fontWeight = FontWeight.Bold)
                Text("Sri Lanka National Hospital Colombo / poisons: 011 269 1111", color = Color.White, fontSize = 13.sp)
                Text("Sri Lanka police: 119", color = Color.White, fontSize = 13.sp)
                Text("India ambulance: 108 · India emergency: 112", color = Color.White, fontSize = 13.sp)
                Text("United States: 911 · Much of Europe: 112", color = Color.White, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { dialPhone("1990") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFFB71C1C))
                ) { Text("Call 1990", fontWeight = FontWeight.Bold) }
            }
        }
        item {
            InfoCard(Color(0xFF1B1E1C)) {
                SectionTitle("What to do", MutedGreen)
                Spacer(Modifier.height(8.dp))
                steps.forEach { (title, body) ->
                    LabeledBlock(title, body)
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
        item {
            InfoCard(Color(0xFF2C1515)) {
                SectionTitle("What not to do", Color(0xFFEF5350))
                Spacer(Modifier.height(8.dp))
                harms.forEach { (title, body) ->
                    LabeledBlock(title, body, Color(0xFFFF8A80))
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
        item {
            InfoCard(Color(0xFF1B1E1C)) {
                SectionTitle("Signs to tell the clinician", Color(0xFFFFD54F))
                Spacer(Modifier.height(8.dp))
                Text("Neurotoxic, such as cobra or krait: drooping eyelids, slurred speech, trouble swallowing, weak breathing.", fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Text("Hemotoxic, such as Russell's viper or saw-scaled viper: bleeding from the bite, gums, or nose, and blood that does not clot.", fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Text("Cytotoxic, such as many pit vipers: rapid swelling, blisters, and dark damaged skin.", fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "A bite can be serious before these signs appear. Krait bites are often nearly painless.",
                    fontSize = 12.sp,
                    color = Color(0xFFFFCC80)
                )
            }
        }
    }
}

private val steps = listOf(
    "1. Move away" to "Step back at least a few metres. Do not catch, corner, or kill the snake.",
    "2. Keep the person still" to "Movement spreads venom through the lymphatics. Lay them down and keep them calm.",
    "3. Splint the limb" to "Keep the bitten limb still, roughly at heart level, with a broad bandage and a firm support. This is not a tight tourniquet.",
    "4. Take off rings and watches" to "Remove anything that will tighten when the limb swells.",
    "5. Note the snake only if it is safe" to "A photo from a safe distance can help the hospital. Do not delay transport to get a better picture.",
    "6. Go to hospital" to "Use a vehicle. Ask for a hospital that can monitor breathing, clotting, and kidneys."
)

private val harms = listOf(
    "Do not cut the bite" to "Cutting causes bleeding and infection. It does not remove venom.",
    "Do not suck the wound" to "Mouth suction and suction pumps do not remove venom and can injure the helper.",
    "Do not use a tight tourniquet" to "A tight arterial tourniquet can kill tissue. Releasing it later can cause collapse.",
    "Do not use ice, chemicals, or electric shock" to "Ice, herbs, and shock do not neutralize venom and add injury.",
    "Do not give alcohol or aspirin" to "Alcohol can speed absorption. Aspirin makes bleeding harder to control."
)
