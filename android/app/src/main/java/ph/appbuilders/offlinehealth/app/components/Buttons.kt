package ph.appbuilders.offlinehealth.app.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ph.appbuilders.offlinehealth.app.theme.Palette
import ph.appbuilders.offlinehealth.fakes.PreviewFrame

// The three button styles from Specs.dc.html → setup. Labels are titleMedium (18/24 Bold) or labelLarge (16/20 Bold).

private val ButtonPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)

/** Min 56 dp, full width, Ink fill, radius 12, Paper label. One per screen: the obvious next step. */
@Composable
fun PrimaryButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = 56.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(containerColor = Palette.Ink, contentColor = Palette.Paper),
        contentPadding = ButtonPadding,
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
    }
}

/** Min 56 dp, Paper, 2 dp Outline border, Ink label. */
@Composable
fun SecondaryButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = 56.dp),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(2.dp, Palette.Outline),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Palette.Paper, contentColor = Palette.Ink),
        contentPadding = ButtonPadding,
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
    }
}

/** Min 48 dp, underlined labelLarge Ink. The escape hatch ("Use basic mode for now", "Download again"). */
@Composable
fun TextLinkButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(MaterialTheme.shapes.small)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge.copy(textDecoration = TextDecoration.Underline),
            color = Palette.Ink,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(widthDp = 360) @Composable
private fun ButtonsPreview() = PreviewFrame {
    Column {
        PrimaryButton("Download AI · 740 MB", {})
        SecondaryButton("Cancel download", {}, Modifier.padding(top = 8.dp))
        TextLinkButton("Use basic mode for now", {}, Modifier.fillMaxWidth())
    }
}
