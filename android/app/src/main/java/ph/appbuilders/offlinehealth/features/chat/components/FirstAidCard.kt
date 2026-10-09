package ph.appbuilders.offlinehealth.features.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.appbuilders.offlinehealth.R
import ph.appbuilders.offlinehealth.app.LocalUiText
import ph.appbuilders.offlinehealth.app.UiKey
import ph.appbuilders.offlinehealth.app.theme.Palette
import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.domain.model.TopicCard
import ph.appbuilders.offlinehealth.domain.model.TopicId
import ph.appbuilders.offlinehealth.fakes.FakeContentSource
import ph.appbuilders.offlinehealth.fakes.PreviewFrame

/**
 * A pre-translated first-aid card: header, "What to do at home" steps, "Go now if…" signs, source.
 * [matchedSigns] are indexes into `card.goNowIf` that the user's message matched: they turn bold with a tag.
 * [flat] is the Topics-screen variant (no fill, no padding). Quieter than the banner: tint, not fill.
 */
@Composable
fun FirstAidCard(card: TopicCard, matchedSigns: Set<Int>, modifier: Modifier = Modifier, flat: Boolean = false) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(if (flat) Color.Transparent else Palette.Surface, MaterialTheme.shapes.large)
            .padding(if (flat) 0.dp else 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        CardHeader(card.topicId, card.title, iconFill = if (flat) Palette.Surface else Palette.Paper)
        HomeSteps(card.atHome)
        GoNowBlock(card.goNowIf, matchedSigns)
        SourceLine(card.source)
    }
}

@Composable
private fun CardHeader(topicId: TopicId, title: String, iconFill: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(Modifier.size(48.dp).background(iconFill, CircleShape), contentAlignment = Alignment.Center) {
            Icon(painterResource(topicId.iconRes()), null, Modifier.size(24.dp), tint = Palette.Ink)
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(LocalUiText.current[UiKey.CARD_KICKER], style = MaterialTheme.typography.labelMedium, color = Palette.InkMuted)
            Text(title, Modifier.semantics { heading() }, style = MaterialTheme.typography.titleLarge, color = Palette.Ink)
        }
    }
}

@Composable
private fun HomeSteps(steps: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            LocalUiText.current[UiKey.CARD_HOME],
            Modifier.semantics { heading() },
            style = MaterialTheme.typography.titleMedium,
            color = Palette.Ink,
        )
        steps.forEachIndexed { index, step ->
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(Modifier.size(32.dp).background(Palette.Ink, CircleShape), contentAlignment = Alignment.Center) {
                    Text("${index + 1}", style = MaterialTheme.typography.labelLarge, color = Palette.Paper)
                }
                Text(step, Modifier.padding(top = 3.dp), style = MaterialTheme.typography.bodyLarge, color = Palette.Ink)
            }
        }
    }
}

@Composable
private fun GoNowBlock(signs: List<String>, matchedSigns: Set<Int>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Palette.DangerSurface, MaterialTheme.shapes.medium)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(painterResource(R.drawable.ic_warning), null, Modifier.size(22.dp), tint = Palette.DangerInk)
            Text(
                LocalUiText.current[UiKey.CARD_GO_NOW],
                Modifier.semantics { heading() },
                style = MaterialTheme.typography.titleMedium,
                color = Palette.DangerInk,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            signs.forEachIndexed { index, sign -> GoNowSign(sign, matched = index in matchedSigns) }
        }
    }
}

@Composable
private fun GoNowSign(sign: String, matched: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.padding(top = 9.dp).size(7.dp).background(Palette.DangerInk, CircleShape))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                sign,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (matched) FontWeight.Bold else FontWeight.Normal),
                color = Palette.Ink,
            )
            if (matched) YouSaidTag(Modifier.align(Alignment.CenterVertically))
        }
    }
}

@Composable
private fun YouSaidTag(modifier: Modifier) {
    Text(
        LocalUiText.current[UiKey.CARD_YOU_SAID],
        modifier = modifier
            .border(1.5.dp, Palette.DangerInk, CircleShape)
            .padding(horizontal = 8.dp, vertical = 1.dp),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, lineHeight = 18.sp),
        color = Palette.DangerInk,
        maxLines = 1,
    )
}

@Composable
private fun SourceLine(source: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(painterResource(R.drawable.ic_description), null, Modifier.padding(top = 2.dp).size(16.dp), tint = Palette.InkSubtle)
        Text(source, style = MaterialTheme.typography.labelMedium, color = Palette.InkSubtle)
    }
}

private val previewCards = FakeContentSource()

@Preview(widthDp = 360) @Composable
private fun CardMatchedBlood() = PreviewFrame {
    FirstAidCard(previewCards.card(TopicId.CHILD_DIARRHEA, Language.CEB), matchedSigns = setOf(2))
}

@Preview(widthDp = 360) @Composable
private fun CardEnglishThreeMatched() = PreviewFrame(Language.ENG) {
    FirstAidCard(previewCards.card(TopicId.CHILD_DIARRHEA, Language.ENG), matchedSigns = setOf(0, 1, 3))
}

@Preview(widthDp = 360) @Composable
private fun CardFlat() = PreviewFrame {
    FirstAidCard(previewCards.card(TopicId.CHILD_DIARRHEA, Language.CEB), matchedSigns = emptySet(), flat = true)
}
