package com.cccuan.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BrandNavy = Color(0xFF0F172A)
val BrandNavyLight = Color(0xFF1E293B)
val BrandEmerald = Color(0xFF10B981)
val BrandEmeraldDark = Color(0xFF059669)
val BrandAmber = Color(0xFFD97706)
val BrandRed = Color(0xFFDC2626)
val SurfaceBg = Color(0xFFF1F5F9)
val CardWhite = Color(0xFFFFFFFF)
val TextMuted = Color(0xFF64748B)

private val LightColors = lightColorScheme(
    primary = BrandNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2E8F0),
    onPrimaryContainer = BrandNavy,
    secondary = BrandEmerald,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF064E3B),
    background = SurfaceBg,
    onBackground = BrandNavy,
    surface = CardWhite,
    onSurface = BrandNavy,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    error = BrandRed,
    onError = Color.White
)

@Composable
fun CcCuanTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
