package com.example.islam.presentation.qibla

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.islam.R
import com.example.islam.core.navigation.Screen
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

// ─── Colors ───────────────────────────────────────────────────────────────────
private val Primary         = Color(0xFF87A96A)
private val EmeraldDeep     = Color(0xFF2D5A27)
private val BackgroundLight = Color(0xFFF9F9F7)
private val NeumorphicShadow    = Color(0xFFD1D1CF)
private val NeumorphicHighlight = Color(0xFFFFFFFF)
private val SlateLight      = Color(0xFFCBD5E1)   // slate-300
private val SlateText       = Color(0xFF475569)   // slate-500
private val SlateDark       = Color(0xFF1E293B)   // slate-800
private val LocationBg      = Color(0xFFE8EFE3)

// ─── Neumorphic helpers ────────────────────────────────────────────────────────
private fun Modifier.neumorphicShadow(
    cornerRadius: Dp = 16.dp,
    shadowColor: Color = NeumorphicShadow,
    highlightColor: Color = NeumorphicHighlight,
    offset: Dp = 8.dp,
    blurRadius: Dp = 16.dp,
): Modifier = this.drawBehind {
    val r = cornerRadius.toPx()
    val o = offset.toPx()
    // dark shadow (bottom-right)
    drawRoundRect(
        color = shadowColor,
        topLeft = Offset(o, o),
        size = size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(r),
        alpha = 0.8f
    )
    // highlight (top-left)
    drawRoundRect(
        color = highlightColor,
        topLeft = Offset(-o, -o),
        size = size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(r),
        alpha = 0.9f
    )
}

@Composable
fun QiblaScreen(
    navController: NavController,
    viewModel: QiblaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val qiblaDegrees = uiState.compass?.qiblaAngle ?: 0f
    val locationName = uiState.locationName.ifEmpty { "—" }
    val distanceKm = uiState.distanceKm

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundLight),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        }
        !uiState.hasSensor -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Pusula sensörü kullanılamıyor.",
                    color = SlateText,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        else -> {
            QiblaFinderContent(
                navController = navController,
                qiblaDegrees = qiblaDegrees,
                locationName = locationName,
                distanceKm = distanceKm
            )
        }
    }
}

@Composable
private fun QiblaFinderContent(
    navController: NavController?,
    qiblaDegrees: Float,
    locationName: String,
    distanceKm: Int,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        IslamicPatternBackground()

        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.Center)
                .offset(y = (-40).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Primary.copy(alpha = 0.10f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            QiblaHeader(navController = navController)

            Spacer(Modifier.weight(1f))
            CompassDial(arrowRotationDeg = qiblaDegrees)
            Spacer(Modifier.height(32.dp))

            DirectionInfo(
                degrees = qiblaDegrees.toInt(),
                locationName = locationName,
                distanceKm = distanceKm
            )
            Spacer(Modifier.weight(1f))
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Header
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun QiblaHeader(navController: NavController?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        NeumorphicIconButton(onClick = {
            navController?.navigate(Screen.Home.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
                launchSingleTop = true
            }
        }) { Icon(Icons.Outlined.GridView, contentDescription = "Ana sayfa", tint = SlateText) }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "QIBLA FINDER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SlateText,
                letterSpacing = 2.sp
            )
            Text(
                text = "HIGH PRECISION",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Primary,
                letterSpacing = 1.5.sp
            )
        }

        NeumorphicIconButton { Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = SlateText) }
    }
}

