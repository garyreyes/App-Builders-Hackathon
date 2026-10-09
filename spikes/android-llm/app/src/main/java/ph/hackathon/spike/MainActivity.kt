package ph.hackathon.spike

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.SamplerConfig
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Spike only: proves a small LLM answers a grounded health question offline on this phone.
// Models are side-loaded with: adb push <model>.litertlm /sdcard/Android/data/ph.hackathon.spike/files/

private const val SYSTEM_PROMPT =
    "You are an offline health helper. Answer ONLY using the GUIDE. " +
        "Reply in the SAME language the user wrote in, in 2-3 short sentences. " +
        "Never diagnose or give medicine doses. If the GUIDE does not cover it, " +
        "say to go to the nearest health center."

private const val GUIDE =
    "GUIDE (child diarrhea): Give more fluids than usual. Give ORS (oral rehydration " +
        "solution) after each loose stool. Keep breastfeeding and feeding. Go to the health " +
        "center NOW if the child cannot drink or breastfeed, vomits everything, has blood in " +
        "the stool, is very sleepy, or has sunken eyes."

// Test inputs only; the team's native speaker replaces these with real phrasing.
private val PRESETS = listOf(
    "Bisaya" to "Tulo na ka adlaw nga nagkalibang ang akong anak. Unsa may akong buhaton?",
    "Waray" to "Tulo na ka adlaw nga nagkakalibang an akon anak. Ano an akon buhaton?",
    "Taglish" to "Tatlong araw nang nagtatae yung anak ko, ano dapat gawin ko?",
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val modelDir = getExternalFilesDir(null)!!
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) { SpikeScreen(modelDir, cacheDir.path) }
            }
        }
    }
}

private class SpikeState {
    var engine: Engine? = null
    var status by mutableStateOf("Pick a model to load.")
    var output by mutableStateOf("")
    var prompt by mutableStateOf(PRESETS[0].second)
    var busy by mutableStateOf(false)
}

@Composable
private fun SpikeScreen(modelDir: File, cacheDir: String) {
    val state = remember { SpikeState() }
    val scope = rememberCoroutineScope()
    val models = remember { modelDir.listFiles { f -> f.name.endsWith(".litertlm") }.orEmpty().toList() }
    DisposableEffect(Unit) { onDispose { state.engine?.close() } }

    Column(
        Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Offline LLM spike", style = MaterialTheme.typography.titleLarge)
        if (models.isEmpty()) Text("No .litertlm files in:\n${modelDir.path}")
        models.forEach { model ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(enabled = !state.busy, onClick = {
                    scope.launch { loadModel(state, model, Backend.CPU(), cacheDir) }
                }) { Text("CPU: ${model.name.take(22)}") }
                OutlinedButton(enabled = !state.busy, onClick = {
                    scope.launch { loadModel(state, model, Backend.GPU(), cacheDir) }
                }) { Text("GPU") }
            }
        }
        Text(state.status, style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PRESETS.forEach { (label, text) ->
                OutlinedButton(onClick = { state.prompt = text }) { Text(label) }
            }
        }
        OutlinedTextField(state.prompt, { state.prompt = it }, Modifier.fillMaxWidth())
        Button(enabled = state.engine != null && !state.busy, onClick = {
            scope.launch { ask(state) }
        }) { Text("Ask (offline)") }
        Text(state.output)
    }
}

private suspend fun loadModel(state: SpikeState, model: File, backend: Backend, cacheDir: String) {
    state.busy = true
    state.status = "Loading ${model.name} (${model.length() / 1_000_000} MB)…"
    val start = SystemClock.elapsedRealtime()
    try {
        withContext(Dispatchers.IO) {
            state.engine?.close()
            val engine = Engine(EngineConfig(modelPath = model.path, backend = backend, cacheDir = cacheDir))
            engine.initialize()
            state.engine = engine
        }
        state.status = "Loaded ${model.name} on $backend in ${SystemClock.elapsedRealtime() - start} ms"
    } catch (t: Throwable) {
        state.engine = null
        state.status = "Load FAILED on $backend: ${t.message}"
    } finally {
        state.busy = false
    }
}

private suspend fun ask(state: SpikeState) {
    val engine = state.engine ?: return
    state.busy = true
    state.output = ""
    val start = SystemClock.elapsedRealtime()
    var firstTokenMs = -1L
    try {
        val config = ConversationConfig(
            systemInstruction = Contents.of(SYSTEM_PROMPT),
            samplerConfig = SamplerConfig(topK = 10, topP = 0.95, temperature = 0.3),
        )
        engine.createConversation(config).use { conversation ->
            conversation.sendMessageAsync("$GUIDE\n\nUSER: ${state.prompt}").collect { chunk ->
                if (firstTokenMs < 0) firstTokenMs = SystemClock.elapsedRealtime() - start
                state.output += chunk.toString()
            }
        }
        val totalMs = SystemClock.elapsedRealtime() - start
        val charsPerSec = state.output.length * 1000 / maxOf(totalMs - firstTokenMs, 1)
        state.status = "First token ${firstTokenMs} ms · total ${totalMs} ms · ~$charsPerSec chars/s"
    } catch (t: Throwable) {
        state.status = "Ask FAILED: ${t.message}"
    } finally {
        state.busy = false
    }
}
