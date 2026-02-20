package com.example.islam.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.islam.R

// ─────────────────────────────────────────────────────────────────────────────
// Renk sabitleri — HTML'deki Tailwind config'den birebir
// ─────────────────────────────────────────────────────────────────────────────
private val Gold            = Color(0xFFD4AF37)  // primary: #D4AF37
private val GlassDark60     = Color(0x99000000)  // bg-glass-dark/60  (0,0,0 at 60%)
private val GlassDark80     = Color(0xCC000000)  // bg-glass-dark/80  (0,0,0 at 80%)
private val BorderFaint     = Color(0x26FFFFFF)  // border-white/10 (15% beyaz)
private val Divider10       = Color(0x1AFFFFFF)  // bg-white/10 divider
private val TextWhite       = Color.White
private val TextWhite90     = Color(0xE6FFFFFF)  // opacity-90
private val TextWhite75     = Color(0xBFFFFFFF)  // opacity-75
private val TextWhite70     = Color(0xB3FFFFFF)  // opacity-70
private val TextWhite60     = Color(0x99FFFFFF)  // opacity-60 (inactive prayer)
private val TextWhite80     = Color(0xCCFFFFFF)  // opacity-80

// Namaz ikonu renkleri (HTML'den)
private val ColorGold       = Color(0xFFD4AF37)  // text-primary (Dhuhr, Asr)
private val ColorOrange     = Color(0xFFFB923C)  // text-orange-400 (Maghrib)
private val ColorIndigo     = Color(0xFFA5B4FC)  // text-indigo-300 (Isha)

// ─────────────────────────────────────────────────────────────────────────────
// Veri modeli
// ─────────────────────────────────────────────────────────────────────────────
data class PrayerDisplayItem(
    val name     : String,
    val time     : String,
    val icon     : ImageVector,
    val iconTint : Color,
    val isActive : Boolean = false
)

