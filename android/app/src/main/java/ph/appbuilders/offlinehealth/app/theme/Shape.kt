package ph.appbuilders.offlinehealth.app.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Corner radii from `Tokens.dc.html`. "Full" (pill, input, send, chips) is `CircleShape`. */
val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),       // small tags
    medium = RoundedCornerShape(12.dp),     // buttons, inner blocks, "Go now" block
    large = RoundedCornerShape(16.dp),      // cards, banner, example chips
    extraLarge = RoundedCornerShape(24.dp), // bottom sheet top
)
