package ph.appbuilders.offlinehealth.features.chat.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ph.appbuilders.offlinehealth.R
import ph.appbuilders.offlinehealth.app.LocalUiText
import ph.appbuilders.offlinehealth.app.UiKey
import ph.appbuilders.offlinehealth.app.UiText
import ph.appbuilders.offlinehealth.app.components.SurfaceButton
import ph.appbuilders.offlinehealth.app.theme.Palette
import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.fakes.PreviewFrame

private val OptionShape = RoundedCornerShape(14.dp)

/** Order of the other languages' word for "language" in the sheet's subtitle (LangSheet.dc.html). */
private val SubtitleOrder = listOf(Language.ENG, Language.CEB, Language.WAR, Language.TGL)

/** Language switch over the chat (C13). Each option shows the language's own name. Tap = switch and close. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSheet(onPick: (Language) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Palette.Paper,
        tonalElevation = 0.dp,
        scrimColor = Palette.Scrim,
        dragHandle = { DragHandle() },
    ) {
        LanguageSheetContent(onPick, Modifier.navigationBarsPadding())
    }
}

@Composable
private fun DragHandle() {
    Box(Modifier.padding(top = 10.dp).size(width = 36.dp, height = 4.dp).background(Palette.Outline, CircleShape))
}

/** The sheet's body, split out so it can be previewed (a modal sheet can't render in @Preview). */
@Composable
fun LanguageSheetContent(onPick: (Language) -> Unit, modifier: Modifier = Modifier) {
    val text = LocalUiText.current
    Column(
        modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(Modifier.padding(start = 4.dp, end = 4.dp, top = 12.dp, bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text[UiKey.LANGUAGE_WORD], Modifier.semantics { heading() }, style = MaterialTheme.typography.titleLarge)
            Text(subtitle(text), style = MaterialTheme.typography.labelMedium, color = Palette.InkMuted)
        }
        Language.entries.forEach { language ->
            LanguageOption(language, selected = language == text.language, onClick = { onPick(language) })
        }
    }
}

private fun subtitle(text: UiText): String =
    SubtitleOrder.filter { it != text.language }.joinToString(" · ") { text.inLanguage(UiKey.LANGUAGE_WORD, it) }

@Composable
private fun LanguageOption(language: Language, selected: Boolean, onClick: () -> Unit) {
    val text = LocalUiText.current
    val alias = text.inLanguage(UiKey.LANGUAGE_ALIAS, language)
    SurfaceButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
        shape = OptionShape,
        color = if (selected) Palette.Paper else Palette.Surface,
        border = if (selected) BorderStroke(2.dp, Palette.Ink) else null,
        selected = selected,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(text.inLanguage(UiKey.LANGUAGE_NAME, language), style = MaterialTheme.typography.titleMedium, color = Palette.Ink)
                if (alias.isNotBlank()) Text(alias, style = MaterialTheme.typography.bodySmall, color = Palette.InkMuted)
            }
            if (selected) SelectedMark()
        }
    }
}

@Composable
private fun SelectedMark() {
    Box(Modifier.size(28.dp).background(Palette.Ink, CircleShape), contentAlignment = Alignment.Center) {
        Icon(painterResource(R.drawable.ic_check), null, Modifier.size(18.dp), tint = Palette.Paper)
    }
}

@Preview(widthDp = 360) @Composable
private fun LanguageSheetBisaya() = PreviewFrame(padding = 0.dp) { LanguageSheetContent(onPick = {}) }

@Preview(widthDp = 320, fontScale = 1.5f) @Composable
private fun LanguageSheetNarrowEnglish() = PreviewFrame(Language.ENG, padding = 0.dp) { LanguageSheetContent(onPick = {}) }
