package ph.appbuilders.offlinehealth.fakes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import ph.appbuilders.offlinehealth.app.ProvideUiText
import ph.appbuilders.offlinehealth.app.UiText
import ph.appbuilders.offlinehealth.app.theme.AppTheme
import ph.appbuilders.offlinehealth.app.theme.Palette
import ph.appbuilders.offlinehealth.app.theme.Space
import ph.appbuilders.offlinehealth.domain.model.Language

/** Theme + placeholder copy for `@Preview`s and the dev gallery. Bisaya by default, like the canvas. */
@Composable
fun PreviewFrame(
    language: Language = Language.CEB,
    padding: Dp = Space.gutter,
    content: @Composable () -> Unit,
) {
    val source = remember { FakeContentSource() }
    AppTheme {
        ProvideUiText(UiText(language, source::uiString)) {
            Box(Modifier.background(Palette.Paper).padding(padding)) { content() }
        }
    }
}
