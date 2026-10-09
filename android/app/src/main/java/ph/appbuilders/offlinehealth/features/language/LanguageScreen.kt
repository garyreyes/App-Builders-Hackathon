package ph.appbuilders.offlinehealth.features.language

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ph.appbuilders.offlinehealth.R
import ph.appbuilders.offlinehealth.app.LocalUiText
import ph.appbuilders.offlinehealth.app.UiKey
import ph.appbuilders.offlinehealth.app.components.PinnedFooterColumn
import ph.appbuilders.offlinehealth.app.components.SurfaceButton
import ph.appbuilders.offlinehealth.app.theme.Palette
import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.fakes.PreviewFrame

/**
 * First launch (A): "choose your language" written in all four languages, then one tile per language in its
 * own name. One tap chooses and continues. Shown until a language is saved.
 */
@Composable
fun LanguageScreen(onPick: (Language) -> Unit) {
    val text = LocalUiText.current
    PinnedFooterColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 40.dp, bottom = 28.dp),
        modifier = Modifier.background(Palette.Paper).safeDrawingPadding(),
        footer = { ChangeLaterHint() },
    ) {
        Icon(painterResource(R.drawable.ic_app_mark), null, Modifier.size(48.dp), tint = Color.Unspecified)
        Spacer(Modifier.height(32.dp))
        Column(Modifier.semantics(mergeDescendants = true) { heading() }, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Language.entries.forEach { language ->
                Text(text.inLanguage(UiKey.LANGUAGE_CHOOSE, language), style = MaterialTheme.typography.titleLarge)
            }
        }
        Spacer(Modifier.height(32.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Language.entries.forEach { language -> LanguageTile(language, onClick = { onPick(language) }) }
        }
    }
}

/** Min 72 dp, Surface, radius 16, padding 12/16/12/20. Native name titleLarge + other name labelMedium. */
@Composable
private fun LanguageTile(language: Language, onClick: () -> Unit) {
    val text = LocalUiText.current
    val alias = text.inLanguage(UiKey.LANGUAGE_ALIAS, language)
    SurfaceButton(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp)) {
        Row(
            modifier = Modifier.padding(start = 20.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(text.inLanguage(UiKey.LANGUAGE_NAME, language), style = MaterialTheme.typography.titleLarge, color = Palette.Ink)
                if (alias.isNotBlank()) Text(alias, style = MaterialTheme.typography.labelMedium, color = Palette.InkMuted)
            }
            Icon(painterResource(R.drawable.ic_chevron_right), null, Modifier.size(24.dp), tint = Palette.Ink)
        }
    }
}

@Composable
private fun ChangeLaterHint() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        Icon(painterResource(R.drawable.ic_language), null, Modifier.size(18.dp), tint = Palette.InkMuted)
        Text(
            LocalUiText.current[UiKey.LANGUAGE_CHANGE_LATER],
            style = MaterialTheme.typography.labelMedium,
            color = Palette.InkMuted,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(widthDp = 360, heightDp = 800) @Composable
private fun LanguagePreview() = PreviewFrame(Language.ENG, padding = 0.dp) { Column(Modifier.fillMaxSize()) { LanguageScreen {} } }

@Preview(widthDp = 320, heightDp = 640, fontScale = 1.5f) @Composable
private fun LanguageNarrowPreview() = PreviewFrame(Language.ENG, padding = 0.dp) { LanguageScreen {} }
