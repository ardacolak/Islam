package com.example.islam.presentation.home

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.islam.core.i18n.LocalStrings
import com.example.islam.domain.model.DailyQuote
import com.example.islam.domain.model.Prayer
import com.example.islam.domain.model.PrayerTime
import com.example.islam.domain.model.QuoteType
import com.example.islam.domain.model.timeFor
import com.example.islam.core.util.DateUtil.cleanTime
import kotlinx.coroutines.launch

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

    if (!state.permissionsGranted) {
        HomePermissionFlow(onAllGranted = viewModel::onPermissionsGranted)
        return
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        // Staggered giriş: Her kart biraz gecikmeli fade+slide ile gelir
        AnimatedEntrance(delayMs = 0) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                state.prayerTime?.hijriDate?.let { hijri ->
                    Text(
                        text = hijri,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Streak kartı
        AnimatedEntrance(delayMs = 80) {
            StreakCard(streak = state.prayerStreak)
        }

        Spacer(Modifier.height(12.dp))

        // Günlük Ayet / Hadis
        AnimatedEntrance(delayMs = 160) {
            state.dailyQuote?.let { DailyQuoteCard(it) }
        }

        Spacer(Modifier.height(16.dp))

        // Loading / Error / Content
        AnimatedContent(
            targetState = when {
                state.isLoading          -> HomeContentState.Loading
                state.error != null      -> HomeContentState.Error(state.error!!)
                state.nextPrayer != null -> HomeContentState.Success
                else                     -> HomeContentState.Loading
            },
            label = "home_content",
            transitionSpec = {
                fadeIn(tween(300)) togetherWith fadeOut(tween(200))
            }
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
                        AnimatedEntrance(delayMs = 240) {
                            NextPrayerCard(
                                prayerName = state.nextPrayer!!.prayer.turkishName,
                                arabicName = state.nextPrayer!!.prayer.arabicName,
                                prayerTime = state.nextPrayer!!.timeString,
                                countdown  = state.countdownText
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        AnimatedEntrance(delayMs = 320) {
                            state.prayerTime?.let { PrayerSummaryCard(it) }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Staggered giriş animasyonu — fade + yukarı kayma
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AnimatedEntrance(
    delayMs: Int,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delayMs.toLong())
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically(
            animationSpec = tween(400),
            initialOffsetY = { it / 4 }
        )
    ) {
        content()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Streak kartı
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StreakCard(streak: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
        val strings = LocalStrings.current
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = if (streak > 0) Color(0xFFFF6D00) else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.4f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = strings.streakDays.format(streak),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = if (streak == 0) strings.streakComplete else strings.streakCongrats,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }
            Text(
                text = if (streak > 0) "🔥" else "💪",
                fontSize = 24.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Günlük Ayet / Hadis kartı
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DailyQuoteCard(quote: DailyQuote) {
    val context = LocalContext.current
    val isAyah = quote.type == QuoteType.AYAH

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                // Paylaş butonu
                IconButton(
                    onClick = {
                        val shareText = "\"${quote.text}\"\n— ${quote.source}\n\n📿 İslam Uygulaması"
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, null))
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Paylaş",
                        tint = onContainerColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = "\u201C${quote.text}\u201D",
                style = MaterialTheme.typography.bodyMedium,
                color = onContainerColor,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(8.dp))

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
// Sonraki namaz kartı — gradient arka plan + animasyonlu geri sayım
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NextPrayerCard(
    prayerName: String,
    arabicName: String,
    prayerTime: String,
    countdown: String
) {
    val primary = MaterialTheme.colorScheme.primary
    val primaryVariant = MaterialTheme.colorScheme.primaryContainer

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            primary,
                            primary.copy(red = (primary.red * 0.85f).coerceIn(0f, 1f),
                                         green = (primary.green * 1.1f).coerceIn(0f, 1f),
                                         blue = (primary.blue * 0.85f).coerceIn(0f, 1f))
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            // İslami geometrik desen overlay
            IslamicPatternOverlay()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val strings = LocalStrings.current
                Text(
                    text = strings.nextPrayer,
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
                    text = strings.remainingTime,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(4.dp))
                // Animasyonlu geri sayım — her saniye rakam yukarı kayarak değişir
                AnimatedCountdown(countdown = countdown)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Animasyonlu geri sayım — her karakter değişince slide + fade
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AnimatedCountdown(countdown: String) {
    // Geri sayım "HH:MM:SS" formatında; her bloğu (saat, dakika, saniye) ayrı animate et
    val parts = countdown.split(":")
    if (parts.size != 3) {
        Text(
            text = countdown,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
        return
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CountdownUnit(value = parts[0])
        CountdownSeparator()
        CountdownUnit(value = parts[1])
        CountdownSeparator()
        CountdownUnit(value = parts[2])
    }
}

@Composable
private fun CountdownUnit(value: String) {
    AnimatedContent(
        targetState = value,
        transitionSpec = {
            (slideInVertically(tween(250)) { -it / 2 } + fadeIn(tween(250))) togetherWith
            (slideOutVertically(tween(200)) { it / 2 } + fadeOut(tween(200)))
        },
        label = "countdown_unit_$value"
    ) { v ->
        Text(
            text = v,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun CountdownSeparator() {
    Text(
        text = ":",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
        modifier = Modifier.padding(horizontal = 2.dp)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// İslami geometrik desen overlay — düşük opaklıkta 8 köşeli yıldız
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun IslamicPatternOverlay() {
    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .alpha(0.06f)
    ) {
        drawIslamicPattern(this)
    }
}

private fun drawIslamicPattern(scope: DrawScope) {
    val tileSize = 60f
    val cols = (scope.size.width / tileSize).toInt() + 2
    val rows = (scope.size.height / tileSize).toInt() + 2

    for (row in -1..rows) {
        for (col in -1..cols) {
            val cx = col * tileSize + if (row % 2 == 0) 0f else tileSize / 2f
            val cy = row * tileSize * 0.866f
            drawEightPointStar(scope, cx, cy, tileSize * 0.35f)
        }
    }
}

private fun drawEightPointStar(scope: DrawScope, cx: Float, cy: Float, r: Float) {
    val path = androidx.compose.ui.graphics.Path()
    val innerR = r * 0.42f
    val points = 8
    for (i in 0 until points * 2) {
        val angle = Math.PI * i / points - Math.PI / 2
        val radius = if (i % 2 == 0) r else innerR
        val x = cx + (radius * kotlin.math.cos(angle)).toFloat()
        val y = cy + (radius * kotlin.math.sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    scope.drawPath(path, color = Color.White,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f))
}

// ─────────────────────────────────────────────────────────────────────────────
// Bugünün vakitleri kartı
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PrayerSummaryCard(prayerTime: PrayerTime) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        val strings = LocalStrings.current
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = strings.todaysTimes,
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
                        text = strings.prayerName(prayer),
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
// İzin akışı
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HomePermissionFlow(onAllGranted: () -> Unit) {
    val context = LocalContext.current

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

    LaunchedEffect(step) {
        if (step == PermissionStep.DONE) onAllGranted()
    }

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
        step = nextStepAfterNotification()
    }

    val alarmSettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        step = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !(context.getSystemService(AlarmManager::class.java)).canScheduleExactAlarms()
        ) PermissionStep.EXACT_ALARM else PermissionStep.DONE
    }

    val strings = LocalStrings.current
    when (step) {
        PermissionStep.LOCATION -> PermissionCard(
            emoji = "📍",
            title = strings.locationPermTitle,
            description = strings.locationPermDesc,
            buttonText = strings.locationPermButton,
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
            title = strings.notificationPermTitle,
            description = strings.notificationPermDesc,
            buttonText = strings.notificationPermButton,
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
            title = strings.exactAlarmPermTitle,
            description = strings.exactAlarmPermDesc,
            buttonText = strings.exactAlarmPermButton,
            onRequest = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    alarmSettingsLauncher.launch(
                        Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    )
                } else {
                    step = PermissionStep.DONE
                }
            }
        )
        PermissionStep.DONE -> Unit
    }
}

private fun nextStepAfterLocation(): PermissionStep = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> PermissionStep.NOTIFICATION
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S        -> PermissionStep.EXACT_ALARM
    else                                                   -> PermissionStep.DONE
}

private fun nextStepAfterNotification(): PermissionStep = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> PermissionStep.EXACT_ALARM
    else                                            -> PermissionStep.DONE
}

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
// Context uzantı fonksiyonları
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
    val strings = LocalStrings.current
    val isNetworkError = message.contains("connect", ignoreCase = true) ||
            message.contains("timeout", ignoreCase = true) ||
            message.contains("network", ignoreCase = true) ||
            message.contains("unable", ignoreCase = true) ||
            message.contains("internet", ignoreCase = true)

    val icon = if (isNetworkError) Icons.Default.WifiOff else Icons.Default.ErrorOutline
    val title = if (isNetworkError) strings.noInternetTitle else strings.errorTitle
    val subtitle = if (isNetworkError) strings.noInternetDesc else message

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
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
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(text = "Tekrar Dene", modifier = Modifier.padding(vertical = 4.dp), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
