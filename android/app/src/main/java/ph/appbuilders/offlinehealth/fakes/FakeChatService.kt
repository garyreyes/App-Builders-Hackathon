package ph.appbuilders.offlinehealth.fakes

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import ph.appbuilders.offlinehealth.content.ContentSource
import ph.appbuilders.offlinehealth.domain.model.AiReplyState
import ph.appbuilders.offlinehealth.domain.model.AiStatus
import ph.appbuilders.offlinehealth.domain.model.ChatResult
import ph.appbuilders.offlinehealth.domain.model.DangerMessage
import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.domain.model.TopicId
import ph.appbuilders.offlinehealth.features.chat.ChatService

/**
 * FAKE: canned results so every chat state can be reached from the real UI (frontend brief §4). The words it looks
 * for only pick a demo script. This is NOT triage: the core's ChatService (glossary + guardrail) replaces it.
 *
 * | Message contains            | Result                                              |
 * |-----------------------------|-----------------------------------------------------|
 * | toothache / ngipon          | not covered (C9)                                    |
 * | seizure / kombulsyon        | danger only, no card (C10)                          |
 * | withheld                    | card, then the reply is withheld (C6)               |
 * | dugo / blood                | danger + diarrhea card + streamed reply (C3–C5)     |
 * | sleepy / vomit              | 3 dangers + 3 matched signs (C11)                   |
 * | anything else               | diarrhea card + streamed reply                      |
 *
 * The AI starts as STARTING and is READY after 3 s. [toggleBasicMode] is the debug-only switch to BASIC.
 * The fake setup calls [useBasicMode] and [onModelReady], so the pill follows what the user chose there.
 */
class FakeChatService(private val content: ContentSource, private val scope: CoroutineScope) : ChatService {

    private val status = MutableStateFlow(AiStatus.STARTING)
    override val aiStatus: StateFlow<AiStatus> = status.asStateFlow()

    init {
        restartWarmUp(WARM_UP_MS)
    }

    /** "Use basic mode for now" on the setup screen. */
    fun useBasicMode() {
        status.value = AiStatus.BASIC
    }

    /** Setup finished: the model starts like it does at launch. */
    fun onModelReady() = restartWarmUp(WARM_UP_MS)

    /** Debug-only too: back to STARTING for long enough to send a message and see "AI warming up" (C8). */
    fun restartWarmUp(durationMs: Long = DEBUG_WARM_UP_MS) {
        status.value = AiStatus.STARTING
        scope.launch {
            delay(durationMs)
            if (status.value == AiStatus.STARTING) status.value = AiStatus.READY
        }
    }

    fun toggleBasicMode() {
        status.value = if (status.value == AiStatus.BASIC) AiStatus.READY else AiStatus.BASIC
    }

    override fun send(text: String, language: Language): Flow<ChatResult> = flow {
        val script = scriptFor(text.lowercase(), language)
        if (script.result.card == null) {
            emit(script.result)
            return@flow
        }
        val first = script.result.copy(ai = firstAiState())
        emit(first)
        if (first.ai == AiReplyState.BasicMode) return@flow
        if (status.first { it != AiStatus.STARTING } == AiStatus.BASIC) {
            emit(first.copy(ai = AiReplyState.BasicMode))
            return@flow
        }
        emit(first.copy(ai = AiReplyState.Thinking))
        delay(THINK_MS)
        if (script.reply == null) emit(first.copy(ai = AiReplyState.Withheld)) else stream(first, script.reply)
    }

    private fun firstAiState(): AiReplyState = when (status.value) {
        AiStatus.READY -> AiReplyState.Thinking
        AiStatus.STARTING -> AiReplyState.WarmingUp
        AiStatus.BASIC -> AiReplyState.BasicMode
    }

    private suspend fun FlowCollector<ChatResult>.stream(base: ChatResult, reply: String) {
        val words = reply.split(" ")
        for (count in 1 until words.size) {
            emit(base.copy(ai = AiReplyState.Streaming(words.take(count).joinToString(" "))))
            delay(WORD_MS)
        }
        emit(base.copy(ai = AiReplyState.Done(reply)))
    }

    private class Script(val result: ChatResult, val reply: String?)

    private fun scriptFor(text: String, language: Language): Script {
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

    private companion object {
        const val WARM_UP_MS = 3_000L
        const val DEBUG_WARM_UP_MS = 30_000L
        const val THINK_MS = 1_200L
        const val WORD_MS = 80L
    }
}
