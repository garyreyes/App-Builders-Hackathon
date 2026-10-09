package ph.appbuilders.offlinehealth.features.modelsetup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ph.appbuilders.offlinehealth.R
import ph.appbuilders.offlinehealth.app.LocalUiText
import ph.appbuilders.offlinehealth.app.UiKey
import ph.appbuilders.offlinehealth.app.UiText
import ph.appbuilders.offlinehealth.app.components.PinnedFooterColumn
import ph.appbuilders.offlinehealth.app.components.PrimaryButton
import ph.appbuilders.offlinehealth.app.components.SecondaryButton
import ph.appbuilders.offlinehealth.app.components.TextLinkButton
import ph.appbuilders.offlinehealth.app.theme.Palette
import ph.appbuilders.offlinehealth.domain.model.SetupState
import ph.appbuilders.offlinehealth.fakes.PreviewFrame

/** Everything the setup screen can ask for. AppNavigation fills these in. */
class SetupActions(
    val onDownload: () -> Unit,
    val onCancel: () -> Unit,
    val onRetry: () -> Unit,
    val onBasicMode: () -> Unit,
    val onStart: () -> Unit,
)

/**
 * One-time AI setup (B1–B8). Header + state detail at the top, actions pinned to the bottom.
 * Errors stay neutral (no red): only danger is red. Every error offers basic mode as the way out.
 */
@Composable
fun SetupScreen(state: SetupState, actions: SetupActions) {
    PinnedFooterColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 24.dp),
        modifier = Modifier.background(Palette.Paper).safeDrawingPadding(),
        footer = { SetupFooter(state, actions) },
    ) {
        SetupBody(state)
    }
}

@Composable
private fun SetupBody(state: SetupState) {
    val text = LocalUiText.current
    when (state) {
        is SetupState.Intro -> {
            SetupHeader(R.drawable.ic_download, text[UiKey.SETUP_INTRO_TITLE], text[UiKey.SETUP_INTRO_BODY])
            IntroFacts(mb(text, state.sizeMb))
        }
        is SetupState.Downloading -> {
            SetupHeader(R.drawable.ic_download, text[UiKey.SETUP_DOWNLOADING_TITLE], text[UiKey.SETUP_DOWNLOADING_BODY])
            DownloadProgress(state.doneMb, state.totalMb)
        }
        SetupState.Verifying -> {
            SetupHeader(R.drawable.ic_verified_user, text[UiKey.SETUP_VERIFYING_TITLE], text[UiKey.SETUP_VERIFYING_BODY])
            CheckingProgress()
        }
        SetupState.Done ->
            SetupHeader(R.drawable.ic_check, text[UiKey.SETUP_DONE_TITLE], text[UiKey.SETUP_DONE_BODY], done = true)
        SetupState.NoInternet ->
            SetupHeader(R.drawable.ic_wifi_off, text[UiKey.SETUP_NO_INTERNET_TITLE], text[UiKey.SETUP_NO_INTERNET_BODY])
        is SetupState.NotEnoughStorage -> {
            SetupHeader(R.drawable.ic_smartphone, text[UiKey.SETUP_STORAGE_TITLE], text[UiKey.SETUP_STORAGE_BODY])
            StorageNeeded(state.neededMb, state.freeMb)
        }
        is SetupState.DownloadFailed -> SetupHeader(
            R.drawable.ic_refresh,
            text[UiKey.SETUP_FAILED_TITLE],
            text.get(UiKey.SETUP_FAILED_BODY, "done" to "${state.doneMb}", "total" to "${state.totalMb}"),
        )
        SetupState.CheckFailed ->
            SetupHeader(R.drawable.ic_gpp_maybe, text[UiKey.SETUP_CHECK_FAILED_TITLE], text[UiKey.SETUP_CHECK_FAILED_BODY])
    }
}

/** One primary action per state, plus "Use basic mode for now" wherever setup can't finish right now. */
@Composable
private fun SetupFooter(state: SetupState, actions: SetupActions) {
    val text = LocalUiText.current
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        when (state) {
            is SetupState.Intro -> {
                PrimaryButton(text.get(UiKey.SETUP_DOWNLOAD, "size" to mb(text, state.sizeMb)), actions.onDownload)
                BasicModeLink(actions.onBasicMode)
                Text(
                    text[UiKey.SETUP_BASIC_NOTE],
                    Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelMedium,
                    color = Palette.InkMuted,
                    textAlign = TextAlign.Center,
                )
            }
            is SetupState.Downloading -> SecondaryButton(text[UiKey.SETUP_CANCEL], actions.onCancel)
            SetupState.Verifying -> Unit
            SetupState.Done -> PrimaryButton(text[UiKey.SETUP_START], actions.onStart)
            SetupState.NoInternet, is SetupState.NotEnoughStorage -> RetryActions(text[UiKey.SETUP_TRY_AGAIN], actions)
            is SetupState.DownloadFailed -> RetryActions(text[UiKey.SETUP_RETRY], actions)
            SetupState.CheckFailed -> RetryActions(text[UiKey.SETUP_DOWNLOAD_AGAIN], actions)
        }
    }
}

@Composable
private fun RetryActions(label: String, actions: SetupActions) {
    PrimaryButton(label, actions.onRetry)
    BasicModeLink(actions.onBasicMode)
}

@Composable
private fun BasicModeLink(onClick: () -> Unit) {
    TextLinkButton(LocalUiText.current[UiKey.SETUP_BASIC], onClick, Modifier.fillMaxWidth())
}

/** "740 MB": every size on this screen goes through one key, so translators control the unit. */
internal fun mb(text: UiText, value: Int): String = text.get(UiKey.SETUP_MB, "mb" to "$value")

private val noActions = SetupActions({}, {}, {}, {}, {})

@Composable
private fun SetupPreview(state: SetupState) = PreviewFrame(padding = 0.dp) {
    Column(Modifier.fillMaxSize()) { SetupScreen(state, noActions) }
}

@Preview(widthDp = 360, heightDp = 800) @Composable private fun B1Intro() = SetupPreview(SetupState.Intro(740))
@Preview(widthDp = 360, heightDp = 800) @Composable private fun B2Downloading() = SetupPreview(SetupState.Downloading(312, 740))
@Preview(widthDp = 360, heightDp = 800) @Composable private fun B3Verifying() = SetupPreview(SetupState.Verifying)
@Preview(widthDp = 360, heightDp = 800) @Composable private fun B4Done() = SetupPreview(SetupState.Done)
@Preview(widthDp = 360, heightDp = 800) @Composable private fun B5NoInternet() = SetupPreview(SetupState.NoInternet)
@Preview(widthDp = 360, heightDp = 800) @Composable private fun B6Storage() = SetupPreview(SetupState.NotEnoughStorage(740, 410))
@Preview(widthDp = 360, heightDp = 800) @Composable private fun B7Failed() = SetupPreview(SetupState.DownloadFailed(312, 740))
@Preview(widthDp = 360, heightDp = 800) @Composable private fun B8CheckFailed() = SetupPreview(SetupState.CheckFailed)
@Preview(widthDp = 320, heightDp = 640, fontScale = 1.5f) @Composable private fun B1Narrow() = SetupPreview(SetupState.Intro(740))
