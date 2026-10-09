package ph.appbuilders.offlinehealth.app.theme

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** Motion timings from Specs.dc.html. Motion is feedback only; nothing that matters waits on it. */
object Motion {
    const val PILL_CROSSFADE_MS = 150
    const val DOTS_HALF_CYCLE_MS = 600   // thinking dots: 0.3 → 1 → 0.3 over 1.2 s
    const val DOTS_STAGGER_MS = 150
    const val CARET_BLINK_MS = 500       // on 0.5 s, off 0.5 s
    const val SPINNER_TURN_MS = 1000
}

/** True when the phone's "Remove animations" is on: no pulse, no blink, instant scroll. */
@Composable
fun rememberReducedMotion(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}
