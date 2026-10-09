package ph.appbuilders.offlinehealth.features.chat

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ph.appbuilders.offlinehealth.domain.model.AiReplyState
import ph.appbuilders.offlinehealth.domain.model.AiStatus
import ph.appbuilders.offlinehealth.domain.model.ChatResult
import ph.appbuilders.offlinehealth.domain.model.Language

/** One sent message and what the service answered. [result] is null only until the instant triage arrives. */
data class ChatTurn(val id: Long, val userText: String, val result: ChatResult?)

data class ChatUiState(val draft: String = "", val turns: List<ChatTurn> = emptyList())

/**
 * Holds the draft and the conversation (in memory only) and calls [ChatService]. No medical logic here:
 * everything the user sees about their message comes from the service's [ChatResult].
 */
class ChatViewModel(private val chat: ChatService) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()
    val aiStatus: StateFlow<AiStatus> = chat.aiStatus

    private var nextId = 0L

    fun onDraftChange(text: String) = _state.update { it.copy(draft = text) }

    /** Example chips fill the field; they never send (HANDOFF rule 5). */
    fun useExample(text: String) = onDraftChange(text)

    fun send(language: Language) {
        val text = _state.value.draft.trim()
        if (text.isEmpty()) return
        val id = nextId++
        _state.update { it.copy(draft = "", turns = it.turns + ChatTurn(id, text, result = null)) }
        // Main.immediate: the first (instant) emission lands before the next frame, so the bubble, banner,
        // and card appear together. Streaming updates are batched to about one every 50 ms.
        viewModelScope.launch {
            var lastShown = 0L
            chat.send(text, language).collect { result ->
                val now = SystemClock.uptimeMillis()
                if (result.ai is AiReplyState.Streaming && now - lastShown < STREAM_BATCH_MS) return@collect
                lastShown = now
                _state.update { s -> s.copy(turns = s.turns.map { if (it.id == id) it.copy(result = result) else it }) }
            }
        }
    }

    private companion object {
        const val STREAM_BATCH_MS = 50L
    }
}
