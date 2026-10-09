package ph.appbuilders.offlinehealth.features.topics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ph.appbuilders.offlinehealth.app.LocalUiText
import ph.appbuilders.offlinehealth.app.UiKey
import ph.appbuilders.offlinehealth.app.components.BackBar
import ph.appbuilders.offlinehealth.app.components.ListRow
import ph.appbuilders.offlinehealth.app.theme.Palette
import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.domain.model.TopicCard
import ph.appbuilders.offlinehealth.domain.model.TopicId
import ph.appbuilders.offlinehealth.domain.model.TopicSummary
import ph.appbuilders.offlinehealth.fakes.FakeContentSource
import ph.appbuilders.offlinehealth.fakes.PreviewFrame
import ph.appbuilders.offlinehealth.features.chat.components.FirstAidCard
import ph.appbuilders.offlinehealth.features.chat.components.iconRes

/** D1: every topic as a row. Opening one shows its card with no typing. */
@Composable
fun TopicsScreen(topics: List<TopicSummary>, onTopic: (TopicId) -> Unit, onBack: () -> Unit) {
    val text = LocalUiText.current
    LazyColumn(Modifier.fillMaxSize().background(Palette.Paper).safeDrawingPadding()) {
        item(key = "bar") { BackBar(onBack, text[UiKey.NAV_BACK], title = text[UiKey.MENU_TOPICS]) }
        item(key = "subtitle") {
            Text(
                text[UiKey.TOPICS_SUBTITLE],
                Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = Palette.InkMuted,
            )
        }
        itemsIndexed(topics, key = { _, topic -> topic.topicId }) { index, topic ->
            ListRow(topic.title, onClick = { onTopic(topic.topicId) }, divider = index < topics.lastIndex) {
                Box(Modifier.size(44.dp).background(Palette.Surface, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(painterResource(topic.topicId.iconRes()), null, Modifier.size(24.dp), tint = Palette.Ink)
                }
            }
        }
    }
}

/**
 * D2: one card, full screen, flat. When it was opened from the Topics list the bar says "Topics", so the user
 * sees where back goes. From the chat, back simply returns to the chat.
 */
@Composable
fun TopicDetailScreen(card: TopicCard, fromTopics: Boolean, onBack: () -> Unit) {
    val text = LocalUiText.current
    Column(Modifier.fillMaxSize().background(Palette.Paper).safeDrawingPadding()) {
        if (fromTopics) {
            BackBar(onBack, text[UiKey.NAV_BACK_TO_TOPICS], label = text[UiKey.NAV_TOPICS])
        } else {
            BackBar(onBack, text[UiKey.NAV_BACK])
        }
        FirstAidCard(
            card,
            matchedSigns = emptySet(),
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
            flat = true,
        )
    }
}

@Preview(widthDp = 360, heightDp = 800) @Composable
private fun D1Topics() = PreviewFrame(Language.ENG, padding = 0.dp) {
    TopicsScreen(remember { FakeContentSource().topics(Language.ENG) }, {}, {})
}

@Preview(widthDp = 360, heightDp = 800) @Composable
private fun D2TopicDetail() = PreviewFrame(Language.ENG, padding = 0.dp) {
    TopicDetailScreen(remember { FakeContentSource().card(TopicId.CHILD_DIARRHEA, Language.ENG) }, fromTopics = true) {}
}
