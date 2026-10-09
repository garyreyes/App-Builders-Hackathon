package ph.appbuilders.offlinehealth.app.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Color tokens from `Tokens.dc.html`: neutrals plus one red. [Danger] fills the danger banner and nothing else.
 * Errors that aren't urgent (no internet, storage) stay neutral. Grouped in an object so `Surface` and `Divider`
 * don't clash with the Material composables of the same name.
 */
object Palette {
    val Paper = Color(0xFFFFFFFF)         // background
    val Surface = Color(0xFFF5F5F3)       // cards, input, tiles, pill
    val SurfaceStrong = Color(0xFFEBEAE6) // user bubble, progress track, disabled
    val Divider = Color(0xFFE4E3DF)       // hairlines between rows
    val Outline = Color(0xFFC8C7C2)       // chip and secondary button borders
    val Ink = Color(0xFF15171A)           // text, primary buttons, step numbers (17.9:1)
    val InkMuted = Color(0xFF4E545B)      // secondary text, disclaimer (7.6:1)
    val InkSubtle = Color(0xFF676D74)     // captions, AI label, source line (5.2:1)
    val Danger = Color(0xFFA80F1A)        // danger banner fill, danger only (7.6:1)
    val DangerInk = Color(0xFF8A0C15)     // red text and dots on tints (9.8:1)
    val DangerSurface = Color(0xFFFBEDEE) // "Go now if" block
    val BrandMark = Color(0xFFC4545A)     // app mark pulse line only
    val Scrim = Color(0x7315171A)         // Ink at 45%
    val Disabled = Color(0xFF8E9398)      // disabled button label (Specs.dc.html)
}

/**
 * Roles set in the Tokens block come first. The rest are filled with the nearest neutral, so no Material
 * baseline color (purple) can leak into a component that reads a role the design didn't name.
 */
val AppColors = lightColorScheme(
    primary = Palette.Ink, onPrimary = Palette.Paper,
    secondary = Palette.InkMuted, onSecondary = Palette.Paper,
    background = Palette.Paper, onBackground = Palette.Ink,
    surface = Palette.Paper, onSurface = Palette.Ink,
    surfaceVariant = Palette.Surface, onSurfaceVariant = Palette.InkMuted,
    surfaceContainer = Palette.Surface, surfaceContainerHigh = Palette.SurfaceStrong,
    outline = Palette.Outline, outlineVariant = Palette.Divider,
    error = Palette.Danger, onError = Palette.Paper,
    errorContainer = Palette.DangerSurface, onErrorContainer = Palette.DangerInk,
    scrim = Palette.Scrim,
    // Not named in the design.
    primaryContainer = Palette.SurfaceStrong, onPrimaryContainer = Palette.Ink,
    secondaryContainer = Palette.SurfaceStrong, onSecondaryContainer = Palette.Ink,
    tertiary = Palette.InkMuted, onTertiary = Palette.Paper,
    tertiaryContainer = Palette.Surface, onTertiaryContainer = Palette.Ink,
    inversePrimary = Palette.SurfaceStrong,
    inverseSurface = Palette.Ink, inverseOnSurface = Palette.Paper,
    surfaceTint = Palette.Ink,
    surfaceBright = Palette.Paper, surfaceDim = Palette.SurfaceStrong,
    surfaceContainerLowest = Palette.Paper, surfaceContainerLow = Palette.Paper,
    surfaceContainerHighest = Palette.SurfaceStrong,
)
