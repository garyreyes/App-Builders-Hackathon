package ph.appbuilders.offlinehealth.app

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import ph.appbuilders.offlinehealth.BuildConfig
import ph.appbuilders.offlinehealth.features.chat.ChatActions
import ph.appbuilders.offlinehealth.features.chat.ChatScreen
import ph.appbuilders.offlinehealth.features.chat.ChatViewModel
import ph.appbuilders.offlinehealth.features.chat.components.MenuAction
import ph.appbuilders.offlinehealth.features.gallery.ComponentGallery
import ph.appbuilders.offlinehealth.features.modelsetup.SetupActions
import ph.appbuilders.offlinehealth.features.modelsetup.SetupScreen
import ph.appbuilders.offlinehealth.features.settings.AboutScreen
import ph.appbuilders.offlinehealth.features.settings.SettingsActions
import ph.appbuilders.offlinehealth.features.settings.SettingsScreen
import ph.appbuilders.offlinehealth.features.settings.SourcesScreen
import ph.appbuilders.offlinehealth.features.topics.TopicDetailScreen
import ph.appbuilders.offlinehealth.features.topics.TopicsScreen

/** Shows one screen of the stack. Content is read in the current language, so a switch re-renders it. */
@Composable
fun Destination(screen: Screen, container: AppContainer, navigator: Navigator, debugItems: List<MenuAction>) {
    val language = LocalUiText.current.language
    val content = container.content
    val topics = remember(language) { content.topics(language) }
    when (screen) {
        Screen.Chat -> ChatDestination(container, navigator, debugItems)
        Screen.Setup -> SetupDestination(container, navigator)
        Screen.Topics -> TopicsScreen(
            topics,
            onTopic = { navigator.push(Screen.TopicDetail(it, fromTopics = true)) },
            onBack = navigator::pop,
        )
        is Screen.TopicDetail -> TopicDetailScreen(
            remember(screen.topicId, language) { content.card(screen.topicId, language) },
            screen.fromTopics,
            onBack = navigator::pop,
        )
        Screen.Settings -> SettingsDestination(container, navigator)
        Screen.About -> AboutScreen(topics, onBack = navigator::pop)
        Screen.Sources -> SourcesScreen(remember(language) { topics.map { content.card(it.topicId, language) } }, navigator::pop)
        Screen.Gallery -> ComponentGallery()
    }
}

@Composable
private fun ChatDestination(container: AppContainer, navigator: Navigator, debugItems: List<MenuAction>) {
    val viewModel = viewModel { ChatViewModel(container.chatService) }
    val state by viewModel.state.collectAsState()
    val aiStatus by viewModel.aiStatus.collectAsState()
    val language = LocalUiText.current.language
    val topics = remember(language) { container.content.topics(language) }
    val context = LocalContext.current
    val actions = remember(viewModel, language) {
        ChatActions(
            onDraftChange = viewModel::onDraftChange,
            onSend = { viewModel.send(language) },
            onExample = viewModel::useExample,
            onTopic = { navigator.push(Screen.TopicDetail(it, fromTopics = false)) },
            onLanguagePick = container.languageSettings::setLanguage,
            onOpenTopics = { navigator.push(Screen.Topics) },
            onOpenSettings = { navigator.push(Screen.Settings) },
            onCall911 = { context.dial911() },
        )
    }
    ChatScreen(state, aiStatus, topics, actions, debugItems)
}

/** "Start" and "Use basic mode" both land on a fresh chat, whether setup was the first run or opened later. */
@Composable
private fun SetupDestination(container: AppContainer, navigator: Navigator) {
    val setup = container.modelSetup
    val state by setup.state.collectAsState()
    val actions = remember(setup, navigator) {
        SetupActions(
            onDownload = setup::startDownload,
            onCancel = setup::cancel,
            onRetry = setup::retry,
            onBasicMode = { setup.useBasicMode(); navigator.resetTo(Screen.Chat) },
            onStart = { navigator.resetTo(Screen.Chat) },
        )
    }
    SetupScreen(state, actions)
}

@Composable
private fun SettingsDestination(container: AppContainer, navigator: Navigator) {
    val aiStatus by container.chatService.aiStatus.collectAsState()
    val actions = remember(container, navigator) {
        SettingsActions(
            onBack = navigator::pop,
            onLanguagePick = container.languageSettings::setLanguage,
            onDownloadAgain = { container.modelSetup.startDownload(); navigator.push(Screen.Setup) },
            onGetAi = { navigator.push(Screen.Setup) },
            onAbout = { navigator.push(Screen.About) },
            onSources = { navigator.push(Screen.Sources) },
        )
    }
    SettingsScreen(aiStatus, BuildConfig.VERSION_NAME, actions)
}

/** Opens the dialer at 911 (no permission needed; emergency calls can work without data or load). */
private fun Context.dial911() {
    try {
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:911")))
    } catch (_: ActivityNotFoundException) {
        // No dialer on this device (some tablets). The banner text still says to call 911.
    }
}
