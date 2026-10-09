package ph.appbuilders.offlinehealth.domain.model

/** State of the AI reply block. Names match `AIReply.dc.html`. */
sealed interface AiReplyState {
    data object Thinking : AiReplyState
    data object WarmingUp : AiReplyState       // sent while the model is still starting
    data class Streaming(val text: String) : AiReplyState
    data class Done(val text: String) : AiReplyState
    data object Withheld : AiReplyState        // guardrail blocked it: quiet "Follow the card above"
    data object BasicMode : AiReplyState       // no model on this phone
}

/** Everything the chat shows for one sent message. */
data class ChatResult(
    val dangers: List<DangerMessage>,          // banner shows max 3
    val card: TopicCard?,                      // null + dangers = danger only (C10); null + none = not covered (C9)
    val matchedSigns: Set<Int>,                // indexes into card.goNowIf to highlight
    val otherTopics: List<TopicSummary>,       // "Also about" chips
    val ai: AiReplyState?,                     // null = no AI block (no topic matched)
)

/** The TopBar status pill. */
enum class AiStatus { READY, STARTING, BASIC }
