package com.example.islam.presentation.qibla

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaScreen(viewModel: QiblaViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Başlık
            Text(
                text = "Kıble Yönü",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "القبلة",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            when {
                // Sensör yok
                !uiState.hasSensor -> NoSensorMessage()

                // Yükleniyor
                uiState.isLoading -> CircularProgressIndicator()

                // Pusula göster
                else -> {
                    val compass = uiState.compass!!

                    // Animasyonlu azimut ve kıble açısı (titreme önleme)
                    val animatedAzimuth by animateFloatAsState(
                        targetValue = compass.azimuth,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "azimuth"
                    )
                    val animatedBearing by animateFloatAsState(
                        targetValue = compass.bearingToQibla,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "bearing"
                    )

                    // Canvas pusula
                    CompassCanvas(
                        azimuth = animatedAzimuth,
                        bearingToQibla = animatedBearing,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .aspectRatio(1f)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Alt bilgi satırı
                    CompassInfoRow(
                        azimuth = compass.azimuth,
                        qiblaAngle = compass.qiblaAngle,
                        bearingToQibla = compass.bearingToQibla
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Canvas Pusula
// ─────────────────────────────────────────────────────────────────────────────

/**
 * İki katmanlı pusula Canvas'ı:
 *
 * **Katman 1 — Dönen pusula halkası** (`rotate(-azimuth)`):
 *   Kuzey her zaman fiziksel kuzeye işaret eder.
 *   Kırmızı-beyaz iğne (kırmızı uç = Kuzey) ve derecelere göre tik işaretleri içerir.
 *
 * **Katman 2 — Sabit Kıble oku** (döndürülmez):
 *   `bearingToQibla` açısında yeşil bir ok çizer.
 *   Kıble tam önde olduğunda ok yukarıyı gösterir.
 */
@Composable
private fun CompassCanvas(
    azimuth: Float,
    bearingToQibla: Float,
    modifier: Modifier = Modifier
) {
    val ringColor         = MaterialTheme.colorScheme.outline
    val tickMajorColor    = MaterialTheme.colorScheme.onBackground
    val tickMinorColor    = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f)
    val northColor        = Color(0xFFD32F2F)   // Kuzey — kırmızı
    val southColor        = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
    val qiblaColor        = Color(0xFF2E7D32)   // Kıble — yeşil
    val centerFillColor   = MaterialTheme.colorScheme.surfaceVariant
    val labelColor        = MaterialTheme.colorScheme.onBackground

    Canvas(modifier = modifier) {
        val cx     = size.width / 2f
        val cy     = size.height / 2f
        val radius = minOf(cx, cy) * 0.88f

        // ── Katman 1: Dönen pusula ─────────────────────────────────────
        rotate(-azimuth, pivot = Offset(cx, cy)) {

            // Dış halka
            drawCircle(
                color  = ringColor,
                radius = radius,
                center = Offset(cx, cy),
                style  = Stroke(width = 3.dp.toPx())
            )

            // Tik işaretleri (her 5°'de bir, majör = 30°)
            for (deg in 0 until 360 step 5) {
                val isMajor = deg % 30 == 0
                val rad     = Math.toRadians(deg.toDouble())
                val len     = if (isMajor) 14.dp.toPx() else 6.dp.toPx()
                val innerR  = radius - len

                drawLine(
                    color       = if (isMajor) tickMajorColor else tickMinorColor,
                    start       = Offset(cx + innerR * sin(rad).toFloat(), cy - innerR * cos(rad).toFloat()),
                    end         = Offset(cx + radius * sin(rad).toFloat(), cy - radius * cos(rad).toFloat()),
                    strokeWidth = if (isMajor) 2.5.dp.toPx() else 1.dp.toPx()
                )
            }

            // Yön harfleri
            val labelRadius = radius - 28.dp.toPx()
            drawCardinalLabel(this, "K", 0f,   cx, cy, labelRadius, northColor)
            drawCardinalLabel(this, "D", 90f,  cx, cy, labelRadius, labelColor)
            drawCardinalLabel(this, "G", 180f, cx, cy, labelRadius, labelColor)
            drawCardinalLabel(this, "B", 270f, cx, cy, labelRadius, labelColor)

            // Kuzey iğnesi — kırmızı üçgen
            drawCompassNeedle(
                cx = cx, cy = cy,
                northColor = northColor,
                southColor = southColor,
                needleLength = radius * 0.52f,
                needleWidth = 10.dp.toPx()
            )
        }

        // ── Ortadaki daire (iki katmanı ayırır) ───────────────────────
        drawCircle(
            color  = centerFillColor,
            radius = 22.dp.toPx(),
            center = Offset(cx, cy)
        )
        drawCircle(
            color  = ringColor,
            radius = 22.dp.toPx(),
            center = Offset(cx, cy),
            style  = Stroke(width = 2.dp.toPx())
        )

        // ── Katman 2: Sabit Kıble oku (döndürülmez) ───────────────────
        val bearRad   = Math.toRadians(bearingToQibla.toDouble())
        val arrowLen  = radius * 0.58f
        val arrowEndX = cx + arrowLen * sin(bearRad).toFloat()
        val arrowEndY = cy - arrowLen * cos(bearRad).toFloat()

        // Ok gövdesi
        drawLine(
            color       = qiblaColor,
            start       = Offset(cx, cy),
            end         = Offset(arrowEndX, arrowEndY),
            strokeWidth = 5.dp.toPx(),
            cap         = StrokeCap.Round
        )

        // Ok ucu (üçgen)
        val tipSize  = 12.dp.toPx()
        val perpRad  = bearRad + Math.PI / 2
        val tipLeft  = Offset(
            arrowEndX - tipSize * sin(perpRad).toFloat(),
            arrowEndY + tipSize * cos(perpRad).toFloat()
        )
        val tipRight = Offset(
            arrowEndX + tipSize * sin(perpRad).toFloat(),
            arrowEndY - tipSize * cos(perpRad).toFloat()
        )
        val tipTop   = Offset(
            arrowEndX + (tipSize * 1.6f) * sin(bearRad).toFloat(),
            arrowEndY - (tipSize * 1.6f) * cos(bearRad).toFloat()
        )
        val arrowPath = Path().apply {
            moveTo(tipTop.x, tipTop.y)
            lineTo(tipLeft.x, tipLeft.y)
            lineTo(tipRight.x, tipRight.y)
            close()
        }
        drawPath(arrowPath, color = qiblaColor)

        // Kıble noktasındaki küçük halka
        drawCircle(
            color  = qiblaColor.copy(alpha = 0.25f),
            radius = 16.dp.toPx(),
            center = Offset(arrowEndX, arrowEndY)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Yardımcı çizim fonksiyonları
// ─────────────────────────────────────────────────────────────────────────────

/** Kuzey-Güney iğnesini çizer (kırmızı üst = kuzey). */
private fun DrawScope.drawCompassNeedle(
    cx: Float, cy: Float,
    northColor: Color, southColor: Color,
    needleLength: Float, needleWidth: Float
) {
    // Kuzey yarısı (kırmızı)
    val northPath = Path().apply {
        moveTo(cx, cy - needleLength)
        lineTo(cx - needleWidth / 2, cy)
        lineTo(cx + needleWidth / 2, cy)
        close()
    }
    drawPath(northPath, color = northColor)

    // Güney yarısı (soluk)
    val southPath = Path().apply {
        moveTo(cx, cy + needleLength)
        lineTo(cx - needleWidth / 2, cy)
        lineTo(cx + needleWidth / 2, cy)
        close()
    }
    drawPath(southPath, color = southColor)
}

/** Belirli bir pusula açısına harf etiketi çizer. */
private fun drawCardinalLabel(
    scope: DrawScope,
    label: String,
    angleDeg: Float,
    cx: Float, cy: Float,
    radius: Float,
    color: Color
) {
    val rad = Math.toRadians(angleDeg.toDouble())
    val x   = cx + radius * sin(rad).toFloat()
    val y   = cy - radius * cos(rad).toFloat()

    scope.drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            this.color        = color.hashCode()
            textSize          = 36f
            textAlign         = android.graphics.Paint.Align.CENTER
            isFakeBoldText    = true
        }
        // Kuzey harfini kırmızı yap
        paint.color = if (angleDeg == 0f)
            android.graphics.Color.rgb(211, 47, 47)
        else
            android.graphics.Color.rgb(
                (color.red * 255).toInt(),
                (color.green * 255).toInt(),
                (color.blue * 255).toInt()
            )
        drawText(label, x, y + paint.textSize / 3, paint)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Alt bilgi satırı
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CompassInfoRow(
    azimuth: Float,
    qiblaAngle: Float,
    bearingToQibla: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        InfoCell(label = "Yön", value = "${azimuth.toInt()}°")
        InfoCell(label = "Kıble", value = "${qiblaAngle.toInt()}°")
        InfoCell(
            label = "Sapma",
            value = "${bearingToQibla.toInt()}°",
            highlight = bearingToQibla < 5f || bearingToQibla > 355f
        )
    }
}

@Composable
private fun InfoCell(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text      = label,
            style     = MaterialTheme.typography.labelSmall,
            color     = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Text(
            text      = value,
            style     = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color     = if (highlight) Color(0xFF2E7D32)
            else MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Hata ekranı
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NoSensorMessage() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text      = "🧭",
            fontSize  = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text      = "Pusula sensörü bulunamadı",
            style     = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text      = "Bu özellik manyetometre sensörü gerektirmektedir.",
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}
