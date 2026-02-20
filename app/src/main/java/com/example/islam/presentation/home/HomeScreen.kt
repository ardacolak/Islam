package com.example.islam.presentation.home

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.islam.domain.model.DailyQuote
import com.example.islam.domain.model.Prayer
import com.example.islam.domain.model.PrayerTime
import com.example.islam.domain.model.QuoteType
import com.example.islam.domain.model.timeFor
import com.example.islam.core.util.DateUtil.cleanTime

// ─────────────────────────────────────────────────────────────────────────────
// Ana içerik durumları (AnimatedContent için)
// ─────────────────────────────────────────────────────────────────────────────

private sealed class HomeContentState {
    object Loading                       : HomeContentState()
    data class Error(val message: String): HomeContentState()
    object Success                       : HomeContentState()
}

// ─────────────────────────────────────────────────────────────────────────────
// İzin akışı adımları
// ─────────────────────────────────────────────────────────────────────────────

private enum class PermissionStep {
    LOCATION, NOTIFICATION, EXACT_ALARM, DONE
}

// ─────────────────────────────────────────────────────────────────────────────
// Ana ekran
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    // İzinler henüz verilmemişse izin akışını göster
    if (!state.permissionsGranted) {
        HomePermissionFlow(onAllGranted = viewModel::onPermissionsGranted)
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Spacer(Modifier.height(16.dp))
        Text(
            text = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = state.todayDateText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(Modifier.height(8.dp))

        // Hijri date
        state.prayerTime?.hijriDate?.let { hijri ->
            Text(
                text = hijri,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Günlük Ayet / Hadis ───────────────────────────────────────────────
        state.dailyQuote?.let { DailyQuoteCard(it) }

        Spacer(Modifier.height(16.dp))

        // ── Loading / Error / Content ─────────────────────────────────────────
        // Her durum geçişi soluklanarak animasyonlu şekilde değişir.
        AnimatedContent(
            targetState = when {
                state.isLoading               -> HomeContentState.Loading
                state.error != null           -> HomeContentState.Error(state.error!!)
                state.nextPrayer != null      -> HomeContentState.Success
                else                          -> HomeContentState.Loading
            },
            label = "home_content"
        ) { contentState ->
            when (contentState) {
                is HomeContentState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                    }
                }
                is HomeContentState.Error -> {
                    ErrorCard(message = contentState.message, onRetry = viewModel::refresh)
                }
                is HomeContentState.Success -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        NextPrayerCard(
                            prayerName = state.nextPrayer!!.prayer.turkishName,
                            arabicName = state.nextPrayer!!.prayer.arabicName,
                            prayerTime = state.nextPrayer!!.timeString,
                            countdown  = state.countdownText
                        )
                        Spacer(Modifier.height(24.dp))
                        state.prayerTime?.let { PrayerSummaryCard(it) }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Günlük Ayet / Hadis kartı
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DailyQuoteCard(quote: DailyQuote) {
    val isAyah = quote.type == QuoteType.AYAH

    // Ayet → hafif yeşil tonlar, Hadis → hafif turuncu tonlar
    val containerColor = if (isAyah)
        MaterialTheme.colorScheme.secondaryContainer
    else
        MaterialTheme.colorScheme.tertiaryContainer

    val onContainerColor = if (isAyah)
        MaterialTheme.colorScheme.onSecondaryContainer
    else
        MaterialTheme.colorScheme.onTertiaryContainer

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Tür etiketi: "Ayet" veya "Hadis"
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = onContainerColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = quote.type.label,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = onContainerColor.copy(alpha = 0.8f)
                )
            }

            Spacer(Modifier.height(10.dp))

            // Ayet / Hadis metni
            Text(
                text = "\u201C${quote.text}\u201D",   // "…"
                style = MaterialTheme.typography.bodyMedium,
                color = onContainerColor,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(8.dp))

            // Kaynak
            Text(
                text = "— ${quote.source}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = onContainerColor.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sonraki namaz kartı
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NextPrayerCard(
    prayerName: String,
    arabicName: String,
    prayerTime: String,
    countdown: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sonraki Namaz",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = arabicName,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = prayerName,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = prayerTime,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f))
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Kalan Süre",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
            )
            Text(
                text = countdown,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bugünün vakitleri kartı
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PrayerSummaryCard(prayerTime: PrayerTime) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Bugünün Vakitleri",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            listOf(
                Prayer.IMSAK, Prayer.FAJR, Prayer.DHUHR,
                Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA
            ).forEach { prayer ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = prayer.turkishName,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = prayerTime.timeFor(prayer).cleanTime(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (prayer != Prayer.ISHA) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// İzin akışı — Sıralı: Konum → Bildirim → Tam Alarm → Yükle
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Üç adımlı sıralı izin ekranı.
 * Her adım tamamlanınca bir sonrakine geçer; hepsi tamamlanınca [onAllGranted] çağrılır.
 *
 * | Adım           | İzin                           | API |
 * |----------------|--------------------------------|-----|
 * | LOCATION       | ACCESS_FINE/COARSE_LOCATION    | tüm |
 * | NOTIFICATION   | POST_NOTIFICATIONS             | 33+ |
 * | EXACT_ALARM    | SCHEDULE_EXACT_ALARM (Ayarlar) | 31+ |
 */
@Composable
private fun HomePermissionFlow(onAllGranted: () -> Unit) {
    val context = LocalContext.current

    // İlk adımı belirle
    var step by remember {
        val initial = when {
            !context.hasLocationPermission() -> PermissionStep.LOCATION
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !context.hasNotificationPermission() -> PermissionStep.NOTIFICATION
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    !context.canScheduleExactAlarms() -> PermissionStep.EXACT_ALARM
            else -> PermissionStep.DONE
        }
        mutableStateOf(initial)
    }

    // DONE'a gelindiğinde ViewModel'ı bilgilendir
    LaunchedEffect(step) {
        if (step == PermissionStep.DONE) onAllGranted()
    }

    // ── Launcher'lar ──────────────────────────────────────────────────────────

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) step = nextStepAfterLocation()
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Reddedilse de devam et — bildirim zorunlu değil, alarm daha kritik
        step = nextStepAfterNotification()
    }

    val alarmSettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Settings'den döndükten sonra tekrar kontrol et
        step = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !(context.getSystemService(AlarmManager::class.java)).canScheduleExactAlarms()
        ) PermissionStep.EXACT_ALARM else PermissionStep.DONE
    }

    // ── Adım UI'ları ──────────────────────────────────────────────────────────

    when (step) {
        PermissionStep.LOCATION -> PermissionCard(
            emoji = "📍",
            title = "Konum İzni Gerekli",
            description = "Namaz vakitlerini hesaplayabilmek ve kıble yönünü belirleyebilmek için " +
                    "cihazınızın konumuna ihtiyaç duyulmaktadır.",
            buttonText = "Konuma İzin Ver",
            onRequest = {
                locationLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        )

        PermissionStep.NOTIFICATION -> PermissionCard(
            emoji = "🔔",
            title = "Bildirim İzni",
            description = "Ezan vakitlerinde bildirim alabilmek için bildirim iznine ihtiyaç vardır.",
            buttonText = "Bildirimlere İzin Ver",
            onRequest = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    step = nextStepAfterNotification()
                }
            }
        )

        PermissionStep.EXACT_ALARM -> PermissionCard(
            emoji = "⏰",
            title = "Tam Alarm İzni",
            description = "Ezan vakitlerinde tam zamanında bildirim verebilmek için " +
                    "Ayarlar → Alarmlar & Hatırlatıcılar bölümünden izin vermeniz gerekiyor.",
            buttonText = "Ayarlara Git",
            onRequest = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    alarmSettingsLauncher.launch(
                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    )
                } else {
                    step = PermissionStep.DONE
                }
            }
        )

        PermissionStep.DONE -> Unit // LaunchedEffect handle eder
    }
}

