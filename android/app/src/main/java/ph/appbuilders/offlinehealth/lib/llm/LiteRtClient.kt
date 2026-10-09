package ph.appbuilders.offlinehealth.lib.llm

import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.SamplerConfig
import com.google.ai.edge.litertlm.ThinkingConfig
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * The model running ON THE PHONE (Google LiteRT-LM): no laptop, no network, works in airplane mode.
 * The model file is side-loaded into the app's own storage (see ollama/README.md, "On the phone").
 * GPU first, CPU if the GPU can't load it. Same prompt, history, and guardrail path as the Ollama bridge.
 */
class LiteRtClient(private val modelFile: File, private val cacheDir: String) : LlmClient {

    private val loadLock = Mutex()
    private val generationLock = Mutex()

    /** Outlives any one screen or request, so a generation is never torn down halfway (see [reply]). */
    private val generationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var engine: Engine? = null

    /** Which backend loaded, for Settings and the docs ("GPU" or "CPU"); null until loaded. */
    @Volatile
    var backendName: String? = null
        private set

    override suspend fun warmUp(): Boolean = withContext(Dispatchers.IO) {
        loadLock.withLock {
            if (engine != null) return@withLock true
            if (!modelFile.isFile) return@withLock false
            load(Backend.GPU(), "GPU") || load(Backend.CPU(), "CPU")
        }
    }

    private fun load(backend: Backend, name: String): Boolean = try {
        val loaded = Engine(EngineConfig(modelPath = modelFile.path, backend = backend, cacheDir = cacheDir))
        loaded.initialize()
        engine = loaded
        backendName = name
        true
    } catch (e: Exception) {
        false
    }

    /**
     * Streams one reply. The native generation always runs to its end on [generationScope] and only then closes
     * its conversation: closing it mid-generation (a guardrail stop, a cancelled screen) crashed the native
     * callback thread (SIGSEGV, emulator, Oct 10). Cancelling this flow only stops listening. One at a time.
     */
    override fun reply(system: String?, history: List<Exchange>, userText: String): Flow<String> = channelFlow {
        val loaded = engine ?: throw IOException("on-phone model not loaded")
        val config = ConversationConfig(
            systemInstruction = Contents.of(system.orEmpty()),
            initialMessages = history.flatMap { listOf(Message.user(it.user), Message.model(it.assistant)) },
            // topK = 1 is greedy decoding, as in the model comparison (temperature 0 there).
            samplerConfig = SamplerConfig(topK = 1, topP = 1.0, temperature = 1.0),
            maxOutputToken = MAX_OUTPUT_TOKENS,
            thinkingConfig = ThinkingConfig(false),
        )
        val failure = CompletableDeferred<Throwable?>()
        generationScope.launch {
            generationLock.withLock {
                val text = StringBuilder()
                val error = try {
                    loaded.createConversation(config).use { conversation ->
                        conversation.sendMessageAsync(userText).collect { chunk ->
                            text.append(chunk.toString())
                            trySend(text.toString()) // fails quietly once the listener is gone
                        }
                    }
                    null
                } catch (e: Exception) {
                    e
                }
                failure.complete(error)
            }
        }
        failure.await()?.let { throw IOException("on-phone model failed: ${it.message}", it) }
    }

    private companion object {
        const val MAX_OUTPUT_TOKENS = 220
    }
}
