package com.example.islam.presentation.qibla

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.islam.core.i18n.LocalStrings
import kotlin.math.cos
import kotlin.math.sin

private fun isAligned(bearingToQibla: Float) =
    bearingToQibla < 5f || bearingToQibla > 355f

@Composable
fun QiblaScreen(viewModel: QiblaViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current

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
            val strings = LocalStrings.current
            Text(
                text = strings.qiblaDirection,
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
                !uiState.hasSensor -> NoSensorMessage()
                uiState.isLoading  -> CircularProgressIndicator()
                else -> {
                    val compass = uiState.compass!!

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

                    val aligned = isAligned(compass.bearingToQibla)

                    // Haptik: hizalandığında titreşim ver (sadece bir kez, durum değişince)
                    var wasAligned by remember { mutableStateOf(false) }
                    LaunchedEffect(aligned) {
                        if (aligned && !wasAligned) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        wasAligned = aligned
                    }

                    // Hizalandığında yeşil parıltı (pulsing)
                    val infiniteTransition = rememberInfiniteTransition(label = "qibla_glow")
                    val glowAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.15f,
                        targetValue = if (aligned) 0.45f else 0.15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "glow_alpha"
                    )

                    CompassCanvas(
                        azimuth = animatedAzimuth,
                        bearingToQibla = animatedBearing,
                        aligned = aligned,
                        glowAlpha = if (aligned) glowAlpha else 0f,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .aspectRatio(1f)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    CompassInfoRow(
                        azimuth = compass.azimuth,
                        qiblaAngle = compass.qiblaAngle,
                        bearingToQibla = compass.bearingToQibla
                    )

                    // Hizalama tebrik mesajı
                    if (aligned) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = strings.qiblaAligned,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E7D32),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Canvas Pusula — glow efekti eklenmiş
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CompassCanvas(
    azimuth: Float,
    bearingToQibla: Float,
    aligned: Boolean,
    glowAlpha: Float,
    modifier: Modifier = Modifier
) {
    val ringColor      = MaterialTheme.colorScheme.outline
    val tickMajorColor = MaterialTheme.colorScheme.onBackground
    val tickMinorColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f)
    val northColor     = Color(0xFFD32F2F)
    val southColor     = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
    val qiblaColor     = if (aligned) Color(0xFF1B5E20) else Color(0xFF2E7D32)
    val centerFillColor = MaterialTheme.colorScheme.surfaceVariant
    val labelColor     = MaterialTheme.colorScheme.onBackground

    Canvas(modifier = modifier) {
        val cx     = size.width / 2f
        val cy     = size.height / 2f
        val radius = minOf(cx, cy) * 0.88f

        // ── Katman 1: Dönen pusula ─────────────────────────────────────
        rotate(-azimuth, pivot = Offset(cx, cy)) {
            drawCircle(
                color  = ringColor,
                radius = radius,
                center = Offset(cx, cy),
                style  = Stroke(width = 3.dp.toPx())
            )

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

            val labelRadius = radius - 28.dp.toPx()
            drawCardinalLabel(this, "K", 0f,   cx, cy, labelRadius, northColor)
            drawCardinalLabel(this, "D", 90f,  cx, cy, labelRadius, labelColor)
            drawCardinalLabel(this, "G", 180f, cx, cy, labelRadius, labelColor)
            drawCardinalLabel(this, "B", 270f, cx, cy, labelRadius, labelColor)

            drawCompassNeedle(
                cx = cx, cy = cy,
                northColor = northColor,
                southColor = southColor,
                needleLength = radius * 0.52f,
                needleWidth  = 10.dp.toPx()
            )
        }

        // ── Ortadaki daire ─────────────────────────────────────────────
        drawCircle(color = centerFillColor, radius = 22.dp.toPx(), center = Offset(cx, cy))
        drawCircle(
            color  = ringColor,
            radius = 22.dp.toPx(),
            center = Offset(cx, cy),
            style  = Stroke(width = 2.dp.toPx())
        )

        // ── Katman 2: Sabit Kıble oku ──────────────────────────────────
        val bearRad   = Math.toRadians(bearingToQibla.toDouble())
        val arrowLen  = radius * 0.58f
        val arrowEndX = cx + arrowLen * sin(bearRad).toFloat()
        val arrowEndY = cy - arrowLen * cos(bearRad).toFloat()

        // Hizalandığında pulsing yeşil glow çemberi
        if (glowAlpha > 0f) {
            drawCircle(
                color  = Color(0xFF2E7D32).copy(alpha = glowAlpha),
                radius = 28.dp.toPx(),
                center = Offset(arrowEndX, arrowEndY)
            )
        }

        // Ok gövdesi
        drawLine(
            color       = qiblaColor,
            start       = Offset(cx, cy),
            end         = Offset(arrowEndX, arrowEndY),
            strokeWidth = 5.dp.toPx(),
            cap         = StrokeCap.Round
        )

        // Ok ucu
        val tipSize  = 12.dp.toPx()
        val perpRad  = bearRad + Math.PI / 2
        val tipLeft  = Offset(arrowEndX - tipSize * sin(perpRad).toFloat(), arrowEndY + tipSize * cos(perpRad).toFloat())
        val tipRight = Offset(arrowEndX + tipSize * sin(perpRad).toFloat(), arrowEndY - tipSize * cos(perpRad).toFloat())
        val tipTop   = Offset(arrowEndX + (tipSize * 1.6f) * sin(bearRad).toFloat(), arrowEndY - (tipSize * 1.6f) * cos(bearRad).toFloat())
        val arrowPath = Path().apply {
            moveTo(tipTop.x, tipTop.y)
            lineTo(tipLeft.x, tipLeft.y)
            lineTo(tipRight.x, tipRight.y)
            close()
        }
        drawPath(arrowPath, color = qiblaColor)

        // Kıble noktasındaki küçük halka
        drawCircle(
            color  = qiblaColor.copy(alpha = if (aligned) 0.4f else 0.25f),
            radius = 16.dp.toPx(),
            center = Offset(arrowEndX, arrowEndY)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Yardımcı çizim fonksiyonları
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawCompassNeedle(
    cx: Float, cy: Float,
    northColor: Color, southColor: Color,
    needleLength: Float, needleWidth: Float
) {
    val northPath = Path().apply {
        moveTo(cx, cy - needleLength)
        lineTo(cx - needleWidth / 2, cy)
        lineTo(cx + needleWidth / 2, cy)
        close()
    }
    drawPath(northPath, color = northColor)

    val southPath = Path().apply {
        moveTo(cx, cy + needleLength)
        lineTo(cx - needleWidth / 2, cy)
        lineTo(cx + needleWidth / 2, cy)
        close()
    }
    drawPath(southPath, color = southColor)
}

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
            textSize       = 36f
            textAlign      = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
        }
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
    val strings = LocalStrings.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        InfoCell(label = strings.direction, value = "${azimuth.toInt()}°")
        InfoCell(label = strings.qibla,     value = "${qiblaAngle.toInt()}°")
        InfoCell(
            label     = strings.deviation,
            value     = "${bearingToQibla.toInt()}°",
            highlight = isAligned(bearingToQibla)
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
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = if (highlight) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onBackground,
            textAlign  = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sensör yok mesajı
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NoSensorMessage() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Pusula illüstrasyonu (SVG yok, native Canvas ile çiziyoruz)
        Canvas(modifier = Modifier.size(96.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r = minOf(cx, cy) * 0.9f
            drawCircle(color = Color(0xFF2E7D32).copy(alpha = 0.15f), radius = r)
            drawCircle(color = Color(0xFF2E7D32), radius = r, style = Stroke(width = 3.dp.toPx()))
            // Kuzey iğnesi
            drawLine(Color(0xFFD32F2F), Offset(cx, cy), Offset(cx, cy - r * 0.6f), strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
            // Güney iğnesi
            drawLine(Color(0xFF9E9E9E), Offset(cx, cy), Offset(cx, cy + r * 0.6f), strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
            // Merkez nokta
            drawCircle(color = Color(0xFF2E7D32), radius = 8.dp.toPx())
        }
        val strings = LocalStrings.current
        Text(
            text      = strings.noSensor,
            style     = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Text(
            text      = strings.magnetometerRequired,
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}
