package ph.appbuilders.offlinehealth.app.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import ph.appbuilders.offlinehealth.app.theme.Palette

/**
 * A tappable block with a Surface fill: topic tiles, example chips, language options.
 * Specs.dc.html: "All Surface buttons: SurfaceStrong on press. Ripple in Ink at 8%."
 * Pass [selected] for one choice in a group, so TalkBack reads it as a radio button.
 */
@Composable
fun SurfaceButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = Palette.Surface,
    border: BorderStroke? = null,
    selected: Boolean? = null,
    contentAlignment: Alignment = Alignment.CenterStart,
    content: @Composable BoxScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val fill = if (pressed && color == Palette.Surface) Palette.SurfaceStrong else color
    val click = if (selected == null) {
        Modifier.clickable(interaction, ripple(), role = Role.Button, onClick = onClick)
    } else {
        Modifier.selectable(selected, interaction, ripple(), role = Role.RadioButton, onClick = onClick)
    }
    Box(
        modifier = modifier
            .clip(shape)
            .background(fill)
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .then(click),
        contentAlignment = contentAlignment,
        content = content,
    )
}
