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
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.islam.R
import com.example.islam.core.i18n.LocalStrings
import com.example.islam.core.navigation.Screen
import com.example.islam.domain.model.DailyQuote
import com.example.islam.domain.model.Prayer
import com.example.islam.domain.model.PrayerTime
import com.example.islam.domain.model.QuoteType
import com.example.islam.domain.model.timeFor
import com.example.islam.core.util.DateUtil.cleanTime

// ─────────────────────────────────────────────────────────────────────────────
// İçerik durumları
// ─────────────────────────────────────────────────────────────────────────────

private sealed class HomeContentState {
    object Loading                        : HomeContentState()
    data class Error(val message: String) : HomeContentState()
    object Success                        : HomeContentState()
}

private enum class PermissionStep { LOCATION, NOTIFICATION, EXACT_ALARM, DONE }

// ─────────────────────────────────────────────────────────────────────────────
// Ana ekran
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (!state.permissionsGranted) {
        HomePermissionFlow(onAllGranted = viewModel::onPermissionsGranted)
        return
    }

    // ── ViewModel state'inden görünür değerleri hazırla ──────────────────────
    val prayerNameStr = state.nextPrayer?.prayer?.turkishName ?: "—"

    val prayerTimeStr = state.prayerTime?.let { pt ->
        state.nextPrayer?.let { np ->
            pt.timeFor(np.prayer).cleanTime()
        }
    } ?: "--:--"

    val countdownStr = state.countdownText.let { raw ->
        val parts = raw.split(":")
        if (parts.size == 3) {
            val h = parts[0].trimStart('0').ifEmpty { "0" }
            val m = parts[1]
            val s = parts[2]
            when {
                h != "0" -> "-${h}sa ${m}dk remaining"
                m != "00" -> "-${m}dk ${s}sn remaining"
                else     -> "-${s}sn remaining"
            }
        } else raw
    }

    val gregorianStr = state.todayDateText
    val hijriStr     = state.prayerTime?.hijriDate ?: ""
    val verseText    = state.dailyQuote?.text
        ?.let { "\u201C$it\u201D" }
        ?: "\u201CVerily, in the remembrance of Allah do hearts find rest.\u201D"
    val verseRef     = state.dailyQuote?.source ?: "Quran 13:28"

    // ── Namaz vakitleri — HTML'deki icon + renk eşleşmesiyle ─────────────────
    val activePrayerName = state.nextPrayer?.prayer?.name?.lowercase() ?: ""
    val prayerItems = state.prayerTime?.let { pt ->
        listOf(
            PrayerDisplayItem(
                name     = "Fajr",
                time     = pt.fajr.cleanTime(),
                icon     = androidx.compose.material.icons.Icons.Outlined.DarkMode,
                iconTint = Color.White,
                isActive = activePrayerName == "fajr"
            ),
            PrayerDisplayItem(
                name     = "Dhuhr",
                time     = pt.dhuhr.cleanTime(),
                icon     = androidx.compose.material.icons.Icons.Outlined.WbSunny,
                iconTint = Color(0xFFD4AF37),   // gold
                isActive = activePrayerName == "dhuhr"
            ),
            PrayerDisplayItem(
                name     = "Asr",
                time     = pt.asr.cleanTime(),
                icon     = androidx.compose.material.icons.Icons.Outlined.WbTwilight,
                iconTint = Color(0xFFD4AF37),   // gold
                isActive = activePrayerName == "asr"
            ),
            PrayerDisplayItem(
                name     = "Maghrib",
                time     = pt.maghrib.cleanTime(),
                icon     = androidx.compose.material.icons.Icons.Outlined.WbTwilight,
                iconTint = Color(0xFFFB923C),   // text-orange-400
                isActive = activePrayerName == "maghrib"
            ),
            PrayerDisplayItem(
                name     = "Isha",
                time     = pt.isha.cleanTime(),
                icon     = androidx.compose.material.icons.Icons.Outlined.Brightness2,
                iconTint = Color(0xFFA5B4FC),   // text-indigo-300
                isActive = activePrayerName == "isha"
            )
        )
    } ?: defaultPrayerItems()

    // ── DawnHomeScreen'i gerçek verilerle göster ─────────────────────────────
    DawnHomeScreen(
        prayerName      = prayerNameStr,
        time            = prayerTimeStr,
        countdown       = countdownStr,
        gregorianDate   = gregorianStr,
        hijriDate       = hijriStr,
        verseText       = verseText,
        verseRef        = verseRef,
        prayerItems     = prayerItems,
        onQiblaClick    = { navController.navigate(Screen.Qibla.route) },
        onTasbihClick   = { navController.navigate(Screen.Dhikr.route) },
        onHomeClick     = { /* zaten buradasın */ },
        onQuranClick    = { /* Kuran sayfası eklenince */ },
        onPrayersClick  = { navController.navigate(Screen.PrayerTimes.route) },
        onSettingsClick = { navController.navigate(Screen.Settings.route) },
        activeTab       = DawnTab.HOME
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Top App Bar
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(onSettingsClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text       = "Islam",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary
            )
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector        = Icons.Outlined.Settings,
                    contentDescription = "Ayarlar",
                    tint               = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Konum + Hava Durumu satırı
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LocationWeatherRow(city: String, country: String) {
    Row(
        modifier            = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment   = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector        = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text      = if (city.isNotBlank()) "$city, $country" else country,
                style     = MaterialTheme.typography.bodyMedium,
                fontWeight= FontWeight.SemiBold,
                color     = MaterialTheme.colorScheme.onBackground,
                maxLines  = 1,
                overflow  = TextOverflow.Ellipsis
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector        = Icons.Outlined.Cloud,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier           = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text  = "—°",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tarih satırı — Miladi (sol) | Hicri (sağ)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DateRow(gregorianDate: String, hijriDate: String?) {
    Row(
        modifier            = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text  = gregorianDate,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
        )
        if (!hijriDate.isNullOrBlank()) {
            Text(
                text  = hijriDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Ramazan Banner
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RamadanBanner(days: Int) {
    val green = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(green, green.copy(green = (green.green * 1.15f).coerceIn(0f, 1f)))
                )
            )
    ) {
        // arka plan desen overlay
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .alpha(0.07f)
        ) { drawIslamicPattern(this) }

        Row(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🌙", fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text       = "Ramazan Ayı Başlangıcı",
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White
                )
            }
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Text(
                    text      = "$days gün sonra",
                    modifier  = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style     = MaterialTheme.typography.labelMedium,
                    fontWeight= FontWeight.Bold,
                    color     = Color.White
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Namaz sayaç kartı — karanlık arka plan + cami deseni + 6 vakit satırı
// ─────────────────────────────────────────────────────────────────────────────

private val CardDark = Color(0xFF2C2A1F)

@Composable
private fun PrayerCountdownCard(
    prayerName : String,
    countdown  : String,
    prayerTime : PrayerTime?,
    nextPrayer : Prayer
) {
    val strings = LocalStrings.current

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardDark)
        ) {
            // Cami deseni
            IslamicPatternOverlay()

            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Label
                Text(
                    text  = "$prayerName ${strings.remainingTime}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f)
                )

                Spacer(Modifier.height(6.dp))

                // Geri sayım
                AnimatedCountdown(countdown = countdown)

                // 6 namaz vakti satırı
                prayerTime?.let {
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                    Spacer(Modifier.height(12.dp))
                    PrayerTimesRow(prayerTime = it, nextPrayer = nextPrayer)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 6 Namaz vakti satırı (kart içi)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PrayerTimesRow(prayerTime: PrayerTime, nextPrayer: Prayer) {
    val strings = LocalStrings.current
    val prayers = listOf(
        Prayer.IMSAK, Prayer.FAJR, Prayer.DHUHR,
        Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA
    )

    Row(
        modifier            = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        prayers.forEach { prayer ->
            val isNext    = prayer == nextPrayer
            val nameColor = if (isNext) MaterialTheme.colorScheme.secondary else Color.White.copy(alpha = 0.6f)
            val timeColor = if (isNext) Color.White else Color.White.copy(alpha = 0.7f)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text      = strings.prayerName(prayer),
                    style     = MaterialTheme.typography.labelSmall,
                    color     = nameColor,
                    fontWeight= if (isNext) FontWeight.Bold else FontWeight.Normal
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text      = prayerTime.timeFor(prayer).cleanTime(),
                    style     = MaterialTheme.typography.labelMedium,
                    color     = timeColor,
                    fontWeight= if (isNext) FontWeight.Bold else FontWeight.Normal
                )
                if (isNext) {
                    Spacer(Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(2.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = RoundedCornerShape(1.dp)
                            )
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Özellik Grid — Kıble, İmsakiye, Zikirmatik
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FeatureGrid(
    onQiblaClick       : () -> Unit,
    onPrayerTimesClick : () -> Unit,
    onDhikrClick       : () -> Unit
) {
    val strings = LocalStrings.current

    Row(
        modifier            = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FeatureCard(
            modifier    = Modifier.weight(1f),
            iconResId   = R.drawable.kible,
            label       = strings.navQibla,
            onClick     = onQiblaClick
        )
        FeatureCard(
            modifier    = Modifier.weight(1f),
            icon        = Icons.Outlined.AccessTime,
            label       = strings.navPrayerTimes,
            onClick     = onPrayerTimesClick
        )
        FeatureCard(
            modifier    = Modifier.weight(1f),
            icon        = Icons.Outlined.FavoriteBorder,
            label       = strings.navDhikr,
            onClick     = onDhikrClick
        )
    }
}

@Composable
private fun FeatureCard(
    modifier  : Modifier = Modifier,
    icon      : ImageVector? = null,
    iconResId : Int? = null,
    label     : String,
    onClick   : () -> Unit
) {
    // ADM-style: lightweight card — F5F5F5 background, 12dp radius, thin border
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier            = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (iconResId != null) {
                    Image(
                        painter = painterResource(iconResId),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Icon(
                        imageVector        = icon!!,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text      = label,
                style     = MaterialTheme.typography.labelMedium,
                fontWeight= FontWeight.SemiBold,
                color     = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines  = 2,
                overflow  = TextOverflow.Ellipsis
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Günün Ayeti / Hadis kartı
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DailyQuoteCard(quote: DailyQuote) {
    val context = LocalContext.current

    // ADM "notice bar" pattern: white card, left accent border, 8dp radius
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        // Sol vurgu çizgisi (ADM notice bar accent)
        Box(
            modifier = Modifier
                .width(4.dp)
                .heightIn(min = 80.dp)
                .background(MaterialTheme.colorScheme.secondary)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            // Başlık satırı
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.secondary,
                        modifier           = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text       = "Günün Ayeti",
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.secondary
                    )
                }
                IconButton(
                    onClick  = {
                        val shareText = "\"${quote.text}\"\n— ${quote.source}\n\n📿 İslam Uygulaması"
                        val intent    = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(intent, null))
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Share,
                        contentDescription = "Paylaş",
                        tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier           = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text       = "\u201C${quote.text}\u201D",
                style      = MaterialTheme.typography.bodyMedium,
                color      = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text       = "— ${quote.source}",
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                modifier   = Modifier.align(Alignment.End)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Yükleniyor kutusu
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LoadingBox() {
    Box(
        modifier            = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment    = Alignment.Center
    ) {
        CircularProgressIndicator(
            color       = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Staggered giriş animasyonu
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AnimatedEntrance(delayMs: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delayMs.toLong())
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(tween(400)) + slideInVertically(
            animationSpec  = tween(400),
            initialOffsetY = { it / 4 }
        )
    ) { content() }
}

// ─────────────────────────────────────────────────────────────────────────────
// Animasyonlu geri sayım
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AnimatedCountdown(countdown: String) {
    val parts = countdown.split(":")
    if (parts.size != 3) {
        Text(
            text       = countdown,
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color      = Color.White
        )
        return
    }
    Row(
        verticalAlignment   = Alignment.CenterVertically,
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
        targetState  = value,
        transitionSpec = {
            (slideInVertically(tween(250)) { -it / 2 } + fadeIn(tween(250))) togetherWith
            (slideOutVertically(tween(200)) { it / 2 } + fadeOut(tween(200)))
        },
        label = "countdown_unit_$value"
    ) { v ->
        Text(
            text       = v,
            style      = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color      = Color.White
        )
    }
}

@Composable
private fun CountdownSeparator() {
    Text(
        text       = ":",
        style      = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color      = Color.White.copy(alpha = 0.5f),
        modifier   = Modifier.padding(horizontal = 2.dp)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// İslami geometrik desen — düşük opaklıkta 8 köşeli yıldız
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun IslamicPatternOverlay() {
    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .alpha(0.06f)
    ) { drawIslamicPattern(this) }
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
    val path    = androidx.compose.ui.graphics.Path()
    val innerR  = r * 0.42f
    val points  = 8
    for (i in 0 until points * 2) {
        val angle  = Math.PI * i / points - Math.PI / 2
        val radius = if (i % 2 == 0) r else innerR
        val x      = cx + (radius * kotlin.math.cos(angle)).toFloat()
        val y      = cy + (radius * kotlin.math.sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    scope.drawPath(
        path  = path,
        color = Color.White,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Hata kartı
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    val strings        = LocalStrings.current
    val isNetworkError = message.contains("connect", ignoreCase = true) ||
            message.contains("timeout", ignoreCase = true) ||
            message.contains("network", ignoreCase = true) ||
            message.contains("unable", ignoreCase = true) ||
            message.contains("internet", ignoreCase = true)

    val icon     = if (isNetworkError) Icons.Default.WifiOff else Icons.Default.ErrorOutline
    val title    = if (isNetworkError) strings.noInternetTitle else strings.errorTitle
    val subtitle = if (isNetworkError) strings.noInternetDesc else message

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                modifier           = Modifier.size(48.dp),
                tint               = MaterialTheme.colorScheme.error
            )
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onErrorContainer,
                textAlign  = TextAlign.Center
            )
            Text(
                text      = subtitle,
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape   = RoundedCornerShape(12.dp),
                modifier= Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector        = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier           = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text     = strings.retryButton,
                    modifier = Modifier.padding(vertical = 4.dp),
                    fontWeight = FontWeight.SemiBold
                )
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

    LaunchedEffect(step) { if (step == PermissionStep.DONE) onAllGranted() }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) step = nextStepAfterLocation()
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> step = nextStepAfterNotification() }

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
            emoji = "📍", title = strings.locationPermTitle,
            description = strings.locationPermDesc, buttonText = strings.locationPermButton,
            onRequest = {
                locationLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                )
            }
        )
        PermissionStep.NOTIFICATION -> PermissionCard(
            emoji = "🔔", title = strings.notificationPermTitle,
            description = strings.notificationPermDesc, buttonText = strings.notificationPermButton,
            onRequest = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                else step = nextStepAfterNotification()
            }
        )
        PermissionStep.EXACT_ALARM -> PermissionCard(
            emoji = "⏰", title = strings.exactAlarmPermTitle,
            description = strings.exactAlarmPermDesc, buttonText = strings.exactAlarmPermButton,
            onRequest = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    alarmSettingsLauncher.launch(Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                else step = PermissionStep.DONE
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
    emoji: String, title: String, description: String,
    buttonText: String, onRequest: () -> Unit
) {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier            = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = emoji, style = MaterialTheme.typography.displayMedium)
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center
                )
                Text(
                    text      = description,
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick  = onRequest,
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp)
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

private fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED ||
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED

private fun Context.hasNotificationPermission(): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
    else true

private fun Context.canScheduleExactAlarms(): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        (getSystemService(AlarmManager::class.java)).canScheduleExactAlarms()
    else true
