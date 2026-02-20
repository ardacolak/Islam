package com.example.islam.presentation.dhikr

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.islam.domain.model.Dhikr

@Composable
fun DhikrScreen(viewModel: DhikrViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Zikirmatik",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        // Dhikr selector (horizontal scroll)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
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

        Spacer(Modifier.height(32.dp))

        // Selected dhikr display
        state.selectedDhikr?.let { dhikr ->
            DhikrCounter(
                dhikr = dhikr,
                onIncrement = viewModel::increment,
                onReset = viewModel::reset
            )
        }
    }
}

@Composable
private fun DhikrChip(dhikr: Dhikr, isSelected: Boolean, onClick: () -> Unit) {
    val containerColor = if (isSelected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.surfaceVariant

    Surface(
        shape = RoundedCornerShape(50),
        color = containerColor,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = dhikr.name,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Tesbihat dönüm noktaları: bu sayılara ulaşınca ekstra titreşim verilir. */
private val MILESTONE_COUNTS = setOf(33, 66, 99)

@Composable
private fun DhikrCounter(
    dhikr: Dhikr,
    onIncrement: () -> Unit,
    onReset: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val progress = (dhikr.count.toFloat() / dhikr.targetCount.toFloat()).coerceIn(0f, 1f)

    // Arabic text
    Text(
        text = dhikr.arabicText,
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
    )

    Spacer(Modifier.height(8.dp))

    Text(
        text = dhikr.meaning,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(32.dp))

    // Progress ring with count
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(180.dp),
            strokeWidth = 10.dp,
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${dhikr.count}",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "/ ${dhikr.targetCount}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }

    Spacer(Modifier.height(40.dp))

    // Increment button (large circle)
    Button(
        onClick = {
            onIncrement()
            // Yeni count değeri increment'ten SONRA dhikr.count + 1 olacak;
            // state güncellemesi async olduğundan mevcut count + 1 ile kontrol ediyoruz.
            val nextCount = dhikr.count + 1
            if (nextCount in MILESTONE_COUNTS) {
                // Tesbihat dönüm noktası: daha belirgin bir titreşim
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            } else {
                // Normal bekleme: hafif kısa titreşim
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        },
        shape = CircleShape,
        modifier = Modifier.size(100.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text(text = "+", fontSize = 36.sp, color = MaterialTheme.colorScheme.onPrimary)
    }

    Spacer(Modifier.height(16.dp))

    // Reset button
    OutlinedButton(onClick = onReset) {
        Icon(Icons.Default.Refresh, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Sıfırla")
    }
}
