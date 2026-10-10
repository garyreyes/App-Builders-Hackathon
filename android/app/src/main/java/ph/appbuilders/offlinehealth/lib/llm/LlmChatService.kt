package ph.appbuilders.offlinehealth.lib.llm

import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import ph.appbuilders.offlinehealth.domain.Guardrail
import ph.appbuilders.offlinehealth.domain.ReplyText
import ph.appbuilders.offlinehealth.domain.model.AiReplyState
import ph.appbuilders.offlinehealth.domain.model.AiStatus
import ph.appbuilders.offlinehealth.domain.model.ChatResult
import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.features.chat.ChatService

/**
 * The AI chat: messages without danger signs get a model reply, with the instant danger
 * check and topic card from [triage] alongside it. Every piece of reply text passes [Guardrail] before it is
 * emitted; the first failure hides the whole reply. Follow-ups see the last [HISTORY_TURNS] answered exchanges.
 * [prompt] builds each request's system prompt from the instant result (its checked card) and the user's language;
 * null uses the client's default system prompt.
 */
class LlmChatService(
    private val client: LlmClient,
    private val triage: (String, Language) -> ChatResult,
    private val scope: CoroutineScope,
    private val prompt: (ChatResult, Language) -> String? = { _, _ -> null },
) : ChatService {

    private val status = MutableStateFlow(AiStatus.STARTING)
    override val aiStatus: StateFlow<AiStatus> = status.asStateFlow()

    private var warmUpJob: Job? = null

    /** True when the user picked basic mode; then a send must not quietly reconnect. */
    private var basicByChoice = false

    /** Answered exchanges only: a withheld reply (and its question) never goes back into the prompt. */
    private val history = ArrayDeque<Exchange>()

    init {
        warmUp()
    }

    /** (Re)connects: STARTING, then READY, or BASIC if the model can't be reached. */
    fun warmUp() {
        basicByChoice = false
        if (warmUpJob?.isActive == true) return
        status.value = AiStatus.STARTING
        warmUpJob = scope.launch {
            status.value = if (client.warmUp()) AiStatus.READY else AiStatus.BASIC
        }
    }

    /** "Use basic mode for now" on the setup screen. */
    fun useBasicMode() {
        warmUpJob?.cancel()
        basicByChoice = true
        status.value = AiStatus.BASIC
    }

    override fun send(text: String, language: Language): Flow<ChatResult> = flow {
        val base = triage(text, language)
        // An emergency warning must never be followed by unverified model advice. Keep the checked card visible.
        if (base.dangers.isNotEmpty()) {
            emit(base.copy(ai = AiReplyState.Withheld))
            return@flow
        }
        if (status.value == AiStatus.BASIC && !basicByChoice) warmUp()
        emit(base.copy(ai = firstAiState()))
        if (status.first { it != AiStatus.STARTING } == AiStatus.BASIC) {
            emit(base.copy(ai = AiReplyState.BasicMode))
            return@flow
        }
        emit(base.copy(ai = AiReplyState.Thinking))
        streamReply(base, text, prompt(base, language))
    }

    private fun firstAiState(): AiReplyState = when (status.value) {
        AiStatus.READY -> AiReplyState.Thinking
        AiStatus.STARTING -> AiReplyState.WarmingUp
        AiStatus.BASIC -> AiReplyState.BasicMode
    }

    private suspend fun FlowCollector<ChatResult>.streamReply(base: ChatResult, text: String, system: String?) {
        val dangerShown = base.dangers.isNotEmpty()
        var full = ""
        try {
            client.reply(system, history.toList(), text).collect { partial ->
                full = ReplyText.plain(partial)
                // Only whole words are shown, and exactly what is shown has passed the guardrail.
                val shown = ReplyText.visiblePrefix(full)
                if (shown.isEmpty()) return@collect
                if (!Guardrail.allows(shown, dangerShown)) throw Rejected()
                emit(base.copy(ai = AiReplyState.Streaming(shown)))
            }
        } catch (_: Rejected) {
            emit(base.copy(ai = AiReplyState.Withheld))
            return
        } catch (_: IOException) {
            emit(base.copy(ai = AiReplyState.Withheld)) // the card above is still complete and safe
            warmUp() // the pill shows whether the model is still reachable
            return
        }
        val reply = ReplyText.finish(full)
        if (!Guardrail.allows(reply, dangerShown)) {
            emit(base.copy(ai = AiReplyState.Withheld))
            return
        }
        remember(Exchange(text, reply))
        emit(base.copy(ai = AiReplyState.Done(reply)))
    }

    private fun remember(exchange: Exchange) {
        history.addLast(exchange)
        while (history.size > HISTORY_TURNS) history.removeFirst()
    }

    private class Rejected : Exception()

    companion object {
        /** Three short exchanges fit the 2048-token context with room for the reply. */
        const val HISTORY_TURNS = 3
    }
}
