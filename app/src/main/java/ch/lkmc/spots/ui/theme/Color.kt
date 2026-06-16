package ch.lkmc.spots.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val Teal = Color(0xFF2C6E6A)
private val TealLight = Color(0xFF7FD1CB)
private val Amber = Color(0xFFB5742B)
private val AmberLight = Color(0xFFF5C77E)

val SpotsLightColors = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDE9E6),
    onPrimaryContainer = Color(0xFF06201E),
    secondary = Amber,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF6E3C9),
    tertiary = Color(0xFF4A6572),
    background = Color(0xFFFBF8F4),
    onBackground = Color(0xFF1A1C1B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C1B),
    surfaceVariant = Color(0xFFE9E6E1),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFFBFBBB4),
)

val SpotsDarkColors = darkColorScheme(
    primary = TealLight,
    onPrimary = Color(0xFF06201E),
    primaryContainer = Color(0xFF254E4B),
    onPrimaryContainer = Color(0xFFCDE9E6),
    secondary = AmberLight,
    onSecondary = Color(0xFF3A2706),
    secondaryContainer = Color(0xFF5A431F),
    tertiary = Color(0xFFB1CAD8),
    background = Color(0xFF0E1216),
    onBackground = Color(0xFFE3E2E0),
    surface = Color(0xFF171C20),
    onSurface = Color(0xFFE3E2E0),
    surfaceVariant = Color(0xFF3F4A4E),
    onSurfaceVariant = Color(0xFFBFC8CC),
    outline = Color(0xFF6B7378),
)
