package ph.appbuilders.offlinehealth.lib.llm

import org.json.JSONArray
import org.json.JSONObject

/** Ollama `/api/chat` wire format: request bodies and one streamed NDJSON line at a time. */
internal object OllamaNdjson {

    class Chunk(val content: String, val done: Boolean)

    /** One single-turn user message, as in training. Sampling settings live in ollama/Modelfile. */
    fun chatRequest(model: String, userText: String?, stream: Boolean): String = JSONObject()
        .put("model", model)
        .put("stream", stream)
        .put("keep_alive", KEEP_ALIVE)
        .put(
            "messages",
            JSONArray().apply {
                if (userText != null) put(JSONObject().put("role", "user").put("content", userText))
            },
        )
        .toString()

    /** Throws [java.io.IOException] when Ollama reports an error (unknown model, crash). */
    fun parse(line: String): Chunk {
        val json = try {
            JSONObject(line)
        } catch (e: org.json.JSONException) {
            throw java.io.IOException("Ollama: unreadable line", e)
        }
        if (json.has("error")) throw java.io.IOException("Ollama: ${json.getString("error")}")
        val content = json.optJSONObject("message")?.optString("content").orEmpty()
        return Chunk(content, json.optBoolean("done", false))
    }

    /** Keeps the model in memory between questions during the demo. */
    private const val KEEP_ALIVE = "30m"
}
