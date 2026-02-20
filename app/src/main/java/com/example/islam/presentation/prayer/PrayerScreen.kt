package com.example.islam.presentation.prayer

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.islam.core.i18n.LocalStrings
import com.example.islam.domain.model.Prayer
import com.example.islam.domain.model.PrayerTime
import com.example.islam.domain.model.timeFor
import com.example.islam.core.util.DateUtil.cleanTime

// Kılınabilir (takip edilebilir) namazlar — Güneş ve İmsak dahil değil
private val TRACKABLE_PRAYERS = setOf(
    Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA
)

@Composable
fun PrayerScreen(viewModel: PrayerViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Şehir ve tarih başlığı
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "${state.userPreferences.city}, ${state.userPreferences.country}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        state.prayerTime?.hijriDate?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // Tamamlanma ilerleme çubuğu
        val completedCount = state.completedPrayers.size
        val totalTrackable = TRACKABLE_PRAYERS.size
        if (completedCount > 0 || state.prayerTime != null) {
            CompletionProgress(completed = completedCount, total = totalTrackable)
        }

        Spacer(Modifier.height(12.dp))

        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                val strings = LocalStrings.current
                Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = viewModel::refresh) { Text(strings.refresh) }
            }
            state.prayerTime != null -> {
                PrayerList(
                    prayerTime = state.prayerTime!!,
                    currentPrayer = state.currentPrayer,
                    completedPrayers = state.completedPrayers,
                    onToggleCompleted = { prayer ->
                        if (prayer in TRACKABLE_PRAYERS) viewModel.togglePrayerCompleted(prayer)
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tamamlanma ilerleme çubuğu
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CompletionProgress(completed: Int, total: Int) {
    val progress = completed.toFloat() / total.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600),
        label = "prayer_progress"
    )

    Column {
        val strings = LocalStrings.current
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = strings.todaysPrayers,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Text(
                text = "$completed / $total",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (completed == total) Color(0xFF2E7D32)
                        else MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = if (completed == total) Color(0xFF2E7D32)
                    else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Namaz listesi
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PrayerList(
    prayerTime: PrayerTime,
    currentPrayer: Prayer?,
    completedPrayers: Set<String>,
    onToggleCompleted: (Prayer) -> Unit
) {
    val prayers = Prayer.entries.toList()

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(prayers) { prayer ->
            PrayerRow(
                prayer = prayer,
                time = prayerTime.timeFor(prayer).cleanTime(),
                isCurrent = prayer == currentPrayer,
                isCompleted = prayer.name in completedPrayers,
                isTrackable = prayer in TRACKABLE_PRAYERS,
                onToggleCompleted = { onToggleCompleted(prayer) }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Namaz satırı — tamamlama checkbox'ı ile
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PrayerRow(
    prayer: Prayer,
    time: String,
    isCurrent: Boolean,
    isCompleted: Boolean,
    isTrackable: Boolean,
    onToggleCompleted: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = when {
            isCompleted -> Color(0xFF2E7D32).copy(alpha = 0.12f)
            isCurrent   -> MaterialTheme.colorScheme.primaryContainer
            else        -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(400),
        label = "prayer_row_color"
    )

    // ADM-style: 8dp radius, thin border on non-active, no shadow
    val borderColor = when {
        isCompleted -> Color(0xFF2E7D32).copy(alpha = 0.3f)
        isCurrent   -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        else        -> MaterialTheme.colorScheme.outlineVariant
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            ),
        shape  = RoundedCornerShape(8.dp),
        color  = containerColor,
        tonalElevation = if (isCurrent) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Tamamlama butonu (sadece kılınabilir namazlar için)
                if (isTrackable) {
                    CompletionCheckbox(
                        isCompleted = isCompleted,
                        onClick = onToggleCompleted
                    )
                    Spacer(Modifier.width(12.dp))
                } else {
                    Spacer(Modifier.width(4.dp))
                }

                Column {
                    val strings = LocalStrings.current
                    Text(
                        text = strings.prayerName(prayer),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isCurrent || isCompleted) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            isCompleted -> Color(0xFF1B5E20)
                            isCurrent   -> MaterialTheme.colorScheme.onPrimaryContainer
                            else        -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Text(
                        text = prayer.arabicName,
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            isCompleted -> Color(0xFF2E7D32).copy(alpha = 0.7f)
                            isCurrent   -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            else        -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        }
                    )
                }
            }

            Text(
                text = time,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = when {
                    isCompleted -> Color(0xFF2E7D32)
                    isCurrent   -> MaterialTheme.colorScheme.primary
                    else        -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tamamlama checkbox'ı — animasyonlu
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CompletionCheckbox(
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isCompleted) 1f else 0.9f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "checkbox_scale"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isCompleted) Color(0xFF2E7D32) else Color.Transparent,
        animationSpec = tween(300),
        label = "checkbox_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isCompleted) Color(0xFF2E7D32)
                      else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        animationSpec = tween(300),
        label = "checkbox_border"
    )

    Box(
        modifier = Modifier
            .size(26.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (!isCompleted) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent),
            )
            // Dış çerçeve
            androidx.compose.foundation.Canvas(Modifier.size(26.dp)) {
                drawCircle(
                    color = borderColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                )
            }
        }
        if (isCompleted) {
            val strings = LocalStrings.current
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = strings.completed,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
