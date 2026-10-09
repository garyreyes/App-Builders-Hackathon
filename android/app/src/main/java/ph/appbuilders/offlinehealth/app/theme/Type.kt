package ph.appbuilders.offlinehealth.app.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ph.appbuilders.offlinehealth.R

/** Atkinson Hyperlegible Next, bundled in res/font (OFL, see assets/licenses). Offline app: no downloadable fonts. */
val Atkinson = FontFamily(
    Font(R.font.atkinson_next_regular, FontWeight.Normal),
    Font(R.font.atkinson_next_semibold, FontWeight.SemiBold),
    Font(R.font.atkinson_next_bold, FontWeight.Bold),
    Font(R.font.atkinson_next_extrabold, FontWeight.ExtraBold),
)

private fun s(size: Int, lh: Int, w: FontWeight) =
    TextStyle(fontFamily = Atkinson, fontSize = size.sp, lineHeight = lh.sp, fontWeight = w)

private val MaterialDefaults = Typography()

/** Type scale from `Tokens.dc.html`. All sizes in sp so they follow the phone's font scale. */
val AppType = Typography(
    headlineMedium = s(28, 36, FontWeight.Bold),      // screen prompt
    headlineSmall = s(24, 30, FontWeight.ExtraBold),  // danger headline
    titleLarge = s(22, 28, FontWeight.Bold),          // card title
    titleMedium = s(18, 24, FontWeight.Bold),         // card section
    bodyLarge = s(18, 26, FontWeight.Normal),         // steps, AI reply, input
    bodyMedium = s(16, 24, FontWeight.Normal),        // body
    labelLarge = s(16, 20, FontWeight.Bold),          // buttons, chips
    labelMedium = s(14, 20, FontWeight.SemiBold),     // captions, pill
    // Not in the design. Same family, and nothing below the 14 sp caption floor.
    displayLarge = MaterialDefaults.displayLarge.copy(fontFamily = Atkinson),
    displayMedium = MaterialDefaults.displayMedium.copy(fontFamily = Atkinson),
    displaySmall = MaterialDefaults.displaySmall.copy(fontFamily = Atkinson),
    headlineLarge = MaterialDefaults.headlineLarge.copy(fontFamily = Atkinson),
    titleSmall = s(14, 20, FontWeight.SemiBold),
    bodySmall = s(14, 20, FontWeight.Normal),
    labelSmall = s(14, 20, FontWeight.SemiBold),
)
