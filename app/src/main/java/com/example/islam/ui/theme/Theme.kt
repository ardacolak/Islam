package com.example.islam.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// ─────────────────────────────────────────────────────────────────────────────
// Açık Tema — Yeşil + Krem + Altın
// Gündüz kullanımı için yumuşak, huzurlu tonlar.
// ─────────────────────────────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    // Birincil — İslami Yeşil
    primary             = IslamicGreen40,
    onPrimary           = White,
    primaryContainer    = IslamicGreen90,
    onPrimaryContainer  = IslamicGreen10,

    // İkincil — Altın
    secondary           = Gold40,
    onSecondary         = White,
    secondaryContainer  = Gold90,
    onSecondaryContainer= Gold10,

    // Üçüncül — Firuze (Cami çinileri)
    tertiary            = Teal40,
    onTertiary          = White,
    tertiaryContainer   = Teal90,
    onTertiaryContainer = Teal10,

    // Genel arka plan — hafif yeşilimsi krem
    background          = Cream99,
    onBackground        = Neutral10,

    // Yüzey katmanları (kart, dialog, bottom sheet)
    surface                  = Cream99,
    onSurface                = Neutral10,
    surfaceVariant           = NeutralVariant95,
    onSurfaceVariant         = NeutralVariant30,
    surfaceContainer         = IslamicGreen99,   // kart arka planı
    surfaceContainerLow      = Cream99,
    surfaceContainerHigh     = NeutralVariant95,
    surfaceContainerHighest  = NeutralVariant90,

    // Kenarlık & bölücüler
    outline             = NeutralVariant50,
    outlineVariant      = NeutralVariant90,

    // Hata
    error               = Error40,
    onError             = White,
    errorContainer      = Error90,
    onErrorContainer    = Error10,

    // Özel yüzeyler
    inverseSurface      = Neutral20,
    inverseOnSurface    = Neutral95,
    inversePrimary      = IslamicGreen80,

    scrim               = Neutral10
)

// ─────────────────────────────────────────────────────────────────────────────
// Koyu Tema — Derin yeşil + Sıcak altın + Derin yüzey katmanları
// Gece kullanımı için göz yormayan, premium görünüm.
// ─────────────────────────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    // Birincil — Açık yeşil
    primary             = IslamicGreen80,
    onPrimary           = IslamicGreen20,
    primaryContainer    = IslamicGreen30,
    onPrimaryContainer  = IslamicGreen90,

    // İkincil — Parlak altın
    secondary           = Gold80,
    onSecondary         = Gold20,
    secondaryContainer  = Gold30,
    onSecondaryContainer= Gold90,

    // Üçüncül — Aydınlık firuze
    tertiary            = Teal80,
    onTertiary          = Teal20,
    tertiaryContainer   = Teal20,
    onTertiaryContainer = Teal90,

    // Arka plan — yoğun koyu
    background          = Neutral10,
    onBackground        = Neutral90,

    // Yüzey katmanları — tam sıyah değil, katmanlı derinlik hissi
    surface                  = Neutral12,
    onSurface                = Neutral90,
    surfaceVariant           = NeutralVariant30,
    onSurfaceVariant         = NeutralVariant80,
    surfaceContainer         = Neutral17,   // kart arka planı
    surfaceContainerLow      = Neutral12,
    surfaceContainerHigh     = Neutral22,
    surfaceContainerHighest  = Neutral24,

    // Kenarlık & bölücüler
    outline             = NeutralVariant60,
    outlineVariant      = NeutralVariant30,

    // Hata
    error               = Error80,
    onError             = Error20,
    errorContainer      = Error40,
    onErrorContainer    = Error90,

    // Özel yüzeyler
    inverseSurface      = Neutral90,
    inverseOnSurface    = Neutral20,
    inversePrimary      = IslamicGreen40,

    scrim               = Neutral10
)

// ─────────────────────────────────────────────────────────────────────────────
// IslamTheme — Uygulamanın tek merkezi tema sarmalayıcısı.
//
// darkTheme: DataStore'daki kullanıcı tercihinden okunarak MainActivity'den
//            geçirilir; false ise açık tema kullanılır.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun IslamTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = IslamicTypography,
        content     = content
    )
}

