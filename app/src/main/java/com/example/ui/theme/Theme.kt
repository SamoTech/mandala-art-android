package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = PurpleLight,
    onPrimary = PurpleDeep,
    primaryContainer = PurpleMedium,
    onPrimaryContainer = Color.White,
    secondary = PinkAccentLight,
    onSecondary = PinkAccentDark,
    secondaryContainer = PinkAccentDark,
    onSecondaryContainer = Color.White,
    tertiary = IndigoLight,
    onTertiary = IndigoDeep,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = Color.White,
    primaryContainer = PurpleContainer,
    onPrimaryContainer = PurpleDeep,
    secondary = PinkAccent,
    onSecondary = Color.White,
    secondaryContainer = PinkContainer,
    onSecondaryContainer = PinkAccentDark,
    tertiary = IndigoPrimary,
    onTertiary = Color.White,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight
)

// Material Glass: larger, softer corner radii for every Material component.
private val GlassShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

/** Soft diagonal gradient painted behind the app's translucent glass surfaces (light & dark variants). */
@Composable
fun mandalaGlassGradient(): Brush {
    val dark = isSystemInDarkTheme()
    return Brush.linearGradient(
        if (dark) {
            listOf(GlassGradientDarkTop, GlassGradientDarkMid, GlassGradientDarkBottom)
        } else {
            listOf(GlassGradientLightTop, GlassGradientLightMid, GlassGradientLightBottom)
        }
    )
}

/** Frosted hairline border color used to outline glass cards and bars. */
@Composable
fun glassStroke(): Color = if (isSystemInDarkTheme()) GlassStrokeDark else GlassStrokeLight

@Composable
fun MandalaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = GlassShapes,
        content = content
    )
}
