package com.example.islam.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// ── Light (Aydınlık) Renk Şeması ─────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary             = IslamicGreen40,
    onPrimary           = Neutral99,
    primaryContainer    = IslamicGreen90,
    onPrimaryContainer  = IslamicGreen10,

    secondary           = Gold40,
    onSecondary         = Neutral99,
    secondaryContainer  = Gold90,
    onSecondaryContainer= Gold10,

    background          = Neutral99,
    onBackground        = Neutral10,

    surface             = Neutral99,
    onSurface           = Neutral10,
    surfaceVariant      = NeutralVariant90,
    onSurfaceVariant    = NeutralVariant30,

    outline             = NeutralVariant50,
    outlineVariant      = NeutralVariant80,

    error               = Error40,
    onError             = Neutral99,
    errorContainer      = Error90,
    onErrorContainer    = Error40
)

// ── Dark (Karanlık) Renk Şeması ───────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary             = IslamicGreen80,
    onPrimary           = IslamicGreen20,
    primaryContainer    = IslamicGreen30,
    onPrimaryContainer  = IslamicGreen90,

    secondary           = Gold80,
    onSecondary         = Gold20,
    secondaryContainer  = Gold20,
    onSecondaryContainer= Gold90,

    background          = Neutral10,
    onBackground        = Neutral90,

    surface             = Neutral10,
    onSurface           = Neutral90,
    surfaceVariant      = NeutralVariant30,
    onSurfaceVariant    = NeutralVariant80,

    outline             = NeutralVariant50,
    outlineVariant      = NeutralVariant30,

    error               = Error80,
    onError             = Neutral10,
    errorContainer      = Error40,
    onErrorContainer    = Error90
)

@Composable
fun IslamTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
