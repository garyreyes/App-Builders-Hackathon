package ph.appbuilders.offlinehealth.lib.llm

import kotlinx.coroutines.flow.Flow

/** One earlier question and the reply the user actually saw (guardrail-passed). */
data class Exchange(val user: String, val assistant: String)

/**
 * The one way the app talks to a language model. Today: Ollama on the laptop ([OllamaClient], debug builds).
 * Later: llama.cpp on the phone, behind the same interface.
 */
interface LlmClient {
    /** Loads the model so the first reply isn't slow. True if the model is reachable and loaded. */
    suspend fun warmUp(): Boolean

    /**
     * A reply to [userText] after the earlier [history], as the cumulative text so far.
     * Throws [java.io.IOException] on failure.
     */
    fun reply(history: List<Exchange>, userText: String): Flow<String>
}
