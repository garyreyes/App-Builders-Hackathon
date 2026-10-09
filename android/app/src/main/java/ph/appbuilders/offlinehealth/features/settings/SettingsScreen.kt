package ph.appbuilders.offlinehealth.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ph.appbuilders.offlinehealth.R
import ph.appbuilders.offlinehealth.app.LocalUiText
import ph.appbuilders.offlinehealth.app.UiKey
import ph.appbuilders.offlinehealth.app.components.BackBar
import ph.appbuilders.offlinehealth.app.components.LanguageSheet
import ph.appbuilders.offlinehealth.app.components.ListRow
import ph.appbuilders.offlinehealth.app.components.PinnedFooterColumn
import ph.appbuilders.offlinehealth.app.components.RowSubtitle
import ph.appbuilders.offlinehealth.app.components.RowTitle
import ph.appbuilders.offlinehealth.app.components.TextLinkButton
import ph.appbuilders.offlinehealth.app.theme.Palette
import ph.appbuilders.offlinehealth.domain.model.AiStatus
import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.fakes.PreviewFrame

/** Everything the settings screen can ask for. AppNavigation fills these in. */
class SettingsActions(
    val onBack: () -> Unit,
    val onLanguagePick: (Language) -> Unit,
    val onDownloadAgain: () -> Unit,
    val onGetAi: () -> Unit,
    val onAbout: () -> Unit,
    val onSources: () -> Unit,
)

/** E: language, AI helper status, about, sources, and the version at the bottom. */
@Composable
fun SettingsScreen(aiStatus: AiStatus, version: String, actions: SettingsActions) {
    val text = LocalUiText.current
    var sheetOpen by rememberSaveable { mutableStateOf(false) }
    PinnedFooterColumn(
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.background(Palette.Paper).safeDrawingPadding(),
        footer = {
            Text(
                text.get(UiKey.SETTINGS_VERSION, "version" to version),
                Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
                style = MaterialTheme.typography.labelMedium,
                color = Palette.InkMuted,
            )
        },
    ) {
        BackBar(actions.onBack, text[UiKey.NAV_BACK], title = text[UiKey.MENU_SETTINGS])
        ListRow(text[UiKey.LANGUAGE_WORD], onClick = { sheetOpen = true }, subtitle = text[UiKey.LANGUAGE_NAME]) {
            RowIcon(R.drawable.ic_language)
        }
        AiHelperSection(aiStatus, actions)
        ListRow(text[UiKey.SETTINGS_ABOUT], actions.onAbout, subtitle = text[UiKey.SETTINGS_ABOUT_DESC]) {
            RowIcon(R.drawable.ic_info)
        }
        ListRow(text[UiKey.SETTINGS_SOURCES], actions.onSources, subtitle = text[UiKey.SETTINGS_SOURCES_DESC]) {
            RowIcon(R.drawable.ic_description)
        }
    }
    if (sheetOpen) {
        LanguageSheet(onPick = { actions.onLanguagePick(it); sheetOpen = false }, onDismiss = { sheetOpen = false })
    }
}

@Composable
private fun RowIcon(icon: Int) {
    Icon(painterResource(icon), null, Modifier.size(24.dp), tint = Palette.Ink)
}

/**
 * Not a link: a status block. Ready or starting offers "Download again"; basic mode offers "Download the AI".
 * Status lines reuse the top bar's words, so the pill and this block always agree.
 */
@Composable
private fun AiHelperSection(status: AiStatus, actions: SettingsActions) {
    val text = LocalUiText.current
    val (icon, line) = when (status) {
        AiStatus.READY -> R.drawable.ic_check to text[UiKey.SETTINGS_AI_READY]
        AiStatus.STARTING -> R.drawable.ic_progress_activity to text[UiKey.TOPBAR_STATUS_STARTING]
        AiStatus.BASIC -> R.drawable.ic_contrast to text[UiKey.TOPBAR_STATUS_BASIC]
    }
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(painterResource(R.drawable.ic_chat_bubble), null, Modifier.padding(top = 2.dp).size(24.dp), tint = Palette.Ink)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Column(Modifier.semantics(mergeDescendants = true) {}) {
                    Text(text[UiKey.SETTINGS_AI], style = RowTitle, color = Palette.Ink)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(painterResource(icon), null, Modifier.size(16.dp), tint = Palette.Ink)
                        Text(line, style = RowSubtitle, color = Palette.Ink)
                    }
                }
                // Pulled 4 dp left (as on the artboard), so the label lines up with the text above it.
                if (status == AiStatus.BASIC) {
                    TextLinkButton(text[UiKey.AI_BASIC_CTA], actions.onGetAi, Modifier.offset(x = (-4).dp))
                } else {
                    TextLinkButton(text[UiKey.SETUP_DOWNLOAD_AGAIN], actions.onDownloadAgain, Modifier.offset(x = (-4).dp))
                }
            }
        }
        HorizontalDivider(thickness = 1.dp, color = Palette.Divider)
    }
}

private val noActions = SettingsActions({}, {}, {}, {}, {}, {})

@Preview(widthDp = 360, heightDp = 800) @Composable
private fun SettingsReady() = PreviewFrame(padding = 0.dp) {
    Column(Modifier.fillMaxSize()) { SettingsScreen(AiStatus.READY, "0.1.0", noActions) }
}

@Preview(widthDp = 360, heightDp = 800) @Composable
private fun SettingsBasicEnglish() = PreviewFrame(Language.ENG, padding = 0.dp) {
    Column(Modifier.fillMaxSize()) { SettingsScreen(AiStatus.BASIC, "0.1.0", noActions) }
}
