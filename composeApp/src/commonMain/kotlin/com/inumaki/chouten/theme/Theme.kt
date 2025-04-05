package com.inumaki.chouten.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Define color scheme
data class ChoutenColors(
    val background: Color,
    val container: Color,
    val overlay: Color,
    val border: Color,
    val fg: Color,
    val accent: Color
)

val LightChoutenColors = ChoutenColors(
    background = Color(0xFFF5F5F5),
    container = Color(0xFFFFFFFF),
    overlay = Color(0xFF212121),
    border = Color(0xFFE0E0E0),
    fg = Color(0xFF757575),
    accent = Color(0xFFFF4081)
)

val DarkChoutenColors = ChoutenColors(
    background = Color(0xFF0C0C0C),
    container = Color(0xFF171717),
    overlay = Color(0xFF272727),
    border = Color(0xFF3B3B3B),
    fg = Color(0xFFD4D4D4),
    accent = Color(0xFF5E5CE6)
)

// Define typography
data class ChoutenTypography(
    val title: TextStyle,
    val body: TextStyle,
    val caption: TextStyle
)

val DefaultTypography = ChoutenTypography(
    title = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Default
    ),
    body = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.Default
    ),
    caption = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Light,
        fontFamily = FontFamily.Default
    )
)

// Define shapes
data class ChoutenShapes(
    val small: RoundedCornerShape,
    val medium: RoundedCornerShape,
    val large: RoundedCornerShape
)

val DefaultShapes = ChoutenShapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
)

@Composable
expect fun isTablet(): Boolean

data class DeviceInfo(val isTablet: Boolean)

val LocalDeviceInfo = staticCompositionLocalOf<DeviceInfo> {
    error("No device information found")
}

// Define CompositionLocals
val LocalChoutenColors = staticCompositionLocalOf { LightChoutenColors }
val LocalChoutenTypography = staticCompositionLocalOf { DefaultTypography }
val LocalChoutenShapes = staticCompositionLocalOf { DefaultShapes }

// ChoutenTheme provider function
@Composable
fun ChoutenTheme(
    colors: ChoutenColors = if (isSystemInDarkTheme()) DarkChoutenColors else LightChoutenColors,
    typography: ChoutenTypography = DefaultTypography,
    shapes: ChoutenShapes = DefaultShapes,
    content: @Composable () -> Unit
) {
    val isTablet = isTablet()

    CompositionLocalProvider(
        LocalChoutenColors provides colors,
        LocalChoutenTypography provides typography,
        LocalChoutenShapes provides shapes,
        LocalDeviceInfo provides DeviceInfo(isTablet),
        content = content
    )
}

// Theme object for easy access
object ChoutenTheme {
    val colors: ChoutenColors
        @Composable get() = LocalChoutenColors.current

    val typography: ChoutenTypography
        @Composable get() = LocalChoutenTypography.current

    val shapes: ChoutenShapes
        @Composable get() = LocalChoutenShapes.current
}