// ─────────────────────────────────────────────────────────────────────────────
// DawnHomeScreen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun DawnHomeScreen(
    prayerName      : String = "Fajr",
    time            : String = "04:32 AM",
    countdown       : String = "-25m remaining",
    gregorianDate   : String = "Monday, 12 Aug",
    hijriDate       : String = "14 Safar 1446",
    verseText       : String = "\"Verily, in the remembrance of Allah do hearts find rest.\"",
    verseRef        : String = "Quran 13:28",
    prayerItems     : List<PrayerDisplayItem> = defaultPrayerItems(),
    onQiblaClick    : () -> Unit = {},
    onTasbihClick   : () -> Unit = {},
    onHomeClick     : () -> Unit = {},
    onQuranClick    : () -> Unit = {},
    onPrayersClick  : () -> Unit = {},
    onSettingsClick : () -> Unit = {},
    activeTab       : DawnTab = DawnTab.HOME
) {
    Box(modifier = Modifier.fillMaxSize()) {

        // ── 1. Arka plan resmi ───────────────────────────────────────────────
        Image(
            painter            = painterResource(id = R.drawable.unnamed),
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize()
        )

        // ── 2. Gradient overlay — HTML: rgba(74,102,128,0.6) → rgba(200,160,150,0.3) → rgba(20,20,30,0.7)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0x994A6680),   // rgba(74,102,128, 0.6)
                            0.5f to Color(0x4DC8A096),   // rgba(200,160,150, 0.3)
                            1.0f to Color(0xB314141E)    // rgba(20,20,30, 0.7)
                        )
                    )
                )
        )

        // ── 3. İçerik — tam ekran Column, nav bar en sonda ─────────────────
        Column(
            modifier            = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── İçerik alanı — yatay padding burada ────────────────────────
            Column(
                modifier            = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 48.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Üst Bölüm ───────────────────────────────────────────────
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text          = gregorianDate,
                        fontSize      = 16.sp,
                        fontWeight    = FontWeight.Medium,
                        color         = TextWhite90,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text          = hijriDate,
                        fontSize      = 14.sp,
                        fontWeight    = FontWeight.Light,
                        color         = TextWhite75,
                        letterSpacing = 0.3.sp
                    )

                    Spacer(Modifier.height(28.dp))

                    Text(
                        text          = prayerName,
                        fontSize      = 60.sp,
                        fontWeight    = FontWeight.Normal,
                        fontFamily    = FontFamily.Serif,
                        color         = TextWhite,
                        letterSpacing = (-1).sp,
                        lineHeight    = 64.sp
                    )
                    Text(
                        text          = time,
                        fontSize      = 36.sp,
                        fontWeight    = FontWeight.Light,
                        color         = TextWhite,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Countdown pill
                Row(
                    modifier              = Modifier
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                        .border(1.dp, BorderFaint, CircleShape)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint               = TextWhite90,
                        modifier           = Modifier.size(14.dp)
                    )
                    Text(
                        text          = countdown,
                        fontSize      = 14.sp,
                        fontWeight    = FontWeight.Medium,
                        color         = TextWhite,
                        letterSpacing = 0.3.sp
                    )
                }

                // ── Orta — ayet bölümü ───────────────────────────────────────
                Spacer(Modifier.weight(1f))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier            = Modifier.widthIn(max = 320.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.FormatQuote,
                        contentDescription = null,
                        tint               = TextWhite80,
                        modifier           = Modifier.size(30.dp)
                    )
                    Text(
                        text       = verseText,
                        fontSize   = 20.sp,
                        fontFamily = FontFamily.Serif,
                        fontStyle  = FontStyle.Italic,
                        fontWeight = FontWeight.Normal,
                        color      = Color(0xF2FFFFFF),
                        textAlign  = TextAlign.Center,
                        lineHeight = 32.sp
                    )
                    Text(
                        text          = verseRef.uppercase(),
                        fontSize      = 12.sp,
                        fontWeight    = FontWeight.Bold,
                        color         = TextWhite70,
                        letterSpacing = 2.5.sp
                    )
                }

                Spacer(Modifier.weight(1f))

                // ── Alt Bölüm — Kıble, Tasbih, Namaz vakitleri ───────────────
                Column(
                    modifier            = Modifier.widthIn(max = 448.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        QiblaButton(
                            onClick  = onQiblaClick,
                            modifier = Modifier.weight(1f)
                        )
                        TasbihButton(
                            onClick  = onTasbihClick,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    PrayerTimesCard(items = prayerItems)
                }
            } // içerik Column sonu

            // ── Nav bar — Column'un en altına yapışık ──────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0x1AFFFFFF))
            )
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment     = Alignment.Bottom
            ) {
                NavItem(
                    icon    = Icons.Filled.Home,
                    label   = "Home",
                    active  = activeTab == DawnTab.HOME,
                    onClick = onHomeClick
                )
                NavItem(
                    icon    = Icons.Outlined.MenuBook,
                    label   = "Quran",
                    active  = activeTab == DawnTab.QURAN,
                    onClick = onQuranClick
                )
                NavItem(
                    icon    = Icons.Outlined.Schedule,
                    label   = "Prayers",
                    active  = activeTab == DawnTab.PRAYERS,
                    onClick = onPrayersClick
                )
                NavItem(
                    icon    = Icons.Outlined.Settings,
                    label   = "Settings",
                    active  = activeTab == DawnTab.SETTINGS,
                    onClick = onSettingsClick
                )
            }
        } // dış Column sonu
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Kıble Butonu
// HTML: bg-glass-dark/60 rounded-2xl | circle icon w-6 h-6 border-2 border-primary
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun QiblaButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier            = modifier
            .clip(RoundedCornerShape(16.dp))            // rounded-2xl
            .background(GlassDark60)
            .border(1.dp, BorderFaint, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = ripple(color = Gold)
            ) { onClick() }
            .padding(vertical = 16.dp, horizontal = 24.dp),  // py-4 px-6
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // w-6 h-6 rounded-full border-2 border-primary + inner dot
        Box(
            modifier         = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .border(2.dp, Gold, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Gold)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text       = "Qibla",
            fontSize   = 18.sp,          // text-lg
            fontWeight = FontWeight.Medium,
            color      = TextWhite
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tasbih Butonu
// HTML: bg-glass-dark/60 rounded-2xl | crop_square icon text-primary
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TasbihButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier            = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GlassDark60)
            .border(1.dp, BorderFaint, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = ripple(color = Gold)
            ) { onClick() }
            .padding(vertical = 16.dp, horizontal = 24.dp),
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector        = Icons.Outlined.CropSquare,  // crop_square
            contentDescription = "Tasbih",
            tint               = Gold,
            modifier           = Modifier.size(24.dp)        // text-2xl
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text       = "Tasbih",
            fontSize   = 18.sp,
            fontWeight = FontWeight.Medium,
            color      = TextWhite
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Namaz Vakitleri Kartı
// HTML: bg-glass-dark/80 rounded-3xl p-1 | flex justify-between | w-px h-8 dividers
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun PrayerTimesCard(
    items    : List<PrayerDisplayItem>,
    modifier : Modifier = Modifier
) {
    Row(
        modifier            = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))          // rounded-3xl
            .background(GlassDark80)
            .border(1.dp, BorderFaint, RoundedCornerShape(24.dp))
            .padding(4.dp)                             // p-1
            .padding(horizontal = 8.dp, vertical = 16.dp),  // px-2 py-4
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        items.forEachIndexed { index, item ->
            // Namaz öğesi
            PrayerItem(item = item, modifier = Modifier.weight(1f))

            // Sağına ince divider (son hariç) — w-px h-8 bg-white/10
            if (index < items.lastIndex) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(Divider10)
                )
            }
        }
    }
}