@Composable
private fun NeumorphicIconButton(onClick: () -> Unit = {}, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .neumorphicShadow(cornerRadius = 16.dp, offset = 5.dp, blurRadius = 10.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(BackgroundLight)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}

// ──────────────────────────────────────────────────────────────────────────────
// Compass Dial
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun CompassDial(arrowRotationDeg: Float) {
    val dialSize = 280.dp

    Box(
        modifier = Modifier.size(dialSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawNeumorphicCircle(size.width / 2, size.height / 2, size.width / 2 - 2.dp.toPx())
        }

        Canvas(
            modifier = Modifier
                .size(dialSize - 24.dp)
                .align(Alignment.Center)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFE8E8E6), BackgroundLight),
                    radius = size.width / 2
                )
            )
            drawCircle(
                color = NeumorphicShadow.copy(alpha = 0.4f),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        val innerSize = dialSize - 48.dp
        Box(
            modifier = Modifier.size(innerSize),
            contentAlignment = Alignment.Center
        ) {
            Text("N", modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp),
                color = SlateText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("E", modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp),
                color = SlateText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("S", modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
                color = SlateText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("W", modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp),
                color = SlateText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        Box(
            modifier = Modifier
                .size(dialSize - 24.dp)
                .rotate(arrowRotationDeg),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 10.dp)
                    .rotate(-arrowRotationDeg)
                    .size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.kible),
                    contentDescription = "Kabe",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Canvas(
                modifier = Modifier
                    .width(16.dp)
                    .height(110.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = 44.dp)
            ) {
                val path = Path().apply {
                    moveTo(size.width / 2f, 0f)
                    lineTo(size.width, size.height * 0.85f)
                    lineTo(size.width / 2f, size.height)
                    lineTo(0f, size.height * 0.85f)
                    close()
                }
                drawPath(
                    path = path,
                    brush = Brush.linearGradient(
                        colors = listOf(Primary, EmeraldDeep),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                )
                val shimmerPath = Path().apply {
                    moveTo(size.width / 2f, 0f)
                    lineTo(size.width / 2f, size.height * 0.85f)
                    lineTo(0f, size.height * 0.85f)
                    close()
                }
                drawPath(
                    path = shimmerPath,
                    color = Color.White.copy(alpha = 0.25f)
                )
            }
        }

        Box(
            modifier = Modifier
                .size(56.dp)
                .neumorphicShadow(cornerRadius = 28.dp, offset = 4.dp, blurRadius = 8.dp)
                .clip(CircleShape)
                .background(BackgroundLight)
                .zIndex(10f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(SlateLight)
            )
        }
    }
}

private fun DrawScope.drawNeumorphicCircle(cx: Float, cy: Float, radius: Float) {
    drawCircle(
        color = NeumorphicShadow,
        radius = radius,
        center = Offset(cx + 16.dp.toPx(), cy + 16.dp.toPx())
    )
    drawCircle(
        color = NeumorphicHighlight,
        radius = radius,
        center = Offset(cx - 16.dp.toPx(), cy - 16.dp.toPx())
    )
    drawCircle(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFE0E0DE)),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height)
        ),
        radius = radius,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.5f),
        radius = radius,
        style = Stroke(width = 1.dp.toPx())
    )
}

// ──────────────────────────────────────────────────────────────────────────────
// Direction Info + Location Card
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun DirectionInfo(degrees: Int, locationName: String, distanceKm: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                text = "$degrees",
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = SlateDark,
                letterSpacing = (-2).sp,
                lineHeight = 72.sp
            )
            Text(
                text = "°",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Primary,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        Text(
            text = "QIBLA DIRECTION",
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = SlateText,
            letterSpacing = 2.sp
        )

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .neumorphicShadow(cornerRadius = 24.dp, offset = 6.dp, blurRadius = 12.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(BackgroundLight)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(LocationBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CURRENT LOCATION",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = locationName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateDark
                )
            }

            Box(
                modifier = Modifier
                    .height(32.dp)
                    .width(1.dp)
                    .background(Color(0xFFE2E8F0))
            )

            Spacer(Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "DIST.",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "$distanceKm km",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Islamic Pattern Background
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun IslamicPatternBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val starColor = Color(0xFF87A96A).copy(alpha = 0.03f)
        val step = 60.dp.toPx()
        val cols = (size.width / step).toInt() + 2
        val rows = (size.height / step).toInt() + 2

        for (row in 0..rows) {
            for (col in 0..cols) {
                val cx = col * step
                val cy = row * step
                drawStar5(cx, cy, step * 0.35f, starColor)
            }
        }
    }
}

private fun DrawScope.drawStar5(cx: Float, cy: Float, r: Float, color: Color) {
    val path = Path()
    val innerR = r * 0.4f
    for (i in 0 until 10) {
        val angle = (i * 36.0 * PI / 180.0) - PI / 2
        val radius = if (i % 2 == 0) r else innerR
        val x = cx + (radius * cos(angle)).toFloat()
        val y = cy + (radius * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color)
}

// ──────────────────────────────────────────────────────────────────────────────
// Preview
// ──────────────────────────────────────────────────────────────────────────────
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun QiblaFinderPreview() {
    MaterialTheme {
        QiblaFinderContent(
            navController = null,
            qiblaDegrees = 147f,
            locationName = "Makkah, SA",
            distanceKm = 0
        )
    }
}
