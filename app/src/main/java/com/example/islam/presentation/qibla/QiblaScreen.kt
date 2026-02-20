package com.example.islam.presentation.qibla

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
import com.airbnb.lottie.compose.*
import com.example.islam.core.i18n.LocalStrings
import kotlin.math.cos
import kotlin.math.sin

// ─────────────────────────────────────────────────────────────────────────────
// Renk sabitleri
// ─────────────────────────────────────────────────────────────────────────────

private val QiblaGreen      = Color(0xFF1B5E20)
private val QiblaGreenLight = Color(0xFF2E7D32)
private val QiblaGreenMid   = Color(0xFF43A047)
private val NeedleRed       = Color(0xFFD32F2F)

private fun isAligned(bearingToQibla: Float) =
    bearingToQibla < 5f || bearingToQibla > 355f

// ─────────────────────────────────────────────────────────────────────────────
// Ana ekran
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen(viewModel: QiblaViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic  = LocalHapticFeedback.current
    val strings = LocalStrings.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text       = strings.qiblaDirection,
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    Icon(
                        imageVector        = Icons.Outlined.Explore,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier
                            .padding(start = 16.dp)
                            .size(24.dp)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Arapça alt başlık
            Text(
                text  = "القبلة",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
            )

            Spacer(Modifier.height(20.dp))

            when {
                !uiState.hasSensor -> {
                    Spacer(Modifier.weight(1f))
                    NoSensorMessage()
                    Spacer(Modifier.weight(1f))
                }
                uiState.isLoading -> {
                    Spacer(Modifier.weight(1f))
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.weight(1f))
                }
                else -> {
                    val compass = uiState.compass!!

                    val animatedAzimuth by animateFloatAsState(
                        targetValue   = compass.azimuth,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label         = "azimuth"
                    )
                    val animatedBearing by animateFloatAsState(
                        targetValue   = compass.bearingToQibla,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label         = "bearing"
                    )

                    val aligned = isAligned(compass.bearingToQibla)

                    var wasAligned by remember { mutableStateOf(false) }
                    LaunchedEffect(aligned) {
                        if (aligned && !wasAligned)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        wasAligned = aligned
                    }

                    // Pulse animasyonu (hizalandığında yeşil parıltı)
                    val infiniteTransition = rememberInfiniteTransition(label = "glow")
                    val glowAlpha by infiniteTransition.animateFloat(
                        initialValue  = 0.1f,
                        targetValue   = if (aligned) 0.4f else 0.1f,
                        animationSpec = infiniteRepeatable(
                            animation  = tween(900, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "glow_alpha"
                    )

                    // Lottie state — Box dışında tanımlanmalı
                    val lottieComp by rememberLottieComposition(
                        LottieCompositionSpec.Asset("qibla_aligned.json")
                    )
                    val lottieProgress by animateLottieCompositionAsState(
                        composition   = lottieComp,
                        isPlaying     = aligned,
                        restartOnPlay = true,
                        iterations    = LottieConstants.IterateForever
                    )

                    // ── Pusula kutusu ────────────────────────────────────────
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier         = Modifier
                            .fillMaxWidth(0.88f)
                            .aspectRatio(1f)
                    ) {
                        // Gölgeli arka plan daire
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .shadow(
                                    elevation     = if (aligned) 16.dp else 8.dp,
                                    shape         = CircleShape,
                                    ambientColor  = if (aligned) QiblaGreenLight.copy(alpha = 0.3f)
                                                    else Color.Gray.copy(alpha = 0.2f),
                                    spotColor     = if (aligned) QiblaGreen.copy(alpha = 0.2f)
                                                    else Color.Gray.copy(alpha = 0.15f)
                                )
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                        )

                        // Pusula çizimi
                        CompassCanvas(
                            azimuth        = animatedAzimuth,
                            bearingToQibla = animatedBearing,
                            aligned        = aligned,
                            glowAlpha      = if (aligned) glowAlpha else 0f,
                            modifier       = Modifier.fillMaxSize()
                        )

                        // Lottie kutlama animasyonu (hizalandığında)
                        if (aligned) {
                            LottieAnimation(
                                composition = lottieComp,
                                progress    = { lottieProgress },
                                modifier    = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── Bilgi kartları ───────────────────────────────────────
                    InfoCardsRow(
                        azimuth        = compass.azimuth,
                        qiblaAngle     = compass.qiblaAngle,
                        bearingToQibla = compass.bearingToQibla
                    )

                    // ── Hizalama mesajı ──────────────────────────────────────
                    AnimatedVisibility(
                        visible = aligned,
                        enter   = fadeIn(tween(400)) + scaleIn(initialScale = 0.9f),
                        exit    = fadeOut(tween(300))
                    ) {
                        Column(
                            modifier            = Modifier.padding(top = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "🕋", fontSize = 32.sp)
                            Text(
                                text       = strings.qiblaAligned,
                                style      = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color      = QiblaGreenLight,
                                textAlign  = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pusula Canvas — gelişmiş görsel
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CompassCanvas(
    azimuth        : Float,
    bearingToQibla : Float,
    aligned        : Boolean,
    glowAlpha      : Float,
    modifier       : Modifier = Modifier
) {
    val bgColor         = MaterialTheme.colorScheme.surface
    val ringColor       = if (aligned) QiblaGreenLight else MaterialTheme.colorScheme.outline
    val tickMajorColor  = MaterialTheme.colorScheme.onSurface
    val tickMinorColor  = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
    val centerFillColor = MaterialTheme.colorScheme.surfaceVariant
    val labelColor      = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = modifier) {
        val cx     = size.width / 2f
        val cy     = size.height / 2f
        val outerR = minOf(cx, cy) * 0.90f
        val innerR = outerR * 0.80f

        // ── Arka plan ──────────────────────────────────────────────────
        drawCircle(
            brush  = Brush.radialGradient(
                colors = listOf(bgColor, bgColor.copy(alpha = 0.95f)),
                center = Offset(cx, cy),
                radius = outerR
            ),
            radius = outerR,
            center = Offset(cx, cy)
        )

        // ── Dönen pusula katmanı ───────────────────────────────────────
        rotate(-azimuth, pivot = Offset(cx, cy)) {

            // Dış halka
            drawCircle(
                color  = ringColor,
                radius = outerR,
                center = Offset(cx, cy),
                style  = Stroke(width = 2.5.dp.toPx())
            )

            // İç dekoratif halka
            drawCircle(
                color  = ringColor.copy(alpha = 0.3f),
                radius = innerR,
                center = Offset(cx, cy),
                style  = Stroke(width = 0.8.dp.toPx())
            )

            // Kadran tıkları
            for (deg in 0 until 360 step 5) {
                val isMajor  = deg % 30 == 0
                val isMinor5 = deg % 15 == 0
                val rad      = Math.toRadians(deg.toDouble())
                val len      = when {
                    isMajor  -> 18.dp.toPx()
                    isMinor5 -> 10.dp.toPx()
                    else     -> 5.dp.toPx()
                }
                val startR = outerR - len
                drawLine(
                    color       = when {
                        isMajor && deg == 0 -> NeedleRed
                        isMajor             -> tickMajorColor
                        isMinor5            -> tickMajorColor.copy(alpha = 0.5f)
                        else                -> tickMinorColor
                    },
                    start       = Offset(cx + startR * sin(rad).toFloat(), cy - startR * cos(rad).toFloat()),
                    end         = Offset(cx + outerR * sin(rad).toFloat(), cy - outerR * cos(rad).toFloat()),
                    strokeWidth = when {
                        isMajor  -> 2.8.dp.toPx()
                        isMinor5 -> 1.5.dp.toPx()
                        else     -> 0.8.dp.toPx()
                    }
                )
            }

            // Kadran etiketleri
            val lblR = outerR - 34.dp.toPx()
            drawCardinalLabel(this, "K", 0f,   cx, cy, lblR, NeedleRed,   42f)
            drawCardinalLabel(this, "D", 90f,  cx, cy, lblR, labelColor,  34f)
            drawCardinalLabel(this, "G", 180f, cx, cy, lblR, labelColor,  34f)
            drawCardinalLabel(this, "B", 270f, cx, cy, lblR, labelColor,  34f)

            // Pusula iğnesi
            drawCompassNeedle(
                cx           = cx, cy = cy,
                northColor   = NeedleRed,
                southColor   = tickMajorColor.copy(alpha = 0.35f),
                needleLength = outerR * 0.48f,
                needleWidth  = 9.dp.toPx()
            )
        }

        // ── Merkez daire ───────────────────────────────────────────────
        drawCircle(
            brush  = Brush.radialGradient(
                colors = listOf(centerFillColor, centerFillColor.copy(alpha = 0.8f)),
                center = Offset(cx, cy),
                radius = 22.dp.toPx()
            ),
            radius = 22.dp.toPx(),
            center = Offset(cx, cy)
        )
        drawCircle(
            color  = ringColor.copy(alpha = 0.5f),
            radius = 22.dp.toPx(),
            center = Offset(cx, cy),
            style  = Stroke(width = 1.5.dp.toPx())
        )

        // ── Kıble oku (sabit — dönmez) ─────────────────────────────────
        val bearRad   = Math.toRadians(bearingToQibla.toDouble())
        val arrowLen  = outerR * 0.58f
        val arrowEndX = cx + arrowLen * sin(bearRad).toFloat()
        val arrowEndY = cy - arrowLen * cos(bearRad).toFloat()

        // Hizalanma glow halkası
        if (glowAlpha > 0f) {
            drawCircle(
                brush  = Brush.radialGradient(
                    colors = listOf(QiblaGreenLight.copy(alpha = glowAlpha), Color.Transparent),
                    center = Offset(arrowEndX, arrowEndY),
                    radius = 40.dp.toPx()
                ),
                radius = 40.dp.toPx(),
                center = Offset(arrowEndX, arrowEndY)
            )
        }

        // Ok gövdesi (gölge + ana çizgi)
        drawLine(
            color       = QiblaGreen.copy(alpha = 0.2f),
            start       = Offset(cx + 2f, cy + 2f),
            end         = Offset(arrowEndX + 2f, arrowEndY + 2f),
            strokeWidth = 8.dp.toPx(),
            cap         = StrokeCap.Round
        )
        drawLine(
            brush       = Brush.linearGradient(
                colors = listOf(QiblaGreenMid, QiblaGreen),
                start  = Offset(cx, cy),
                end    = Offset(arrowEndX, arrowEndY)
            ),
            start       = Offset(cx, cy),
            end         = Offset(arrowEndX, arrowEndY),
            strokeWidth = 6.dp.toPx(),
            cap         = StrokeCap.Round
        )

        // Ok ucu (üçgen)
        val tipSize  = 13.dp.toPx()
        val perpRad  = bearRad + Math.PI / 2
        val tipLeft  = Offset(arrowEndX - tipSize * sin(perpRad).toFloat(), arrowEndY + tipSize * cos(perpRad).toFloat())
        val tipRight = Offset(arrowEndX + tipSize * sin(perpRad).toFloat(), arrowEndY - tipSize * cos(perpRad).toFloat())
        val tipTop   = Offset(
            arrowEndX + (tipSize * 1.8f) * sin(bearRad).toFloat(),
            arrowEndY - (tipSize * 1.8f) * cos(bearRad).toFloat()
        )
        val arrowPath = Path().apply {
            moveTo(tipTop.x, tipTop.y)
            lineTo(tipLeft.x, tipLeft.y)
            lineTo(tipRight.x, tipRight.y)
            close()
        }
        drawPath(arrowPath, color = QiblaGreen)

        // 🕋 Kabe emoji — ok ucunun biraz ötesinde
        val kaabeDist = arrowLen + 28.dp.toPx()
        val kaabeX    = cx + kaabeDist * sin(bearRad).toFloat()
        val kaabeY    = cy - kaabeDist * cos(bearRad).toFloat()
        drawContext.canvas.nativeCanvas.apply {
            val emPaint = android.graphics.Paint().apply {
                textSize  = 36f
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
            }
            drawText("🕋", kaabeX, kaabeY + emPaint.textSize / 3f, emPaint)
        }
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
        lineTo(cx - needleWidth / 2f, cy)
        lineTo(cx + needleWidth / 2f, cy)
        close()
    }
    val southPath = Path().apply {
        moveTo(cx, cy + needleLength)
        lineTo(cx - needleWidth / 2f, cy)
        lineTo(cx + needleWidth / 2f, cy)
        close()
    }
    drawPath(northPath, color = northColor)
    drawPath(southPath, color = southColor)
    drawCircle(color = southColor.copy(alpha = 0.6f), radius = 5.dp.toPx(), center = Offset(cx, cy))
}

private fun drawCardinalLabel(
    scope    : DrawScope,
    label    : String,
    angleDeg : Float,
    cx       : Float,
    cy       : Float,
    radius   : Float,
    color    : Color,
    textSize : Float = 36f
) {
    val rad = Math.toRadians(angleDeg.toDouble())
    val x   = cx + radius * sin(rad).toFloat()
    val y   = cy - radius * cos(rad).toFloat()
    scope.drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            this.textSize  = textSize
            textAlign      = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
            isAntiAlias    = true
            typeface       = android.graphics.Typeface.DEFAULT_BOLD
        }
        paint.color = android.graphics.Color.argb(
            (color.alpha * 255).toInt(),
            (color.red   * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue  * 255).toInt()
        )
        drawText(label, x, y + paint.textSize / 3f, paint)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bilgi kartları
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InfoCardsRow(
    azimuth        : Float,
    qiblaAngle     : Float,
    bearingToQibla : Float
) {
    val strings = LocalStrings.current
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        InfoCard(
            modifier  = Modifier.weight(1f),
            label     = strings.direction,
            value     = "${azimuth.toInt()}°"
        )
        InfoCard(
            modifier  = Modifier.weight(1f),
            label     = strings.qibla,
            value     = "${qiblaAngle.toInt()}°",
            accent    = true
        )
        InfoCard(
            modifier  = Modifier.weight(1f),
            label     = strings.deviation,
            value     = "${bearingToQibla.toInt()}°",
            highlight = isAligned(bearingToQibla)
        )
    }
}

@Composable
private fun InfoCard(
    modifier  : Modifier = Modifier,
    label     : String,
    value     : String,
    highlight : Boolean = false,
    accent    : Boolean = false
) {
    val containerColor = when {
        highlight -> QiblaGreenLight.copy(alpha = 0.12f)
        accent    -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else      -> MaterialTheme.colorScheme.surface
    }
    val valueColor = when {
        highlight -> QiblaGreenLight
        accent    -> MaterialTheme.colorScheme.primary
        else      -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (highlight) 4.dp else 1.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text       = value,
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = valueColor,
                textAlign  = TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sensör yok mesajı
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NoSensorMessage() {
    val strings = LocalStrings.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier         = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(QiblaGreenLight.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🧭", fontSize = 48.sp)
        }
        Text(
            text       = strings.noSensor,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign  = TextAlign.Center
        )
        Text(
            text      = strings.magnetometerRequired,
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}