// Tek namaz öğesi
@Composable
private fun PrayerItem(item: PrayerDisplayItem, modifier: Modifier = Modifier) {
    val textColor = if (item.isActive) TextWhite else TextWhite60

    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)   // space-y-1
    ) {
        // İkon — text-2xl
        Icon(
            imageVector        = item.icon,
            contentDescription = item.name,
            tint               = if (item.isActive) TextWhite else item.iconTint,
            modifier           = Modifier.size(24.dp)
        )
        // İsim — text-sm font-bold (aktif) / font-medium (pasif)
        Text(
            text       = item.name,
            fontSize   = 14.sp,
            fontWeight = if (item.isActive) FontWeight.Bold else FontWeight.Medium,
            color      = textColor,
            textAlign  = TextAlign.Center
        )
        // Saat — text-[10px]
        Text(
            text      = item.time,
            fontSize  = 10.sp,
            fontWeight = FontWeight.Normal,
            color     = textColor,
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Nav Bar Öğesi
// HTML: flex-col items-center space-y-1 | active=text-white, inactive=text-white/60
// ─────────────────────────────────────────────────────────────────────────────
enum class DawnTab { HOME, QURAN, PRAYERS, SETTINGS }

@Composable
private fun NavItem(
    icon    : ImageVector,
    label   : String,
    active  : Boolean,
    onClick : () -> Unit
) {
    Column(
        modifier            = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = ripple(bounded = true, color = Color.White)
            ) { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = label,
            tint               = if (active) TextWhite else TextWhite60,
            modifier           = Modifier.size(24.dp)
        )
        Text(
            text          = label,
            fontSize      = 10.sp,
            fontWeight    = if (active) FontWeight.Medium else FontWeight.Normal,
            color         = if (active) TextWhite90 else TextWhite60,
            letterSpacing = 0.2.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Varsayılan namaz öğeleri
// HTML icon sırası: dark_mode | wb_sunny | wb_twilight | wb_twilight(orange) | nights_stay
// ─────────────────────────────────────────────────────────────────────────────
fun defaultPrayerItems() = listOf(
    PrayerDisplayItem(
        name     = "Fajr",
        time     = "04:32 AM",
        icon     = Icons.Outlined.DarkMode,      // dark_mode
        iconTint = TextWhite,
        isActive = true
    ),
    PrayerDisplayItem(
        name     = "Dhuhr",
        time     = "00:30 AM",
        icon     = Icons.Outlined.WbSunny,       // wb_sunny (gold)
        iconTint = ColorGold,
        isActive = false
    ),
    PrayerDisplayItem(
        name     = "Asr",
        time     = "04:13 AM",
        icon     = Icons.Outlined.WbTwilight,    // wb_twilight (gold)
        iconTint = ColorGold,
        isActive = false
    ),
    PrayerDisplayItem(
        name     = "Maghrib",
        time     = "05:00 AM",
        icon     = Icons.Outlined.WbTwilight,    // wb_twilight (orange)
        iconTint = ColorOrange,
        isActive = false
    ),
    PrayerDisplayItem(
        name     = "Isha",
        time     = "12:27 AM",
        icon     = Icons.Outlined.Brightness2,   // nights_stay → Brightness2 (indigo)
        iconTint = ColorIndigo,
        isActive = false
    )
)

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────
@Preview(
    name           = "Dawn Prayer Screen",
    showBackground = true,
    widthDp        = 390,
    heightDp       = 844
)
@Composable
private fun DawnHomeScreenPreview() {
    DawnHomeScreen()
}
