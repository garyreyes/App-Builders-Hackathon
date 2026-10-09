package ph.appbuilders.offlinehealth.lib.llm

import java.io.IOException
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OllamaNdjsonTest {

    @Test fun parsesAStreamedChunk() {
        val chunk = OllamaNdjson.parse("""{"model":"health-chat","message":{"role":"assistant","content":"Hatagi"},"done":false}""")
        assertEquals("Hatagi", chunk.content)
        assertFalse(chunk.done)
    }

    @Test fun parsesTheFinalChunk() {
        val chunk = OllamaNdjson.parse("""{"model":"health-chat","message":{"role":"assistant","content":""},"done":true,"done_reason":"stop"}""")
        assertEquals("", chunk.content)
        assertTrue(chunk.done)
    }

    @Test(expected = IOException::class) fun errorLineThrows() {
        OllamaNdjson.parse("""{"error":"model 'health-chat' not found"}""")
    }

    @Test(expected = IOException::class) fun garbageLineThrows() {
        OllamaNdjson.parse("not json")
    }

    @Test fun requestWithoutHistoryIsOneUserMessage() {
        val json = JSONObject(OllamaNdjson.chatRequest("health-chat", null, emptyList(), "Hilanat", stream = true))
        assertEquals("health-chat", json.getString("model"))
        assertTrue(json.getBoolean("stream"))
        val messages = json.getJSONArray("messages")
        assertEquals(1, messages.length())
        assertEquals("user", messages.getJSONObject(0).getString("role"))
        assertEquals("Hilanat", messages.getJSONObject(0).getString("content"))
    }

    @Test fun warmUpRequestHasNoMessages() {
        val json = JSONObject(OllamaNdjson.chatRequest("health-chat", null, emptyList(), userText = null, stream = false))
        assertEquals(0, json.getJSONArray("messages").length())
    }

    @Test fun historyComesFirstInOrder() {
        val history = listOf(Exchange("Hilanat", "Uminom hin tubig."))
        val messages = JSONObject(OllamaNdjson.chatRequest("health-chat", null, history, "Pira ka adlaw?", stream = true))
            .getJSONArray("messages")
        assertEquals(3, messages.length())
        assertEquals("user" to "Hilanat", messages.getJSONObject(0).let { it.getString("role") to it.getString("content") })
        assertEquals("assistant" to "Uminom hin tubig.", messages.getJSONObject(1).let { it.getString("role") to it.getString("content") })
        assertEquals("user" to "Pira ka adlaw?", messages.getJSONObject(2).let { it.getString("role") to it.getString("content") })
    }

    @Test fun systemPromptComesFirstAndThinkingIsOff() {
        val json = JSONObject(OllamaNdjson.chatRequest("health-chat", "Be safe.", emptyList(), "Hilanat", stream = true))
        assertFalse(json.getBoolean("think"))
        val first = json.getJSONArray("messages").getJSONObject(0)
        assertEquals("system", first.getString("role"))
        assertEquals("Be safe.", first.getString("content"))
        assertEquals(2, json.getJSONArray("messages").length())
    }
}
