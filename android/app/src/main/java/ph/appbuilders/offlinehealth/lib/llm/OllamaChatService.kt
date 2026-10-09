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
 * Real AI replies from an [LlmClient], under the instant card from [triage] (ARCHITECTURE "Send pipeline").
 * Every piece of reply text passes [Guardrail] before it is emitted; the first failure hides the whole reply.
 * The prompt is the user's text only, exactly as in training (system prompt + sampling live in ollama/Modelfile).
 */
class OllamaChatService(
    private val client: LlmClient,
    private val triage: (String, Language) -> ChatResult,
    private val scope: CoroutineScope,
) : ChatService {

    private val status = MutableStateFlow(AiStatus.STARTING)
    override val aiStatus: StateFlow<AiStatus> = status.asStateFlow()

    private var warmUpJob: Job? = null

    /** True when the user picked basic mode; then a send must not quietly reconnect. */
    private var basicByChoice = false

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
        if (base.card == null) {
            emit(base) // nothing matched: the fixed "not covered" text, no AI (ARCHITECTURE step 3)
            return@flow
        }
        if (status.value == AiStatus.BASIC && !basicByChoice) warmUp() // the laptop may be back
        emit(base.copy(ai = firstAiState()))
        if (status.first { it != AiStatus.STARTING } == AiStatus.BASIC) {
            emit(base.copy(ai = AiReplyState.BasicMode))
            return@flow
        }
        emit(base.copy(ai = AiReplyState.Thinking))
        streamReply(base, text)
    }

    private fun firstAiState(): AiReplyState = when (status.value) {
        AiStatus.READY -> AiReplyState.Thinking
        AiStatus.STARTING -> AiReplyState.WarmingUp
        AiStatus.BASIC -> AiReplyState.BasicMode
    }

    private suspend fun FlowCollector<ChatResult>.streamReply(base: ChatResult, text: String) {
        val dangerShown = base.dangers.isNotEmpty()
        var full = ""
        try {
            client.reply(text).collect { partial ->
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
        val ai = if (Guardrail.allows(reply, dangerShown)) AiReplyState.Done(reply) else AiReplyState.Withheld
        emit(base.copy(ai = ai))
    }

    private class Rejected : Exception()
}
