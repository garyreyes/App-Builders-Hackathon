package ph.appbuilders.offlinehealth.lib.settings

import kotlinx.coroutines.flow.StateFlow
import ph.appbuilders.offlinehealth.domain.model.Language

/** The app's own language. `null` until the user picks one on first launch. The core's `SettingsStore` implements this. */
interface LanguageSettings {
    val language: StateFlow<Language?>
    fun setLanguage(language: Language)
}
