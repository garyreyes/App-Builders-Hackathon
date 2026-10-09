package ph.appbuilders.offlinehealth.app

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.domain.model.SetupState
import ph.appbuilders.offlinehealth.features.chat.components.MenuAction
import ph.appbuilders.offlinehealth.features.language.LanguageScreen

/**
 * Root of the UI: provides the copy for the current language and shows the top of the back stack.
 * With no language saved yet, the language picker shows first (A), then setup (B1) unless the AI is ready.
 * Thin layer: it reads state and hands each screen its callbacks (see Destinations.kt).
 */
@Composable
fun AppNavigation(container: AppContainer) {
    val navigator = viewModel { Navigator() }
    val saved by container.languageSettings.language.collectAsState()
    val stack by navigator.stack.collectAsState()
    // Before a language is picked, the picker's one line of help is in English, like the canvas.
    val language = saved ?: Language.ENG
    val uiText = remember(language) { UiText(language, container.content::uiString) }
    BackHandler(enabled = saved != null && stack.size > 1) { navigator.pop() }

    ProvideUiText(uiText) {
        if (saved == null) {
            LanguageScreen(onPick = { picked ->
                container.languageSettings.setLanguage(picked)
                if (container.modelSetup.state.value !is SetupState.Done) navigator.resetTo(Screen.Setup)
            })
        } else {
            Destination(stack.last(), container, navigator, debugItems(container, navigator))
        }
    }
}

/** Debug builds only: the fakes' controls, setup error states (which also open setup), and the gallery. */
private fun debugItems(container: AppContainer, navigator: Navigator): List<MenuAction> {
    if (container.debugActions.isEmpty()) return emptyList()
    val setupItems = container.debugSetupActions.map { item ->
        MenuAction(item.label) { item.onClick(); navigator.push(Screen.Setup) }
    }
    return container.debugActions + setupItems + MenuAction("Debug: component gallery") { navigator.push(Screen.Gallery) }
}
