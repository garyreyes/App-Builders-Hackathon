package ph.appbuilders.offlinehealth.features.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ph.appbuilders.offlinehealth.R
import ph.appbuilders.offlinehealth.app.LocalUiText
import ph.appbuilders.offlinehealth.app.UiKey
import ph.appbuilders.offlinehealth.app.components.SurfaceButton
import ph.appbuilders.offlinehealth.app.theme.Palette
import ph.appbuilders.offlinehealth.domain.model.TopicId
import ph.appbuilders.offlinehealth.domain.model.TopicSummary

// Blocks the canvas draws inline in the chat screens (HANDOFF §3 "Also build these").

/** Empty chat (C1): the prompt in the user's language and one line on what the app does. */
@Composable
fun ChatIntro(modifier: Modifier = Modifier) {
    val text = LocalUiText.current
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text[UiKey.CHAT_PROMPT], Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineMedium)
        Text(text[UiKey.CHAT_SUBTITLE], style = MaterialTheme.typography.bodyMedium, color = Palette.InkMuted)
    }
}

/** A small section heading ("Pananglitan", "O pili og topiko"). */
@Composable
fun SectionLabel(label: String, modifier: Modifier = Modifier) {
    Text(label, modifier.semantics { heading() }, style = MaterialTheme.typography.labelMedium, color = Palette.InkMuted)
}

/** Three example messages. Tapping one fills the field; it never sends. */
@Composable
fun ExampleChips(onExample: (String) -> Unit, modifier: Modifier = Modifier) {
    val text = LocalUiText.current
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(text[UiKey.CHAT_EXAMPLES], Modifier.padding(bottom = 2.dp))
        listOf(UiKey.CHAT_EXAMPLE_1, UiKey.CHAT_EXAMPLE_2, UiKey.CHAT_EXAMPLE_3).forEach { key ->
            val example = text[key]
            SurfaceButton(onClick = { onExample(example) }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("“$example”", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = Palette.Ink)
                    Icon(painterResource(R.drawable.ic_arrow_upward), null, Modifier.size(20.dp), tint = Palette.InkMuted)
                }
            }
        }
    }
}

/** The topic grid with its heading, under the empty chat (C1) or a not-covered card (C9). */
@Composable
fun TopicShortcuts(heading: String, topics: List<TopicSummary>, onTopic: (TopicId) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel(heading)
        TopicGrid(topics, onTopic)
    }
}

/** Not covered (C9): a calm card, same shell as the first-aid card. Never a guess. */
@Composable
fun NotCoveredCard(modifier: Modifier = Modifier) {
    val text = LocalUiText.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Palette.Surface, MaterialTheme.shapes.large)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(48.dp).background(Palette.Paper, CircleShape), contentAlignment = Alignment.Center) {
            Icon(painterResource(TopicId.NONE.iconRes()), null, Modifier.size(24.dp), tint = Palette.Ink)
        }
        Text(text[UiKey.NOT_COVERED_TITLE], Modifier.semantics { heading() }, style = MaterialTheme.typography.titleLarge)
        Text(text[UiKey.NOT_COVERED_BODY], style = MaterialTheme.typography.bodyLarge, color = Palette.Ink)
    }
}

/** "Also about:" chips for other matching topics (C5). Tap opens that card. */
@Composable
fun AlsoAboutChips(topics: List<TopicSummary>, onTopic: (TopicId) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(LocalUiText.current[UiKey.CHAT_ALSO_ABOUT], style = MaterialTheme.typography.labelMedium, color = Palette.InkMuted)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            topics.forEach { topic ->
                Row(
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Palette.Outline, CircleShape)
                        .clickable(role = Role.Button) { onTopic(topic.topicId) }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(painterResource(topic.topicId.iconRes()), null, Modifier.size(20.dp), tint = Palette.Ink)
                    Text(topic.title, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
                }
            }
        }
    }
}

/** "AI reply below" (C3): shown while the reply is coming and off screen. Tap scrolls to it. */
@Composable
fun ReplyBelowChip(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(CircleShape)
            .background(Palette.SurfaceStrong)
            .border(1.dp, Palette.Outline, CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 12.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(painterResource(R.drawable.ic_arrow_downward), null, Modifier.size(18.dp), tint = Palette.Ink)
        Text(
            LocalUiText.current[UiKey.CHAT_REPLY_BELOW],
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = Palette.Ink,
            maxLines = 1,
        )
    }
}
