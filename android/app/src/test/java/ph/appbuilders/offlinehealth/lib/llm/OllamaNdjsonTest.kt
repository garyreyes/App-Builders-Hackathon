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

    @Test fun requestIsOneUserMessage() {
        val json = JSONObject(OllamaNdjson.chatRequest("health-chat", "Hilanat", stream = true))
        assertEquals("health-chat", json.getString("model"))
        assertTrue(json.getBoolean("stream"))
        val messages = json.getJSONArray("messages")
        assertEquals(1, messages.length())
        assertEquals("user", messages.getJSONObject(0).getString("role"))
        assertEquals("Hilanat", messages.getJSONObject(0).getString("content"))
    }

    @Test fun warmUpRequestHasNoMessages() {
        val json = JSONObject(OllamaNdjson.chatRequest("health-chat", userText = null, stream = false))
        assertEquals(0, json.getJSONArray("messages").length())
    }
}
