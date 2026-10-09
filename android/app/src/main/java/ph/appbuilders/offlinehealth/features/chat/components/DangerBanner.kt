package ph.appbuilders.offlinehealth.features.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ph.appbuilders.offlinehealth.R
import ph.appbuilders.offlinehealth.app.LocalUiText
import ph.appbuilders.offlinehealth.app.UiKey
import ph.appbuilders.offlinehealth.app.theme.Palette
import ph.appbuilders.offlinehealth.domain.model.DangerMessage
import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.fakes.FakeSamples
import ph.appbuilders.offlinehealth.fakes.PreviewFrame

/** The banner never shows more than this many reasons. The service orders them most severe first. */
const val MAX_DANGER_REASONS = 3

/**
 * The loudest thing on screen, and the only place Danger red is used. Never dismissible, never collapsed,
 * never animated in, never below the card. Red is always backed by an icon and bold text.
 * TalkBack reads it first (assertive live region). [onCall] should open the dialer at 911.
 */
@Composable
fun DangerBanner(dangers: List<DangerMessage>, onCall: () -> Unit, modifier: Modifier = Modifier) {
    val text = LocalUiText.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Palette.Danger, MaterialTheme.shapes.large)
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Assertive }
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(painterResource(R.drawable.ic_warning), null, Modifier.size(32.dp), tint = Palette.Paper)
            Text(
                text[UiKey.DANGER_HEADLINE],
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.headlineSmall,
                color = Palette.Paper,
            )
        }
        Column(Modifier.padding(start = 44.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            dangers.take(MAX_DANGER_REASONS).forEach { Reason(it.text) }
        }
        CallButton(text[UiKey.DANGER_CALL], onCall)
    }
}

@Composable
private fun Reason(reason: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.padding(top = 9.dp).size(8.dp).background(Palette.Paper, CircleShape))
        Text(
            reason,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = Palette.Paper,
        )
    }
}

@Composable
private fun CallButton(label: String, onCall: () -> Unit) {
    val shape = MaterialTheme.shapes.medium
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(shape)
            .border(2.dp, Palette.Paper.copy(alpha = 0.85f), shape)
            .clickable(role = Role.Button, onClick = onCall)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
    ) {
        Icon(painterResource(R.drawable.ic_call), null, Modifier.size(20.dp), tint = Palette.Paper)
        Text(label, style = MaterialTheme.typography.labelLarge, color = Palette.Paper, textAlign = TextAlign.Center)
    }
}

@Preview(widthDp = 360) @Composable
private fun DangerBannerOneReason() = PreviewFrame { DangerBanner(FakeSamples.dangerCeb, {}) }

@Preview(widthDp = 360) @Composable
private fun DangerBannerThreeReasons() = PreviewFrame(Language.ENG) { DangerBanner(FakeSamples.dangerEngThree, {}) }

@Preview(widthDp = 320, fontScale = 1.5f) @Composable
private fun DangerBannerNarrow() = PreviewFrame { DangerBanner(FakeSamples.dangerCeb, {}) }
