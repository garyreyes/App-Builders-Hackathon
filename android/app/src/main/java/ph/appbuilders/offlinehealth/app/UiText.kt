package ph.appbuilders.offlinehealth.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import ph.appbuilders.offlinehealth.domain.model.Language

/**
 * All copy the user reads, looked up by [UiKey] in the current app language. It's provided once at the root
 * from `ContentSource.uiString`, so composables never hard-code text and a language switch re-renders everything.
 */
@Immutable
class UiText(
    val language: Language,
    private val lookup: (key: String, language: Language) -> String,
) {
    operator fun get(key: String): String = lookup(key, language)

    /** Fills `{name}` placeholders, e.g. `get(UiKey.TOPBAR_LANGUAGE_DESC, "language" to name)`. */
    fun get(key: String, vararg args: Pair<String, String>): String =
        args.fold(get(key)) { text, (name, value) -> text.replace("{$name}", value) }

    /** The same key in another language, e.g. each language's own name on the language picker. */
    fun inLanguage(key: String, language: Language): String = lookup(key, language)
}

val LocalUiText = staticCompositionLocalOf<UiText> { error("UiText not provided. Wrap the UI in ProvideUiText.") }

@Composable
fun ProvideUiText(uiText: UiText, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalUiText provides uiText, content = content)
}
