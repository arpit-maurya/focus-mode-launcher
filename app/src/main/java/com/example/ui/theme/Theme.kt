package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.EInkThemePreset

private val PurePaperColorScheme = lightColorScheme(
    primary = PaperInk,
    onPrimary = PaperCanvas,
    primaryContainer = PaperSurface,
    onPrimaryContainer = PaperInk,
    secondary = PaperSlate,
    onSecondary = PaperCanvas,
    background = PaperCanvas,
    onBackground = PaperInk,
    surface = PaperSurface,
    onSurface = PaperInk,
    outline = PaperBorder,
    outlineVariant = PaperMuted
)

private val WarmEPaperColorScheme = lightColorScheme(
    primary = WarmInk,
    onPrimary = WarmCanvas,
    primaryContainer = WarmSurface,
    onPrimaryContainer = WarmInk,
    secondary = WarmSlate,
    onSecondary = WarmCanvas,
    background = WarmCanvas,
    onBackground = WarmInk,
    surface = WarmSurface,
    onSurface = WarmInk,
    outline = WarmBorder,
    outlineVariant = WarmMuted
)

private val CharcoalSlateColorScheme = darkColorScheme(
    primary = CharcoalInk,
    onPrimary = CharcoalCanvas,
    primaryContainer = CharcoalSurface,
    onPrimaryContainer = CharcoalInk,
    secondary = CharcoalSlate,
    onSecondary = CharcoalCanvas,
    background = CharcoalCanvas,
    onBackground = CharcoalInk,
    surface = CharcoalSurface,
    onSurface = CharcoalInk,
    outline = CharcoalBorder,
    outlineVariant = CharcoalMuted
)

private val BrutalistInkColorScheme = darkColorScheme(
    primary = BrutalInk,
    onPrimary = BrutalCanvas,
    primaryContainer = BrutalSurface,
    onPrimaryContainer = BrutalInk,
    secondary = BrutalSlate,
    onSecondary = BrutalCanvas,
    background = BrutalCanvas,
    onBackground = BrutalInk,
    surface = BrutalSurface,
    onSurface = BrutalInk,
    outline = BrutalBorder,
    outlineVariant = BrutalMuted
)

@Composable
fun DistractionFreeTheme(
    themePreset: EInkThemePreset = EInkThemePreset.WARM_EPAPER,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themePreset) {
        EInkThemePreset.PURE_PAPER -> PurePaperColorScheme
        EInkThemePreset.WARM_EPAPER -> WarmEPaperColorScheme
        EInkThemePreset.CHARCOAL_SLATE -> CharcoalSlateColorScheme
        EInkThemePreset.ULTRA_CONTRAST -> BrutalistInkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
