package ph.appbuilders.offlinehealth.features.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ph.appbuilders.offlinehealth.R
import ph.appbuilders.offlinehealth.app.LocalUiText
import ph.appbuilders.offlinehealth.app.UiKey
import ph.appbuilders.offlinehealth.app.theme.Palette
import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.fakes.FakeSamples
import ph.appbuilders.offlinehealth.fakes.PreviewFrame

private val FieldShape = RoundedCornerShape(28.dp)

/**
 * Message input with the always-visible disclaimer. v1 has no attach, camera, or mic (frontend brief §1).
 * The send button appears only when there's text, so there is never a disabled send. Enter inserts a new line;
 * only the button sends, so a stressed tap can't send half a message.
 */
@Composable
fun Composer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth().background(Palette.Paper)) {
        HorizontalDivider(thickness = 1.dp, color = Palette.Divider)
        Column(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Disclaimer()
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MessageField(value, onValueChange, Modifier.weight(1f))
                if (value.isNotBlank()) SendButton(onSend)
            }
        }
    }
}

@Composable
private fun Disclaimer() {
    Row(Modifier.padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(painterResource(R.drawable.ic_info), null, Modifier.padding(top = 2.dp).size(16.dp), tint = Palette.InkMuted)
        Text(
            LocalUiText.current[UiKey.COMPOSER_DISCLAIMER],
            style = MaterialTheme.typography.labelMedium,
            color = Palette.InkMuted,
        )
    }
}

@Composable
private fun MessageField(value: String, onValueChange: (String) -> Unit, modifier: Modifier) {
    val text = LocalUiText.current
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val active = focused || value.isNotEmpty()
    val description = text[UiKey.COMPOSER_FIELD_DESC]
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.semantics { contentDescription = description },
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Palette.Ink),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        minLines = 1,
        maxLines = 5,
        interactionSource = interaction,
        cursorBrush = SolidColor(Palette.Ink),
        decorationBox = { inner ->
            Box(
                modifier = Modifier
                    .heightIn(min = 56.dp)
                    .background(if (active) Palette.Paper else Palette.Surface, FieldShape)
                    .border(2.dp, if (active) Palette.Ink else Palette.Surface, FieldShape)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isEmpty()) {
                    Text(text[UiKey.COMPOSER_PLACEHOLDER], style = MaterialTheme.typography.bodyLarge, color = Palette.InkSubtle)
                }
                inner()
            }
        },
    )
}

@Composable
private fun SendButton(onSend: () -> Unit) {
    Surface(onClick = onSend, modifier = Modifier.size(56.dp), shape = CircleShape, color = Palette.Ink) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painterResource(R.drawable.ic_arrow_upward),
                LocalUiText.current[UiKey.COMPOSER_SEND],
                Modifier.size(26.dp),
                tint = Palette.Paper,
            )
        }
    }
}

@Preview(widthDp = 360) @Composable
private fun ComposerEmpty() = PreviewFrame(padding = 0.dp) { Composer("", {}, {}) }

@Preview(widthDp = 360) @Composable
private fun ComposerTyping() = PreviewFrame(padding = 0.dp) {
    Composer(FakeSamples.USER_CEB, {}, {})
}

@Preview(widthDp = 320, fontScale = 1.5f) @Composable
private fun ComposerNarrowEnglish() = PreviewFrame(Language.ENG, padding = 0.dp) {
    Composer(FakeSamples.USER_ENG, {}, {})
}
