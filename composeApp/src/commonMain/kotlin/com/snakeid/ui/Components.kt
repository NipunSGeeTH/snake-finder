package com.snakeid.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snakeid.domain.VenomStatus
import com.snakeid.domain.dangerStars
import com.snakeid.platform.loadAssetBytes
import com.snakeid.platform.toImageBitmap

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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlass()
            .padding(16.dp)
            .background(container.copy(alpha = 0.12f))
    ) {
        content()
    }
}

@Composable
fun TabGlyph(kind: String, tint: Color, modifier: Modifier = Modifier.size(26.dp)) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
        val c = Offset(size.width / 2f, size.height / 2f)
        when (kind) {
            "identify" -> {
                drawCircle(tint, radius = size.minDimension * 0.28f, style = stroke)
                drawLine(tint, Offset(c.x, 1.5.dp.toPx()), Offset(c.x, size.height - 1.5.dp.toPx()), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(1.5.dp.toPx(), c.y), Offset(size.width - 1.5.dp.toPx(), c.y), stroke.width, StrokeCap.Round)
            }
            "articles" -> {
                val left = 4.dp.toPx()
                val right = size.width - 4.dp.toPx()
                drawRoundRect(tint, topLeft = Offset(left, 3.dp.toPx()), size = androidx.compose.ui.geometry.Size(right - left, size.height - 6.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()), style = stroke)
                listOf(0.32f, 0.5f, 0.68f).forEach { y ->
                    drawLine(tint, Offset(left + 4.dp.toPx(), size.height * y), Offset(right - 4.dp.toPx(), size.height * y), stroke.width, StrokeCap.Round)
                }
            }
            "species" -> {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(3.dp.toPx(), size.height * 0.72f)
                    quadraticBezierTo(size.width * 0.35f, size.height * 0.05f, size.width * 0.55f, size.height * 0.48f)
                    quadraticBezierTo(size.width * 0.72f, size.height * 0.85f, size.width - 2.dp.toPx(), size.height * 0.28f)
                }
                drawPath(path, tint, style = stroke)
                drawCircle(tint, radius = 2.4.dp.toPx(), center = Offset(size.width - 3.dp.toPx(), size.height * 0.26f))
            }
            else -> {
                drawCircle(tint, radius = size.minDimension * 0.22f, center = c, style = stroke)
                for (index in 0 until 8) {
                    val angle = Math.toRadians((index * 45.0) - 90.0)
                    val inner = size.minDimension * 0.34f
                    val outer = size.minDimension * 0.46f
                    drawLine(
                        tint,
                        Offset(c.x + kotlin.math.cos(angle).toFloat() * inner, c.y + kotlin.math.sin(angle).toFloat() * inner),
                        Offset(c.x + kotlin.math.cos(angle).toFloat() * outer, c.y + kotlin.math.sin(angle).toFloat() * outer),
                        stroke.width,
                        StrokeCap.Round
                    )
                }
            }
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

val ScreenBackground = Color(0xFF07110E)
val MutedGreen = Color(0xFF7DFFB0)
val InkMuted = Color(0xFFB7C4BC)
val WarningAmber = Color(0xFFFFB74D)
val GlassShape = RoundedCornerShape(22.dp)
val AppBackground = Brush.verticalGradient(
    listOf(Color(0xFF1C4030), Color(0xFF0C1814), ScreenBackground)
)

@Composable
fun OphiaHeader(onBack: (() -> Unit)? = null) {
    val mark = remember { loadAssetBytes("brand/logo.jpg")?.toImageBitmap() }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            TextButton(onClick = onBack) {
                Text("‹", fontSize = 28.sp, color = MutedGreen, fontWeight = FontWeight.Medium)
            }
        }
        if (mark != null) {
            Image(
                bitmap = mark,
                contentDescription = "Ophia",
                modifier = Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("Ophia", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.White)
            Text("Discover nature, one photo", color = InkMuted, fontSize = 13.sp)
        }
    }
}

@Composable
fun PageTitle(
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.White)
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, color = InkMuted, fontSize = 13.sp)
            }
        }
        if (trailing != null) trailing()
    }
}

fun Modifier.liquidGlass(shape: Shape = GlassShape): Modifier = this
    .clip(shape)
    .background(
        Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.22f), Color(0xFF163026).copy(alpha = 0.62f), Color(0xFF0C1612).copy(alpha = 0.78f))
        )
    )
    .border(
        width = 1.dp,
        brush = Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.55f), Color(0xFF7DFFB0).copy(alpha = 0.16f), Color.White.copy(alpha = 0.08f))
        ),
        shape = shape
    )
