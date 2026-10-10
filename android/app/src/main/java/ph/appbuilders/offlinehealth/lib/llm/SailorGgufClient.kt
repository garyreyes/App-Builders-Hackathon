package ph.appbuilders.offlinehealth.lib.llm

import android.content.Context
import com.arm.aichat.AiChat
import com.arm.aichat.InferenceEngine
import java.io.File
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex

/** Runs the trained Sailor2 GGUF in this Android process through llama.cpp. */
class SailorGgufClient(context: Context, private val modelFile: File) : LlmClient {
    private val engine = AiChat.getInferenceEngine(context.applicationContext)
    private val lock = Mutex()
    private var loaded = false

    override suspend fun warmUp(): Boolean {
        lock.lock()
        try {
            if (loaded) return true
            if (!modelFile.isFile) return false
            try {
                val state = engine.state.first {
                    it is InferenceEngine.State.Initialized || it is InferenceEngine.State.Error
                }
                if (state is InferenceEngine.State.Error) return false
                engine.loadModel(modelFile.absolutePath)
                loaded = true
                return true
            } catch (_: Exception) {
                return false
            }
        } finally {
            lock.unlock()
        }
    }

    override fun reply(system: String?, history: List<Exchange>, userText: String): Flow<String> = flow {
        lock.lock()
        try {
            if (!loaded) throw IOException("on-device Sailor2 model not loaded")
            val prompt = buildString {
                append(system.orEmpty().ifBlank { "You are a helpful Waray health assistant." })
                if (history.isNotEmpty()) {
                    append("\nPrevious conversation:\n")
                    history.forEach {
                        append("User: ").append(it.user).append('\n')
                        append("Assistant: ").append(it.assistant).append('\n')
                    }
                    append("Answer the latest user message below.\n")
                }
            }
            try {
                // The native call clears its context and applies the GGUF chat template.
                engine.setSystemPrompt(prompt)
                val answer = StringBuilder()
                engine.sendUserPrompt(userText, MAX_OUTPUT_TOKENS).collect { token ->
                    answer.append(token)
                    emit(answer.toString())
                }
            } catch (e: Exception) {
                throw IOException("on-device Sailor2 generation failed", e)
            }
        } finally {
            lock.unlock()
        }
    }

    private companion object {
        const val MAX_OUTPUT_TOKENS = 220
    }
}
