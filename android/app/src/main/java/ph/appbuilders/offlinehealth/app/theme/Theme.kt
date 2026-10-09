package ph.appbuilders.offlinehealth.app.theme

import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Pressed feedback at 8% (Specs.dc.html: "Ripple in Ink at 8%"). The color follows the content color,
 * so it's Ink on Surface buttons and still visible on Ink-filled primary buttons.
 */
private val QuietRipple = RippleConfiguration(
    rippleAlpha = RippleAlpha(
        draggedAlpha = 0.08f,
        focusedAlpha = 0.08f,
        hoveredAlpha = 0.08f,
        pressedAlpha = 0.08f,
    ),
)

/** Light only for now (HANDOFF non-negotiable 8). Elevation stays at level 0 almost everywhere. */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AppColors, typography = AppType, shapes = AppShapes) {
        CompositionLocalProvider(LocalRippleConfiguration provides QuietRipple, content = content)
    }
}
