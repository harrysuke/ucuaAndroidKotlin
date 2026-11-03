package com.ucua.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- JPB maritime safety palette -----------------------------------------
val UcuaPrimary = Color(0xFF0B4F6C)
val UcuaPrimaryDark = Color(0xFF073A4F)
val UcuaPrimaryLight = Color(0xFF3D7E99)
val UcuaAccent = Color(0xFFF2A33C)
val UcuaAccentDark = Color(0xFF8A5200)
val UcuaSuccess = Color(0xFF2E9E6B)
val UcuaDanger = Color(0xFFD64545)
val UcuaWarning = Color(0xFFE08A2B)
val UcuaInfo = Color(0xFF3B7DD8)

val UcuaBackground = Color(0xFFF4F7F9)
val UcuaSurface = Color(0xFFFFFFFF)
val UcuaSurfaceMuted = Color(0xFFEDF2F5)

// Semantic status colours used across chips, timeline and charts.
val StatusDraft = Color(0xFF6B7A85)
val StatusPending = Color(0xFFE08A2B)
val StatusClosed = Color(0xFF2E9E6B)
val StatusRejected = Color(0xFFD64545)

private val LightColors = lightColorScheme(
    primary = UcuaPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8E8F0),
    onPrimaryContainer = UcuaPrimaryDark,
    secondary = UcuaAccent,
    onSecondary = Color(0xFF3B2600),
    secondaryContainer = Color(0xFFFCEBCF),
    onSecondaryContainer = UcuaAccentDark,
    tertiary = UcuaSuccess,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD8F0E4),
    onTertiaryContainer = Color(0xFF0F4D33),
    error = UcuaDanger,
    onError = Color.White,
    errorContainer = Color(0xFFFBE0E0),
    onErrorContainer = Color(0xFF6B1414),
    background = UcuaBackground,
    onBackground = Color(0xFF16232B),
    surface = UcuaSurface,
    onSurface = Color(0xFF16232B),
    surfaceVariant = UcuaSurfaceMuted,
    onSurfaceVariant = Color(0xFF51636E),
    outline = Color(0xFFB9C6CE),
    outlineVariant = Color(0xFFDCE4E9),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7FB9D3),
    onPrimary = Color(0xFF04222F),
    primaryContainer = Color(0xFF14435A),
    onPrimaryContainer = Color(0xFFD8E8F0),
    secondary = Color(0xFFF5BE72),
    onSecondary = Color(0xFF2E1C00),
    secondaryContainer = Color(0xFF5C3B00),
    onSecondaryContainer = Color(0xFFFCEBCF),
    tertiary = Color(0xFF6FCB9B),
    onTertiary = Color(0xFF00291A),
    tertiaryContainer = Color(0xFF1B5138),
    onTertiaryContainer = Color(0xFFD8F0E4),
    error = Color(0xFFF19A9A),
    onError = Color(0xFF4A0F0F),
    errorContainer = Color(0xFF6B1B1B),
    onErrorContainer = Color(0xFFFBE0E0),
    background = Color(0xFF0E161C),
    onBackground = Color(0xFFE4EDF2),
    surface = Color(0xFF151F26),
    onSurface = Color(0xFFE4EDF2),
    surfaceVariant = Color(0xFF1F2C34),
    onSurfaceVariant = Color(0xFFA9BAC4),
    outline = Color(0xFF44555F),
    outlineVariant = Color(0xFF2A3942),
)

private val UcuaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

private val UcuaTypography = Typography(
    headlineLarge = TextStyle(fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 25.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontSize = 21.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 19.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 23.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 15.sp, fontWeight = FontWeight.Medium),
)

@Composable
fun UcuaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = UcuaTypography,
        shapes = UcuaShapes,
        content = content,
    )
}
