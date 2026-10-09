package ph.appbuilders.offlinehealth.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ph.appbuilders.offlinehealth.app.LocalUiText
import ph.appbuilders.offlinehealth.app.UiKey
import ph.appbuilders.offlinehealth.app.components.BackBar
import ph.appbuilders.offlinehealth.app.components.RowTitle
import ph.appbuilders.offlinehealth.app.theme.Palette
import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.domain.model.TopicCard
import ph.appbuilders.offlinehealth.domain.model.TopicSummary
import ph.appbuilders.offlinehealth.fakes.FakeContentSource
import ph.appbuilders.offlinehealth.fakes.PreviewFrame
import ph.appbuilders.offlinehealth.features.chat.components.iconRes

// The two pages Settings links to. No artboard draws them, so they only reuse copy and content that already
// exist (the disclaimer line, topic titles, each card's source). No new medical or legal wording.

/** "Not a doctor" first, then the topics this app covers. */
@Composable
fun AboutScreen(topics: List<TopicSummary>, onBack: () -> Unit) {
    val text = LocalUiText.current
    InfoPage(text[UiKey.SETTINGS_ABOUT], onBack) {
        Text(text[UiKey.COMPOSER_DISCLAIMER], style = MaterialTheme.typography.bodyLarge, color = Palette.Ink)
        Text(
            text[UiKey.NOT_COVERED_TOPICS],
            Modifier.padding(top = 16.dp).semantics { heading() },
            style = MaterialTheme.typography.labelMedium,
            color = Palette.InkMuted,
        )
        topics.forEach { topic ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Icon(painterResource(topic.topicId.iconRes()), null, Modifier.size(24.dp), tint = Palette.Ink)
                Text(topic.title, style = MaterialTheme.typography.bodyLarge, color = Palette.Ink)
            }
        }
    }
}

/** Each card's own source line, under its title. */
@Composable
fun SourcesScreen(cards: List<TopicCard>, onBack: () -> Unit) {
    val text = LocalUiText.current
    InfoPage(text[UiKey.SETTINGS_SOURCES], onBack) {
        cards.forEachIndexed { index, card ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(card.title, style = RowTitle, color = Palette.Ink)
                Text(card.source, style = MaterialTheme.typography.bodyMedium, color = Palette.InkMuted)
            }
            if (index < cards.lastIndex) HorizontalDivider(thickness = 1.dp, color = Palette.Divider)
        }
    }
}

@Composable
private fun InfoPage(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().background(Palette.Paper).safeDrawingPadding()) {
        BackBar(onBack, LocalUiText.current[UiKey.NAV_BACK], title = title)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
    }
}

private val previewContent = FakeContentSource()

@Preview(widthDp = 360, heightDp = 800) @Composable
private fun AboutPreview() = PreviewFrame(Language.ENG, padding = 0.dp) {
    AboutScreen(remember { previewContent.topics(Language.ENG) }) {}
}

@Preview(widthDp = 360, heightDp = 800) @Composable
private fun SourcesPreview() = PreviewFrame(Language.ENG, padding = 0.dp) {
    SourcesScreen(remember { previewContent.topics(Language.ENG).map { previewContent.card(it.topicId, Language.ENG) } }) {}
}
