package com.mobile360.banking.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun BankingTheme(easyMode: Boolean = false, content: @Composable () -> Unit) {
    val base = lightColorScheme(
        primary = AppColors.primaryContainer,
        onPrimary = AppColors.onPrimary,
        primaryContainer = AppColors.tonalContainer,
        onPrimaryContainer = AppColors.onTonalContainer,
        inversePrimary = AppColors.inversePrimary,
        secondary = AppColors.secondary,
        onSecondary = AppColors.onSecondary,
        secondaryContainer = AppColors.secondaryContainer,
        onSecondaryContainer = AppColors.onSecondaryContainer,
        tertiary = AppColors.tertiary,
        onTertiary = AppColors.onTertiary,
        tertiaryContainer = AppColors.tertiaryContainer,
        onTertiaryContainer = AppColors.onTertiaryContainer,
        background = AppColors.background,
        onBackground = AppColors.onBackground,
        surface = AppColors.surface,
        onSurface = AppColors.onSurface,
        surfaceVariant = AppColors.surfaceVariant,
        onSurfaceVariant = AppColors.onSurfaceVariant,
        surfaceTint = AppColors.surfaceTint,
        inverseSurface = AppColors.inverseSurface,
        inverseOnSurface = AppColors.inverseOnSurface,
        error = AppColors.error,
        onError = AppColors.onError,
        errorContainer = AppColors.errorContainer,
        onErrorContainer = AppColors.onErrorContainer,
        outline = AppColors.outline,
        outlineVariant = AppColors.outlineVariant,
        scrim = AppColors.scrim,
        surfaceBright = AppColors.surfaceBright,
        surfaceDim = AppColors.surfaceDim,
        surfaceContainerLowest = AppColors.surfaceContainerLowest,
        surfaceContainerLow = AppColors.surfaceContainerLow,
        surfaceContainer = AppColors.surfaceContainer,
        surfaceContainerHigh = AppColors.surfaceContainerHigh,
        surfaceContainerHighest = AppColors.surfaceContainerHighest
    )
    val scheme = if (easyMode) {
        base.copy(onSurfaceVariant = AppColors.inkDark, outline = AppColors.inkDark, primary = AppColors.primary)
    } else base
    MaterialTheme(
        colorScheme = scheme,
        typography = if (easyMode) BankTypography.scaled(1.25f) else BankTypography,
        shapes = BankShapes,
        content = content
    )
}
