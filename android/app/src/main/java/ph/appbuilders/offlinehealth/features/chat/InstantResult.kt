package ph.appbuilders.offlinehealth.features.chat

import ph.appbuilders.offlinehealth.content.DemoCards
import ph.appbuilders.offlinehealth.content.DemoContentSource
import ph.appbuilders.offlinehealth.domain.Triage
import ph.appbuilders.offlinehealth.domain.model.ChatResult
import ph.appbuilders.offlinehealth.domain.model.DangerMessage
import ph.appbuilders.offlinehealth.domain.model.Language

/**
 * The instant, model-free answer to a message (ARCHITECTURE "Send pipeline" step 2): danger messages plus the top
 * topic's card, complete and safe on its own. The AI reply is added later by the chat service.
 */
class InstantResult(private val content: DemoContentSource) {

    fun of(text: String, language: Language): ChatResult {
        val match = Triage.match(text)
        val dangers = match.dangers.map { DangerMessage(it, content.dangerMessage(it, language)) }
        val top = match.topics.firstOrNull()
            ?: return ChatResult(dangers, card = null, emptySet(), emptyList(), ai = null)
        val matchedSigns = match.dangers.mapNotNull { DemoCards.signIndex(top, it) }.toSet()
        // "Also about": the other matched topics first, then the rest of the library.
        val others = content.topics(language)
            .filter { it.topicId != top }
            .sortedBy { if (it.topicId in match.topics) 0 else 1 }
        return ChatResult(dangers, content.card(top, language), matchedSigns, others, ai = null)
    }
}
