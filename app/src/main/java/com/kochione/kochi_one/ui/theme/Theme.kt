package com.kochione.kochi_one.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun KochiOneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is disabled by default to respect app theme selection
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val targetColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val animatedColorScheme = targetColorScheme.copy(
        primary = animateColorAsState(targetColorScheme.primary, tween(500), label = "primary").value,
        onPrimary = animateColorAsState(targetColorScheme.onPrimary, tween(500), label = "onPrimary").value,
        primaryContainer = animateColorAsState(targetColorScheme.primaryContainer, tween(500), label = "primaryContainer").value,
        onPrimaryContainer = animateColorAsState(targetColorScheme.onPrimaryContainer, tween(500), label = "onPrimaryContainer").value,
        inversePrimary = animateColorAsState(targetColorScheme.inversePrimary, tween(500), label = "inversePrimary").value,
        secondary = animateColorAsState(targetColorScheme.secondary, tween(500), label = "secondary").value,
        onSecondary = animateColorAsState(targetColorScheme.onSecondary, tween(500), label = "onSecondary").value,
        secondaryContainer = animateColorAsState(targetColorScheme.secondaryContainer, tween(500), label = "secondaryContainer").value,
        onSecondaryContainer = animateColorAsState(targetColorScheme.onSecondaryContainer, tween(500), label = "onSecondaryContainer").value,
        tertiary = animateColorAsState(targetColorScheme.tertiary, tween(500), label = "tertiary").value,
        onTertiary = animateColorAsState(targetColorScheme.onTertiary, tween(500), label = "onTertiary").value,
        tertiaryContainer = animateColorAsState(targetColorScheme.tertiaryContainer, tween(500), label = "tertiaryContainer").value,
        onTertiaryContainer = animateColorAsState(targetColorScheme.onTertiaryContainer, tween(500), label = "onTertiaryContainer").value,
        background = animateColorAsState(targetColorScheme.background, tween(500), label = "background").value,
        onBackground = animateColorAsState(targetColorScheme.onBackground, tween(500), label = "onBackground").value,
        surface = animateColorAsState(targetColorScheme.surface, tween(500), label = "surface").value,
        onSurface = animateColorAsState(targetColorScheme.onSurface, tween(500), label = "onSurface").value,
        surfaceVariant = animateColorAsState(targetColorScheme.surfaceVariant, tween(500), label = "surfaceVariant").value,
        onSurfaceVariant = animateColorAsState(targetColorScheme.onSurfaceVariant, tween(500), label = "onSurfaceVariant").value,
        surfaceTint = animateColorAsState(targetColorScheme.surfaceTint, tween(500), label = "surfaceTint").value,
        inverseSurface = animateColorAsState(targetColorScheme.inverseSurface, tween(500), label = "inverseSurface").value,
        inverseOnSurface = animateColorAsState(targetColorScheme.inverseOnSurface, tween(500), label = "inverseOnSurface").value,
        error = animateColorAsState(targetColorScheme.error, tween(500), label = "error").value,
        onError = animateColorAsState(targetColorScheme.onError, tween(500), label = "onError").value,
        errorContainer = animateColorAsState(targetColorScheme.errorContainer, tween(500), label = "errorContainer").value,
        onErrorContainer = animateColorAsState(targetColorScheme.onErrorContainer, tween(500), label = "onErrorContainer").value,
        outline = animateColorAsState(targetColorScheme.outline, tween(500), label = "outline").value,
        outlineVariant = animateColorAsState(targetColorScheme.outlineVariant, tween(500), label = "outlineVariant").value,
        scrim = animateColorAsState(targetColorScheme.scrim, tween(500), label = "scrim").value
    )

    MaterialTheme(
        colorScheme = animatedColorScheme,
        typography = Typography,
        content = content
    )
}