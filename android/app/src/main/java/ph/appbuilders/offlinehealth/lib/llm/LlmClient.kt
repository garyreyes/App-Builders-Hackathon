package ph.appbuilders.offlinehealth.lib.llm

import kotlinx.coroutines.flow.Flow

/**
 * The one way the app talks to a language model. Today: Ollama on the laptop ([OllamaClient], debug builds).
 * Later: llama.cpp on the phone, behind the same interface.
 */
interface LlmClient {
    /** Loads the model so the first reply isn't slow. True if the model is reachable and loaded. */
    suspend fun warmUp(): Boolean

    /** One single-turn reply to [userText], as the cumulative text so far. Throws [java.io.IOException] on failure. */
    fun reply(userText: String): Flow<String>
}
