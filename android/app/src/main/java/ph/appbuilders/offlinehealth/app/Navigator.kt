package ph.appbuilders.offlinehealth.app

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ph.appbuilders.offlinehealth.domain.model.TopicId

/**
 * The app's screens, switched with a plain `when` (no navigation library). The language picker isn't here:
 * it shows whenever no language is saved yet, whatever the stack says (see AppNavigation).
 */
sealed interface Screen {
    data object Chat : Screen
    data object Setup : Screen
    data object Topics : Screen
    data class TopicDetail(val topicId: TopicId, val fromTopics: Boolean) : Screen
    data object Settings : Screen
    data object About : Screen
    data object Sources : Screen
    data object Gallery : Screen   // debug builds only
}

/** A back stack in a ViewModel, so the open screen survives rotation. The bottom entry is never popped. */
class Navigator : ViewModel() {
    private val _stack = MutableStateFlow<List<Screen>>(listOf(Screen.Chat))
    val stack: StateFlow<List<Screen>> = _stack.asStateFlow()

    fun push(screen: Screen) = _stack.update { if (it.last() == screen) it else it + screen }

    fun pop() = _stack.update { if (it.size > 1) it.dropLast(1) else it }

    /** Starts over at [screen], e.g. first-run setup, or back to the chat once setup is finished. */
    fun resetTo(screen: Screen) {
        _stack.value = listOf(screen)
    }
}
