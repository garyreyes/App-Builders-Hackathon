package ph.appbuilders.offlinehealth.app

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.domain.model.TopicSummary
import ph.appbuilders.offlinehealth.features.chat.ChatActions
import ph.appbuilders.offlinehealth.features.chat.ChatScreen
import ph.appbuilders.offlinehealth.features.chat.ChatViewModel
import ph.appbuilders.offlinehealth.features.chat.components.MenuAction
import ph.appbuilders.offlinehealth.features.gallery.ComponentGallery

/** The app's screens, switched with a plain `when` (no navigation library). PR 4 adds language, setup, topics, settings. */
sealed interface Screen {
    data object Chat : Screen
    data object Gallery : Screen   // debug builds only
}

/**
 * Root of the UI: provides the copy for the current language and switches screens.
 * Thin layer: it reads the language setting and hands each screen its ViewModel and callbacks.
 */
@Composable
fun AppNavigation(container: AppContainer) {
    val saved by container.languageSettings.language.collectAsState()
    val language = saved ?: Language.CEB // PR 4: no language yet → language picker
    val uiText = remember(language) { UiText(language, container.content::uiString) }
    val topics = remember(language) { container.content.topics(language) }
    var screen by remember { mutableStateOf<Screen>(Screen.Chat) }
    BackHandler(enabled = screen != Screen.Chat) { screen = Screen.Chat }

    val debugItems = if (container.debugActions.isEmpty()) emptyList() else {
        container.debugActions + MenuAction("Debug: component gallery") { screen = Screen.Gallery }
    }
    ProvideUiText(uiText) {
        when (screen) {
            Screen.Chat -> ChatDestination(container, topics, debugItems)
            Screen.Gallery -> ComponentGallery()
        }
    }
}

@Composable
private fun ChatDestination(container: AppContainer, topics: List<TopicSummary>, debugItems: List<MenuAction>) {
    val viewModel = viewModel { ChatViewModel(container.chatService) }
    val state by viewModel.state.collectAsState()
    val aiStatus by viewModel.aiStatus.collectAsState()
    val language = LocalUiText.current.language
    val context = LocalContext.current
    val actions = remember(viewModel, language) {
        ChatActions(
            onDraftChange = viewModel::onDraftChange,
            onSend = { viewModel.send(language) },
            onExample = viewModel::useExample,
            onTopic = {},          // PR 4: open the topic card
            onLanguagePick = container.languageSettings::setLanguage,
            onOpenTopics = {},     // PR 4: topics screen
            onOpenSettings = {},   // PR 4: settings / AI setup
            onCall911 = { context.dial911() },
        )
    }
    ChatScreen(state, aiStatus, topics, actions, debugItems)
}

/** Opens the dialer at 911 (no permission needed; emergency calls can work without data or load). */
private fun Context.dial911() {
    try {
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:911")))
    } catch (_: ActivityNotFoundException) {
        // No dialer on this device (some tablets). The banner text still says to call 911.
    }
}
