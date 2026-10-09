package ph.appbuilders.offlinehealth.features.modelsetup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.appbuilders.offlinehealth.R
import ph.appbuilders.offlinehealth.app.LocalUiText
import ph.appbuilders.offlinehealth.app.UiKey
import ph.appbuilders.offlinehealth.app.theme.Palette

// Pieces of the setup screen (Setup.dc.html, Specs → setup).

/** 64 dp icon circle, headlineMedium title, bodyLarge InkMuted line. [done] fills the circle with Ink. */
@Composable
internal fun SetupHeader(icon: Int, title: String, body: String, done: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier.size(64.dp).background(if (done) Palette.Ink else Palette.Surface, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(icon),
                null,
                Modifier.size(if (done) 32.dp else 30.dp),
                tint = if (done) Palette.Paper else Palette.Ink,
            )
        }
        // A live region, so TalkBack announces each step as the download moves on without a tap.
        Text(
            title,
            Modifier.semantics { heading(); liveRegion = LiveRegionMode.Polite },
            style = MaterialTheme.typography.headlineMedium,
            color = Palette.Ink,
        )
        Text(body, style = MaterialTheme.typography.bodyLarge, color = Palette.InkMuted)
    }
}

/** B1: size (bold), works offline, stays on the phone. */
@Composable
internal fun IntroFacts(size: String) {
    val text = LocalUiText.current
    val sizeLine = buildAnnotatedString {
        val parts = text[UiKey.SETUP_INTRO_SIZE].split("{size}", limit = 2)
        append(parts[0])
        if (parts.size == 2) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(size) }
            append(parts[1])
        }
    }
    Column(Modifier.padding(top = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Fact(R.drawable.ic_smartphone) { Text(sizeLine, style = MaterialTheme.typography.bodyMedium) }
        Fact(R.drawable.ic_wifi_off) { Text(text[UiKey.SETUP_INTRO_OFFLINE], style = MaterialTheme.typography.bodyMedium) }
        Fact(R.drawable.ic_lock) { Text(text[UiKey.SETUP_INTRO_PRIVATE], style = MaterialTheme.typography.bodyMedium) }
    }
}

@Composable
private fun Fact(icon: Int, label: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Icon(painterResource(icon), null, Modifier.size(24.dp), tint = Palette.InkMuted)
        label()
    }
}

/** B2: 8 dp bar, "312 of 740 MB" bold + percent, and a reminder to keep Wi-Fi on. */
@Composable
internal fun DownloadProgress(doneMb: Int, totalMb: Int) {
    val text = LocalUiText.current
    val fraction = if (totalMb > 0) (doneMb.toFloat() / totalMb).coerceIn(0f, 1f) else 0f
    Column(Modifier.padding(top = 32.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Bar(fraction, Modifier.semantics { contentDescription = text[UiKey.SETUP_PROGRESS_DESC] })
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text.get(UiKey.SETUP_PROGRESS, "done" to "$doneMb", "total" to "$totalMb"),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            )
            Text("${(fraction * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, color = Palette.InkMuted)
        }
        Row(
            modifier = Modifier.padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(painterResource(R.drawable.ic_wifi), null, Modifier.size(18.dp), tint = Palette.InkMuted)
            Text(text[UiKey.SETUP_KEEP_WIFI], style = MaterialTheme.typography.labelMedium, color = Palette.InkMuted)
        }
    }
}

/** B3: the indeterminate version of the same bar. */
@Composable
internal fun CheckingProgress() {
    LinearProgressIndicator(
        modifier = Modifier.padding(top = 32.dp).fillMaxWidth().height(8.dp).clip(CircleShape),
        color = Palette.Ink,
        trackColor = Palette.SurfaceStrong,
        strokeCap = StrokeCap.Round,
        gapSize = 0.dp,
    )
}

/** B6: needed vs free, a bar of how much of the need is free, and how much more to clear. */
@Composable
internal fun StorageNeeded(neededMb: Int, freeMb: Int) {
    val text = LocalUiText.current
    val fraction = if (neededMb > 0) (freeMb.toFloat() / neededMb).coerceIn(0f, 1f) else 0f
    Column(Modifier.padding(top = 32.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StorageFigure(text[UiKey.SETUP_STORAGE_NEEDED], mb(text, neededMb), Modifier.weight(1f))
            StorageFigure(text[UiKey.SETUP_STORAGE_FREE], mb(text, freeMb), Modifier.weight(1f))
        }
        Bar(fraction, Modifier.padding(top = 14.dp).clearAndSetSemantics {}) // the figures above say it
        Text(
            text.get(UiKey.SETUP_STORAGE_MORE, "mb" to "${(neededMb - freeMb).coerceAtLeast(0)}"),
            Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Palette.InkMuted,
        )
    }
}

@Composable
private fun StorageFigure(label: String, value: String, modifier: Modifier) {
    Column(modifier.semantics(mergeDescendants = true) {}, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Palette.InkMuted)
        Text(value, style = MaterialTheme.typography.headlineMedium.copy(lineHeight = 34.sp), color = Palette.Ink)
    }
}

/** Specs → setup: 8 dp, radius full, Ink on SurfaceStrong, no gap or stop dot. */
@Composable
private fun Bar(fraction: Float, modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        progress = { fraction },
        modifier = modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
        color = Palette.Ink,
        trackColor = Palette.SurfaceStrong,
        strokeCap = StrokeCap.Round,
        gapSize = 0.dp,
        drawStopIndicator = {},
    )
}
