package com.example.islam.presentation.dhikr

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.islam.core.i18n.LocalStrings
import com.example.islam.domain.model.Dhikr
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val MAX_DOTS = 100

// ─────────────────────────────────────────────────────────────────────────────
// Ana ekran
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DhikrScreen(viewModel: DhikrViewModel = hiltViewModel()) {
    val state  by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current

    // ── Animasyonlar ──────────────────────────────────────────────────────────
    val lastDotScale        = remember { Animatable(1f) }
    val celebRingScale      = remember { Animatable(0.8f) }
    val celebRingAlpha      = remember { Animatable(0f) }

    val count = state.selectedDhikr?.count ?: 0
    LaunchedEffect(count) {
        if (count > 0) {
            lastDotScale.snapTo(1.9f)
            lastDotScale.animateTo(1f, spring(stiffness = Spring.StiffnessMediumLow))
        }
    }

    LaunchedEffect(state.isCelebrating) {
        if (state.isCelebrating) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            celebRingAlpha.snapTo(0.9f)
            celebRingScale.snapTo(0.75f)
            celebRingScale.animateTo(1.5f, tween(700, easing = FastOutSlowInEasing))
            celebRingAlpha.animateTo(0f, tween(600))
            celebRingScale.snapTo(0.8f)
        }
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val strings = LocalStrings.current
        Text(
            text = strings.dhikrCounter,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(14.dp))

        // Zikir seçici chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(state.dhikrList) { dhikr ->
                DhikrChip(
                    dhikr = dhikr,
                    isSelected = dhikr.id == state.selectedDhikr?.id,
                    onClick = { viewModel.selectDhikr(dhikr) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        state.selectedDhikr?.let { dhikr ->
            // Arapça metin
            Text(
                text = dhikr.arabicText,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = dhikr.meaning,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            // Sıfırla butonu (sağa hizalı, küçük)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { viewModel.reset() },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
                    )
                    Spacer(Modifier.width(4.dp))
                    val strings = LocalStrings.current
                    Text(
                        strings.reset,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
                    )
                }
            }

            // Tesbih halkası + merkez sayaç
            TasbeehRingWithCounter(
                dhikr          = dhikr,
                cycleCount     = state.cycleCount,
                isCelebrating  = state.isCelebrating,
                lastDotScale   = lastDotScale.value,
                celebRingScale = celebRingScale.value,
                celebRingAlpha = celebRingAlpha.value,
                onTap = {
                    if (!state.isCelebrating) {
                        val next = dhikr.count + 1
                        if (next % 33 == 0 || next >= dhikr.targetCount) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } else {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        viewModel.increment()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tesbih Halkası + Merkez Sayaç
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TasbeehRingWithCounter(
    dhikr: Dhikr,
    cycleCount: Int,
    isCelebrating: Boolean,
    lastDotScale: Float,
    celebRingScale: Float,
    celebRingAlpha: Float,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filledColor  = MaterialTheme.colorScheme.primary
    val emptyColor   = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
    val celebColor   = Color(0xFF66BB6A)
    val glowColor    = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    val ringColor    = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)

    // Kutlama sırasında sürekli nabız
    val celebPulse = if (isCelebrating) {
        val inf = rememberInfiniteTransition(label = "celeb_pulse")
        inf.animateFloat(
            initialValue = 0.92f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(tween(280), RepeatMode.Reverse),
            label = "celeb_pulse_val"
        ).value
    } else 1f

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // ── Kutlama ring efekti ────────────────────────────────────────────
        if (celebRingAlpha > 0f) {
            Canvas(
                modifier = Modifier
                    .size(270.dp)
                    .graphicsLayer {
                        scaleX = celebRingScale * celebPulse
                        scaleY = celebRingScale * celebPulse
                        alpha  = celebRingAlpha
                    }
            ) {
                drawCircle(
                    color  = celebColor,
                    radius = size.width / 2f - 4.dp.toPx(),
                    style  = Stroke(width = 10.dp.toPx())
                )
            }
        }

        // ── Ana tesbih Canvas ─────────────────────────────────────────────
        Canvas(
            modifier = Modifier
                .size(270.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onTap
                )
        ) {
            // Arka plan halka çizgisi
            drawCircle(
                color  = ringColor,
                radius = size.width * 0.42f,
                style  = Stroke(width = 2.dp.toPx())
            )

            drawTasbeehDots(
                count        = dhikr.count,
                targetCount  = dhikr.targetCount,
                isCelebrating= isCelebrating,
                lastDotScale = lastDotScale,
                filledColor  = if (isCelebrating) celebColor else filledColor,
                emptyColor   = emptyColor,
                glowColor    = glowColor
            )
        }

        // ── Merkez içerik ─────────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ana sayı — animasyonlu kayma
            AnimatedContent(
                targetState = if (isCelebrating) dhikr.targetCount else dhikr.count,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInVertically(tween(180)) { -it / 2 } + fadeIn(tween(180))) togetherWith
                        (slideOutVertically(tween(140)) { it / 2 } + fadeOut(tween(140)))
                    } else {
                        (slideInVertically(tween(280)) { it / 2 } + fadeIn(tween(280))) togetherWith
                        (slideOutVertically(tween(220)) { -it / 2 } + fadeOut(tween(220)))
                    }
                },
                label = "main_count"
            ) { n ->
                Text(
                    text  = "$n",
                    fontSize = 54.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isCelebrating) celebColor else MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text  = "/ ${dhikr.targetCount}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
            )

            Spacer(Modifier.height(6.dp))

            // Devir sayacı — ↺ N
            AnimatedContent(
                targetState = cycleCount,
                transitionSpec = {
                    (slideInVertically(tween(240)) { -it } + fadeIn(tween(240))) togetherWith
                    (slideOutVertically(tween(200)) { it } + fadeOut(tween(200)))
                },
                label = "cycle_count"
            ) { cycles ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text  = "↺ ",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                    Text(
                        text  = "$cycles",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
                    )
                }
            }
        }

        // Tamamlanma badgesi
        if (isCelebrating) {
            val strings = LocalStrings.current
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(50),
                color = celebColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = strings.dhikrCompleted,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Canvas çizim — tesbih bead'leri
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawTasbeehDots(
    count: Int,
    targetCount: Int,
    isCelebrating: Boolean,
    lastDotScale: Float,
    filledColor: Color,
    emptyColor: Color,
    glowColor: Color
) {
    val cx          = size.width / 2f
    val cy          = size.height / 2f
    val displayDots = targetCount.coerceAtMost(MAX_DOTS)
    val ringR       = size.width * 0.42f

    val spacing = (2 * PI * ringR / displayDots).toFloat()
    val baseDotR = (spacing * 0.34f).coerceIn(3.5.dp.toPx(), 8.5.dp.toPx())

    for (i in 0 until displayDots) {
        val angle  = (2 * PI * i / displayDots) - PI / 2
        val x      = cx + ringR * cos(angle).toFloat()
        val y      = cy + ringR * sin(angle).toFloat()
        val center = Offset(x, y)

        // Dolu/boş hesabı (büyük hedefler için oran bazlı)
        val isFilled = if (targetCount <= MAX_DOTS) {
            i < count
        } else {
            val portion = count.toFloat() / targetCount.toFloat()
            i < (portion * displayDots).toInt()
        }

        val isLastDot = !isCelebrating &&
                        targetCount <= MAX_DOTS &&
                        count > 0 &&
                        i == (count - 1).coerceAtMost(displayDots - 1)

        val dotR = if (isLastDot) baseDotR * lastDotScale else baseDotR

        // Son nokta için ışıma efekti
        if (isLastDot && lastDotScale > 1.1f) {
            drawCircle(color = glowColor, radius = dotR * 2.2f, center = center)
        }

        drawCircle(
            color  = if (isFilled) filledColor else emptyColor,
            radius = dotR,
            center = center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Zikir chip bileşeni
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DhikrChip(dhikr: Dhikr, isSelected: Boolean, onClick: () -> Unit) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(250),
        label = "chip_color"
    )

    Surface(
        shape = RoundedCornerShape(50),
        color = containerColor,
        modifier = Modifier.clickable(onClick = onClick),
        tonalElevation = if (isSelected) 0.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dhikr.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${dhikr.targetCount}×",
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
            )
        }
    }
}
