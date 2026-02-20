package com.example.islam.presentation.prayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.islam.domain.model.Prayer
import com.example.islam.domain.model.PrayerTime
import com.example.islam.domain.model.timeFor
import com.example.islam.core.util.DateUtil.cleanTime

@Composable
fun PrayerScreen(viewModel: PrayerViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // City Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
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
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(Modifier.height(16.dp))

        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error
                )
                TextButton(onClick = viewModel::refresh) { Text("Yenile") }
            }
            state.prayerTime != null -> {
                PrayerList(
                    prayerTime = state.prayerTime!!,
                    currentPrayer = state.currentPrayer
                )
            }
        }
    }
}

@Composable
private fun PrayerList(prayerTime: PrayerTime, currentPrayer: Prayer?) {
    val prayers = Prayer.entries.toList()

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(prayers) { prayer ->
            PrayerRow(
                prayer = prayer,
                time = prayerTime.timeFor(prayer).cleanTime(),
                isCurrent = prayer == currentPrayer
            )
        }
    }
}

@Composable
private fun PrayerRow(prayer: Prayer, time: String, isCurrent: Boolean) {
    val containerColor = if (isCurrent)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = prayer.turkishName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = prayer.arabicName,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Text(
                text = time,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (isCurrent) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