/** Konum izninden sonraki adımı döndürür. */
private fun nextStepAfterLocation(): PermissionStep = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> PermissionStep.NOTIFICATION
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S        -> PermissionStep.EXACT_ALARM
    else                                                   -> PermissionStep.DONE
}

/** Bildirim izninden sonraki adımı döndürür. */
private fun nextStepAfterNotification(): PermissionStep = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> PermissionStep.EXACT_ALARM
    else                                            -> PermissionStep.DONE
}

// ─────────────────────────────────────────────────────────────────────────────
// Yeniden kullanılabilir izin kartı
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PermissionCard(
    emoji: String,
    title: String,
    description: String,
    buttonText: String,
    onRequest: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = emoji, style = MaterialTheme.typography.displayMedium)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onRequest,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(buttonText, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Context uzantı fonksiyonları (ekran içi kullanım)
// ─────────────────────────────────────────────────────────────────────────────

private fun android.content.Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED ||
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED

private fun android.content.Context.hasNotificationPermission(): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
    else true

private fun android.content.Context.canScheduleExactAlarms(): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        (getSystemService(AlarmManager::class.java)).canScheduleExactAlarms()
    else true

// ─────────────────────────────────────────────────────────────────────────────
// Hata kartı
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    // İnternet hatası olup olmadığını basitçe tespit et
    val isNetworkError = message.contains("connect", ignoreCase = true) ||
            message.contains("timeout", ignoreCase = true) ||
            message.contains("network", ignoreCase = true) ||
            message.contains("unable", ignoreCase = true) ||
            message.contains("internet", ignoreCase = true)

    val icon = if (isNetworkError) Icons.Default.WifiOff else Icons.Default.ErrorOutline
    val title = if (isNetworkError) "İnternet Bağlantısı Yok" else "Bir Hata Oluştu"
    val subtitle = if (isNetworkError)
        "Namaz vakitleri yüklenemiyor. Bağlantınızı kontrol edip tekrar deneyin."
    else
        message

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Tekrar Dene",
                    modifier = Modifier.padding(vertical = 4.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
