package ph.appbuilders.offlinehealth.fakes

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.lib.settings.LanguageSettings

/** FAKE: in-memory only, so the choice resets when the app process ends. The core's SettingsStore persists it. */
class FakeLanguageSettings(initial: Language?) : LanguageSettings {
    private val state = MutableStateFlow(initial)
    override val language: StateFlow<Language?> = state.asStateFlow()

    override fun setLanguage(language: Language) {
        state.value = language
    }
}
