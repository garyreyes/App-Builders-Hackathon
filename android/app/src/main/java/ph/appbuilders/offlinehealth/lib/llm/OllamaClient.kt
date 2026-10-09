package ph.appbuilders.offlinehealth.lib.llm

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * DEMO BRIDGE (debug builds only): the model runs in Ollama on the laptop, reached over USB with
 * `adb reverse tcp:11434 tcp:11434`. No internet is involved. See ollama/README.md.
 * The only network code in the app until the model downloader lands.
 */
class OllamaClient(private val baseUrl: String, private val model: String) : LlmClient {

    override suspend fun warmUp(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Empty messages: Ollama only loads the model.
            post(OllamaNdjson.chatRequest(model, system = null, emptyList(), userText = null, stream = false)).use { it.readText() }
            true
        } catch (_: IOException) {
            false
        }
    }

    override fun reply(system: String?, history: List<Exchange>, userText: String): Flow<String> = flow {
        val text = StringBuilder()
        post(OllamaNdjson.chatRequest(model, system, history, userText, stream = true)).use { reader ->
            while (true) {
                currentCoroutineContext().ensureActive()
                val line = reader.readLine() ?: break
                if (line.isBlank()) continue
                val chunk = OllamaNdjson.parse(line)
                text.append(chunk.content)
                emit(text.toString())
                if (chunk.done) break
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun post(body: String): java.io.BufferedReader {
        val connection = (URL("$baseUrl/api/chat").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
        }
        connection.outputStream.use { it.write(body.toByteArray()) }
        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            val detail = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            connection.disconnect()
            throw IOException("Ollama HTTP ${connection.responseCode}: $detail")
        }
        return connection.inputStream.bufferedReader()
    }

    private companion object {
        const val CONNECT_TIMEOUT_MS = 2_000
        const val READ_TIMEOUT_MS = 60_000
    }
}
