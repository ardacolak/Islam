package com.example.islam.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.islam.R

// ─────────────────────────────────────────────────────────────────────────────
// Google Fonts sağlayıcısı
// res/font/font_certs.xml üzerinden doğrulanır.
// ─────────────────────────────────────────────────────────────────────────────
private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs
)

// ── Nunito — Başlık ve UI Fontu ───────────────────────────────────────────────
// Yuvarlak, modern, okunabilir. İslami içerik için huzurlu hissiyet.
private val nunitoFont = GoogleFont("Nunito")

private val NunitoFamily = FontFamily(
    Font(googleFont = nunitoFont, fontProvider = googleFontProvider, weight = FontWeight.Light,     style = FontStyle.Normal),
    Font(googleFont = nunitoFont, fontProvider = googleFontProvider, weight = FontWeight.Normal,    style = FontStyle.Normal),
    Font(googleFont = nunitoFont, fontProvider = googleFontProvider, weight = FontWeight.Medium,    style = FontStyle.Normal),
    Font(googleFont = nunitoFont, fontProvider = googleFontProvider, weight = FontWeight.SemiBold,  style = FontStyle.Normal),
    Font(googleFont = nunitoFont, fontProvider = googleFontProvider, weight = FontWeight.Bold,      style = FontStyle.Normal),
    Font(googleFont = nunitoFont, fontProvider = googleFontProvider, weight = FontWeight.ExtraBold, style = FontStyle.Normal),
)

// ── Lato — Okuma / Gövde Fontu ───────────────────────────────────────────────
// Açık, tarafsız, uzun metinlerde göz yormaz.
private val latoFont = GoogleFont("Lato")

private val LatoFamily = FontFamily(
    Font(googleFont = latoFont, fontProvider = googleFontProvider, weight = FontWeight.Light,    style = FontStyle.Normal),
    Font(googleFont = latoFont, fontProvider = googleFontProvider, weight = FontWeight.Normal,   style = FontStyle.Normal),
    Font(googleFont = latoFont, fontProvider = googleFontProvider, weight = FontWeight.Medium,   style = FontStyle.Normal),
    Font(googleFont = latoFont, fontProvider = googleFontProvider, weight = FontWeight.Bold,     style = FontStyle.Normal),
)

// ─────────────────────────────────────────────────────────────────────────────
// Material 3 Tipografi Ölçeği
//
// Display  → Karşılama ekranları, hero başlıkları
// Headline → Ekran başlıkları, kart başlıkları
// Title    → Bölüm başlıkları, liste öğesi başlıkları
// Body     → Paragraf & açıklama metni, namaz vakiti açıklamaları
// Label    → Düğme, sekme, etiket
// ─────────────────────────────────────────────────────────────────────────────
val IslamicTypography = Typography(

    // ── Display ───────────────────────────────────────────────────────────────
    displayLarge = TextStyle(
        fontFamily    = NunitoFamily,
        fontWeight    = FontWeight.Light,
        fontSize      = 57.sp,
        lineHeight    = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily    = NunitoFamily,
        fontWeight    = FontWeight.Light,
        fontSize      = 45.sp,
        lineHeight    = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily    = NunitoFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 36.sp,
        lineHeight    = 44.sp,
        letterSpacing = 0.sp
    ),

    // ── Headline ──────────────────────────────────────────────────────────────
    headlineLarge = TextStyle(
        fontFamily    = NunitoFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 32.sp,
        lineHeight    = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily    = NunitoFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 28.sp,
        lineHeight    = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily    = NunitoFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 24.sp,
        lineHeight    = 32.sp,
        letterSpacing = 0.sp
    ),

    // ── Title ─────────────────────────────────────────────────────────────────
    titleLarge = TextStyle(
        fontFamily    = NunitoFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 22.sp,
        lineHeight    = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily    = NunitoFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily    = NunitoFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // ── Body — Namaz vakitleri, hadisler, açıklama metinleri ──────────────────
    bodyLarge = TextStyle(
        fontFamily    = LatoFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 16.sp,
        lineHeight    = 26.sp,   // okuma konforu için geniş satır aralığı
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily    = LatoFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = 22.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily    = LatoFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 12.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.4.sp
    ),

    // ── Label — Butonlar, sekmeler, etiketler ─────────────────────────────────
    labelLarge = TextStyle(
        fontFamily    = NunitoFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily    = NunitoFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily    = NunitoFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 11.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.5.sp
    )
)