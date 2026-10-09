package ph.appbuilders.offlinehealth.features.chat.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ph.appbuilders.offlinehealth.R
import ph.appbuilders.offlinehealth.app.LocalUiText
import ph.appbuilders.offlinehealth.app.UiKey
import ph.appbuilders.offlinehealth.app.theme.Motion
import ph.appbuilders.offlinehealth.app.theme.Palette
import ph.appbuilders.offlinehealth.domain.model.AiStatus
import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.fakes.PreviewFrame

/** Specs.dc.html: at font scale ≥ 1.3 or width < 340 dp the pill drops its label (C12). */
@Composable
fun rememberCompactTopBar(): Boolean =
    LocalDensity.current.fontScale >= 1.3f || LocalConfiguration.current.screenWidthDp < 340

/** Chat top bar: app mark, AI status pill, language switch (one tap), overflow menu. No title text. */
@Composable
fun TopBar(
    status: AiStatus,
    onStatusClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onTopicsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = false,
    compact: Boolean = rememberCompactTopBar(),
) {
    Column(modifier.fillMaxWidth().background(Palette.Paper)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(painterResource(R.drawable.ic_app_mark), null, Modifier.size(32.dp), tint = Color.Unspecified)
            StatusPill(status, compact, onStatusClick)
            Spacer(Modifier.weight(1f))
            LanguageButton(onLanguageClick)
            OverflowMenu(onTopicsClick, onSettingsClick)
        }
        if (showDivider) HorizontalDivider(thickness = 1.dp, color = Palette.Divider)
    }
}

@Composable
private fun StatusPill(status: AiStatus, compact: Boolean, onClick: () -> Unit) {
    val text = LocalUiText.current
    val (labelKey, descKey) = when (status) {
        AiStatus.READY -> UiKey.TOPBAR_STATUS_READY to UiKey.TOPBAR_STATUS_READY_DESC
        AiStatus.STARTING -> UiKey.TOPBAR_STATUS_STARTING to UiKey.TOPBAR_STATUS_STARTING_DESC
        AiStatus.BASIC -> UiKey.TOPBAR_STATUS_BASIC to UiKey.TOPBAR_STATUS_BASIC_DESC
    }
    val description = text[descKey]
    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description }
            .background(Palette.Surface)
            .height(32.dp)
            .padding(if (compact) PaddingValues(horizontal = 7.dp) else PaddingValues(start = 8.dp, end = 12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Crossfade(status, animationSpec = tween(Motion.PILL_CROSSFADE_MS), label = "status pill") { shown ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusIcon(shown)
                if (!compact) {
                    Text(
                        text[labelKey],
                        modifier = Modifier.clearAndSetSemantics {},
                        style = MaterialTheme.typography.labelMedium,
                        color = Palette.InkMuted,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusIcon(status: AiStatus) {
    val (icon, tint) = when (status) {
        AiStatus.READY -> R.drawable.ic_check to Palette.Ink
        AiStatus.STARTING -> R.drawable.ic_progress_activity to Palette.Ink
        AiStatus.BASIC -> R.drawable.ic_contrast to Palette.InkMuted
    }
    Icon(painterResource(icon), null, Modifier.size(18.dp), tint = tint)
}

@Composable
private fun LanguageButton(onClick: () -> Unit) {
    val text = LocalUiText.current
    val description = text.get(UiKey.TOPBAR_LANGUAGE_DESC, "language" to text[UiKey.LANGUAGE_NAME])
    Row(
        modifier = Modifier
            .height(48.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = description }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(painterResource(R.drawable.ic_language), null, Modifier.size(22.dp), tint = Palette.Ink)
        Text(
            text[UiKey.LANGUAGE_CODE],
            modifier = Modifier.clearAndSetSemantics {},
            style = MaterialTheme.typography.labelLarge,
            color = Palette.Ink,
        )
    }
}

@Composable
private fun OverflowMenu(onTopicsClick: () -> Unit, onSettingsClick: () -> Unit) {
    val text = LocalUiText.current
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) {
            Icon(painterResource(R.drawable.ic_more_vert), text[UiKey.TOPBAR_MENU_DESC], tint = Palette.Ink)
        }
        DropdownMenu(
            expanded = open,
            onDismissRequest = { open = false },
            shape = MaterialTheme.shapes.large,
            containerColor = Palette.Paper,
            border = BorderStroke(1.dp, Palette.Divider),
            shadowElevation = 8.dp,
        ) {
            MenuItem(R.drawable.ic_grid_view, text[UiKey.MENU_TOPICS]) { open = false; onTopicsClick() }
            MenuItem(R.drawable.ic_settings, text[UiKey.MENU_SETTINGS]) { open = false; onSettingsClick() }
        }
    }
}

@Composable
private fun MenuItem(icon: Int, label: String, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(label, style = MaterialTheme.typography.labelLarge, color = Palette.Ink) },
        leadingIcon = { Icon(painterResource(icon), null, Modifier.size(24.dp), tint = Palette.Ink) },
        onClick = onClick,
    )
}

@Composable
private fun TopBarPreview(status: AiStatus, language: Language = Language.CEB, compact: Boolean = false) {
    PreviewFrame(language, padding = 0.dp) {
        TopBar(status, {}, {}, {}, {}, compact = compact)
    }
}

@Preview(widthDp = 360) @Composable private fun TopBarReady() = TopBarPreview(AiStatus.READY)
@Preview(widthDp = 360) @Composable private fun TopBarStarting() = TopBarPreview(AiStatus.STARTING)
@Preview(widthDp = 360) @Composable private fun TopBarBasicEnglish() = TopBarPreview(AiStatus.BASIC, Language.ENG)
@Preview(widthDp = 360) @Composable private fun TopBarCompactWaray() = TopBarPreview(AiStatus.READY, Language.WAR, true)
