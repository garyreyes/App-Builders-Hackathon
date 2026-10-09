package ph.appbuilders.offlinehealth.fakes

import ph.appbuilders.offlinehealth.content.ContentSource
import ph.appbuilders.offlinehealth.domain.model.ChatResult
import ph.appbuilders.offlinehealth.domain.model.DangerMessage
import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.domain.model.TopicId

/**
 * FAKE: picks a canned demo result by keyword. This is NOT triage: the glossary-based `Triage` replaces it.
 * Shared by [FakeChatService] (canned reply too) and the Ollama chat (card only; the model writes the reply).
 *
 * | Message contains            | Result                                              |
 * |-----------------------------|-----------------------------------------------------|
 * | toothache / ngipon          | not covered (C9)                                    |
 * | seizure / kombulsyon        | danger only, no card (C10)                          |
 * | withheld                    | card, then the reply is withheld (C6)               |
 * | dugo / blood                | danger + diarrhea card + streamed reply (C3–C5)     |
 * | sleepy / vomit              | 3 dangers + 3 matched signs (C11)                   |
 * | anything else               | diarrhea card + streamed reply                      |
 */
class FakeTriage(private val content: ContentSource) {

    class Script(val result: ChatResult, val reply: String?)

    fun script(message: String, language: Language): Script {
        val text = message.lowercase()
        val ceb = language == Language.CEB
        val has = { words: List<String> -> words.any { it in text } }
        return when {
            has(listOf("toothache", "ngipon")) -> Script(noCard(emptyList()), reply = null)
            has(listOf("seizure", "kombulsyon", "kumbulsyon")) ->
                Script(noCard(if (ceb) FakeSamples.seizureCeb else FakeSamples.seizureEng), reply = null)
            has(listOf("withheld")) -> Script(diarrhea(language, emptyList(), emptySet()), reply = null)
            has(listOf("dugo", "blood")) -> Script(
                diarrhea(language, if (ceb) FakeSamples.dangerCeb else FakeSamples.dangerEng, setOf(2)),
                if (ceb) FakeSamples.REPLY_CEB else FakeSamples.REPLY_ENG_BLOOD,
            )
            has(listOf("sleepy", "vomit")) ->
                Script(diarrhea(language, FakeSamples.dangerEngThree, setOf(0, 1, 3)), FakeSamples.REPLY_ENG)
            else -> Script(diarrhea(language, emptyList(), emptySet()), stepsAsReply(language))
        }
    }

    private fun diarrhea(language: Language, dangers: List<DangerMessage>, matched: Set<Int>): ChatResult {
        val others = content.topics(language).filter { it.topicId == TopicId.FEVER || it.topicId == TopicId.DENGUE_WARNING }
        return ChatResult(dangers, content.card(TopicId.CHILD_DIARRHEA, language), matched, others, ai = null)
    }

    private fun noCard(dangers: List<DangerMessage>) = ChatResult(dangers, null, emptySet(), emptyList(), ai = null)

    /** The generic reply reuses the card's own steps, so no new local-language text is invented. */
    private fun stepsAsReply(language: Language): String =
        content.card(TopicId.CHILD_DIARRHEA, language).atHome.joinToString(" ")
}
