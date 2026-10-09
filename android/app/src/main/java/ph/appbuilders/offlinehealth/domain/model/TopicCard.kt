package ph.appbuilders.offlinehealth.domain.model

/** A pre-translated first-aid card in one language. */
data class TopicCard(
    val topicId: TopicId,
    val title: String,
    val atHome: List<String>,
    val goNowIf: List<String>,
    val source: String,
)

/** A pre-translated danger message in one language. Never model-written. */
data class DangerMessage(val id: DangerSignId, val text: String)

/** A topic's title, for topic lists and "Also about" chips. */
data class TopicSummary(val topicId: TopicId, val title: String)
