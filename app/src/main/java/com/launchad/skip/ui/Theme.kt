package com.launchad.skip.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.graphics.Color
import com.launchad.skip.R

private val DisplayFont = FontFamily(
    Font(R.font.zcool_qingke_huangyou_regular),
)

private val BaseTypography = Typography()

private val AppTypography = Typography(
    displayLarge = BaseTypography.displayLarge.copy(fontFamily = DisplayFont),
    displayMedium = BaseTypography.displayMedium.copy(fontFamily = DisplayFont),
    displaySmall = BaseTypography.displaySmall.copy(fontFamily = DisplayFont),
    headlineLarge = BaseTypography.headlineLarge.copy(fontFamily = DisplayFont),
    headlineMedium = BaseTypography.headlineMedium.copy(fontFamily = DisplayFont),
    headlineSmall = BaseTypography.headlineSmall.copy(fontFamily = DisplayFont),
    titleLarge = BaseTypography.titleLarge.copy(fontFamily = DisplayFont),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF0B5C55),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC9EAE3),
    onPrimaryContainer = Color(0xFF052F2B),
    secondary = Color(0xFFA34F12),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDFB9),
    onSecondaryContainer = Color(0xFF341400),
    tertiary = Color(0xFF385B88),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD8E5F5),
    onTertiaryContainer = Color(0xFF0B2B4C),
    background = Color(0xFFF5F7F3),
    onBackground = Color(0xFF17201D),
    surface = Color(0xFFF5F7F3),
    onSurface = Color(0xFF17201D),
    surfaceVariant = Color(0xFFE0E7E2),
    onSurfaceVariant = Color(0xFF3F4945),
    outline = Color(0xFF6D7772),
    error = Color(0xFFAA312C),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8CD6CD),
    onPrimary = Color(0xFF003733),
    primaryContainer = Color(0xFF084B46),
    onPrimaryContainer = Color(0xFFC9EAE3),
    secondary = Color(0xFFF4B87D),
    onSecondary = Color(0xFF4A2800),
    secondaryContainer = Color(0xFF6A3808),
    onSecondaryContainer = Color(0xFFFFDFB9),
    tertiary = Color(0xFFA9C8ED),
    onTertiary = Color(0xFF0B3157),
    tertiaryContainer = Color(0xFF22496F),
    onTertiaryContainer = Color(0xFFD8E5F5),
    background = Color(0xFF0E1715),
    onBackground = Color(0xFFE1E8E4),
    surface = Color(0xFF0E1715),
    onSurface = Color(0xFFE1E8E4),
    surfaceVariant = Color(0xFF2A3632),
    onSurfaceVariant = Color(0xFFBCC8C2),
    outline = Color(0xFF87938D),
    error = Color(0xFFFFB4AB),
)

@Composable
fun LaunchAdSkipTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
