package ph.appbuilders.offlinehealth.app.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.appbuilders.offlinehealth.R
import ph.appbuilders.offlinehealth.app.theme.Palette

/**
 * The 64 dp bar on Topics, TopicDetail, and Settings: a 48 dp back button, then either a screen [title]
 * (titleLarge heading) or a quiet [label] that names where back goes (TopicDetail's "Topics").
 */
@Composable
fun BackBar(
    onBack: () -> Unit,
    backDescription: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    label: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(64.dp).padding(start = 4.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(painterResource(R.drawable.ic_arrow_back), backDescription, tint = Palette.Ink)
        }
        if (title != null) {
            Text(title, Modifier.semantics { heading() }, style = MaterialTheme.typography.titleLarge, maxLines = 2)
        } else if (label != null) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = Palette.InkMuted,
                maxLines = 1,
            )
        }
    }
}

/**
 * A full-width list row (Topics list, Settings): min 72 dp, padding 12/16/12/20, [leading] slot, title 18/24
 * SemiBold, optional [subtitle] 16/22 InkMuted, a chevron, and a hairline under it unless it's the last row.
 */
@Composable
fun ListRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    divider: Boolean = true,
    leading: @Composable RowScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(start = 20.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            leading()
            Column(Modifier.weight(1f)) {
                Text(title, style = RowTitle, color = Palette.Ink)
                if (subtitle != null) Text(subtitle, style = RowSubtitle, color = Palette.InkMuted)
            }
            Icon(painterResource(R.drawable.ic_chevron_right), null, Modifier.size(22.dp), tint = Palette.InkSubtle)
        }
        if (divider) HorizontalDivider(thickness = 1.dp, color = Palette.Divider)
    }
}

val RowTitle @Composable get() = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
val RowSubtitle @Composable get() = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp)

/**
 * A screen that scrolls as one column, with [footer] pinned to the bottom when everything fits and simply
 * following the body when it doesn't (font scale 1.5×, 320 dp). Used by the language picker and setup.
 */
@Composable
fun PinnedFooterColumn(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    footer: @Composable ColumnScope.() -> Unit,
    body: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .fillMaxWidth()
                .padding(contentPadding),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(content = body)
            Column(Modifier.padding(top = 24.dp), content = footer)
        }
    }
}
