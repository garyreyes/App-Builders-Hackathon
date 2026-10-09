package ph.appbuilders.offlinehealth.features.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ph.appbuilders.offlinehealth.app.LocalUiText
import ph.appbuilders.offlinehealth.app.UiKey
import ph.appbuilders.offlinehealth.domain.model.TopicSummary
import ph.appbuilders.offlinehealth.features.chat.components.AiReply
import ph.appbuilders.offlinehealth.features.chat.components.AlsoAboutChips
import ph.appbuilders.offlinehealth.features.chat.components.ChatIntro
import ph.appbuilders.offlinehealth.features.chat.components.CheckedSteps
import ph.appbuilders.offlinehealth.features.chat.components.DangerBanner
import ph.appbuilders.offlinehealth.features.chat.components.ExampleChips
import ph.appbuilders.offlinehealth.features.chat.components.FirstAidCard
import ph.appbuilders.offlinehealth.features.chat.components.NotCoveredCard
import ph.appbuilders.offlinehealth.features.chat.components.TopicShortcuts
import ph.appbuilders.offlinehealth.features.chat.components.UserMessage

/** Renders one [ChatItem] with its component. */
@Composable
fun ChatRow(item: ChatItem, topics: List<TopicSummary>, actions: ChatActions, compact: Boolean, modifier: Modifier) {
    val text = LocalUiText.current
    when (item) {
        ChatItem.Intro -> ChatIntro(modifier)
        ChatItem.Examples -> ExampleChips(actions.onExample, modifier)
        ChatItem.Shortcuts -> TopicShortcuts(text[UiKey.CHAT_TOPICS], topics, actions.onTopic, modifier)
        is ChatItem.User -> UserMessage(item.text, modifier)
        is ChatItem.Banner -> DangerBanner(item.dangers, actions.onCall911, modifier, compact)
        is ChatItem.Card ->
            if (item.compact) CheckedSteps(item.card, onOpen = { actions.onTopic(item.card.topicId) }, modifier)
            else FirstAidCard(item.card, item.matched, modifier)
        is ChatItem.NotCovered -> NotCoveredCard(modifier)
        is ChatItem.NotCoveredTopics -> TopicShortcuts(text[UiKey.NOT_COVERED_TOPICS], topics, actions.onTopic, modifier)
        is ChatItem.Ai -> AiReply(item.state, onDownloadAi = actions.onOpenSettings, modifier = modifier)
        is ChatItem.AlsoAbout -> AlsoAboutChips(item.topics, actions.onTopic, modifier)
    }
}
