package com.snakeid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snakeid.domain.VenomStatus
import com.snakeid.domain.dangerStars

fun venomColor(status: VenomStatus): Color = when (status) {
    VenomStatus.DEADLY -> Color(0xFFD32F2F)
    VenomStatus.VENOMOUS -> Color(0xFFF57C00)
    VenomStatus.MILDLY_VENOMOUS -> Color(0xFFFBC02D)
    VenomStatus.NON_VENOMOUS -> Color(0xFF388E3C)
}

@Composable
fun StatusBadge(status: VenomStatus) {
    val background = venomColor(status)
    Surface(color = background, shape = RoundedCornerShape(6.dp)) {
        Text(
            status.badge,
            color = if (status == VenomStatus.MILDLY_VENOMOUS) Color.Black else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun DangerLine(level: Int) {
    Text(
        "Danger ${dangerStars(level)}",
        color = Color(0xFFFFD54F),
        fontSize = 12.sp
    )
}

@Composable
fun ColorDot(color: Color) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
fun SectionTitle(text: String, color: Color = Color.White) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = color)
}

@Composable
fun LabeledBlock(title: String, body: String, titleColor: Color = Color.White) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = titleColor)
        Spacer(Modifier.height(2.dp))
        Text(body, fontSize = 12.sp, color = Color.LightGray)
    }
}

@Composable
fun InfoCard(container: Color, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = container)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun SampleDotRow(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ColorDot(color)
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 12.sp)
    }
}

val ScreenBackground = Color(0xFF121413)
val MutedGreen = Color(0xFF81C784)
val WarningAmber = Color(0xFFFFB74D)
