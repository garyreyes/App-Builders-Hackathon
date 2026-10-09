package ph.appbuilders.offlinehealth.features.chat

import ph.appbuilders.offlinehealth.domain.model.AiReplyState
import ph.appbuilders.offlinehealth.domain.model.DangerMessage
import ph.appbuilders.offlinehealth.domain.model.TopicCard
import ph.appbuilders.offlinehealth.domain.model.TopicSummary

/**
 * One row in the chat list. Display mapping only: which blocks a [ChatResult][ph.appbuilders.offlinehealth.domain.model.ChatResult]
 * shows. AI-first (owner, Oct 10): bubble, danger banner, AI reply, the card folded into a row, "Also about".
 * When the AI can't answer (withheld, basic mode, none), the full card or the not-covered block takes its place.
 * No medical decisions here.
 */
sealed interface ChatItem {
    val key: String

    data object Intro : ChatItem { override val key = "intro" }
    data object Examples : ChatItem { override val key = "examples" }
    data object Shortcuts : ChatItem { override val key = "shortcuts" }

    data class User(val turnId: Long, val text: String) : ChatItem { override val key = "user-$turnId" }
    data class Banner(val turnId: Long, val dangers: List<DangerMessage>) : ChatItem { override val key = "banner-$turnId" }
    /** [compact]: folded into one row under an AI answer; otherwise the full card. */
    data class Card(val turnId: Long, val card: TopicCard, val matched: Set<Int>, val compact: Boolean) : ChatItem {
        override val key = "card-$turnId"
    }
    data class NotCovered(val turnId: Long) : ChatItem { override val key = "notcovered-$turnId" }
    data class NotCoveredTopics(val turnId: Long) : ChatItem { override val key = "notcovered-topics-$turnId" }
    data class Ai(val turnId: Long, val state: AiReplyState) : ChatItem { override val key = "ai-$turnId" }
    data class AlsoAbout(val turnId: Long, val topics: List<TopicSummary>) : ChatItem { override val key = "also-$turnId" }
}

fun chatItems(turns: List<ChatTurn>): List<ChatItem> =
    if (turns.isEmpty()) listOf(ChatItem.Intro, ChatItem.Examples, ChatItem.Shortcuts) else turns.flatMap(::turnItems)

private fun turnItems(turn: ChatTurn): List<ChatItem> = buildList {
    add(ChatItem.User(turn.id, turn.userText))
    val result = turn.result ?: return@buildList
    if (result.dangers.isNotEmpty()) add(ChatItem.Banner(turn.id, result.dangers))
    val ai = result.ai
    val answering = ai != null && ai != AiReplyState.Withheld && ai != AiReplyState.BasicMode
    if (answering) {
        add(ChatItem.Ai(turn.id, ai!!))
        if (result.card != null) add(ChatItem.Card(turn.id, result.card, result.matchedSigns, compact = true))
    } else {
        if (result.card != null) add(ChatItem.Card(turn.id, result.card, result.matchedSigns, compact = false))
        val notCovered = result.card == null && result.dangers.isEmpty()
        if (notCovered) {
            add(ChatItem.NotCovered(turn.id))
            add(ChatItem.NotCoveredTopics(turn.id))
        }
        // A withheld line next to "I can't help with this one" would say the same thing twice.
        if (ai != null && !(notCovered && ai == AiReplyState.Withheld)) add(ChatItem.Ai(turn.id, ai))
    }
    if (result.card != null && result.otherTopics.isNotEmpty()) add(ChatItem.AlsoAbout(turn.id, result.otherTopics))
}

/** True while the AI block of a turn is still on its way (where the "AI reply below" chip may point). */
fun AiReplyState.isPending(): Boolean =
    this == AiReplyState.Thinking || this == AiReplyState.WarmingUp || this is AiReplyState.Streaming
