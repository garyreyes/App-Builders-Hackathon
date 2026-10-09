package ph.appbuilders.offlinehealth.features.chat

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import ph.appbuilders.offlinehealth.domain.model.AiStatus
import ph.appbuilders.offlinehealth.domain.model.ChatResult
import ph.appbuilders.offlinehealth.domain.model.Language

/**
 * Contract the core session implements (triage + library + llm + guardrail). The UI only sees this.
 * The first emission is the instant triage result (ai = Thinking/WarmingUp/BasicMode), then AI updates follow.
 */
interface ChatService {
    fun send(text: String, language: Language): Flow<ChatResult>
    val aiStatus: StateFlow<AiStatus>
}
