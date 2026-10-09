package ph.appbuilders.offlinehealth

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ph.appbuilders.offlinehealth.app.theme.AppTheme
import ph.appbuilders.offlinehealth.features.gallery.ComponentGallery

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Light only: dark system-bar icons over the white Paper background, even in system dark mode.
        val lightBars = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = lightBars, navigationBarStyle = lightBars)
        setContent {
            AppTheme { ComponentGallery() }
        }
    }
}
