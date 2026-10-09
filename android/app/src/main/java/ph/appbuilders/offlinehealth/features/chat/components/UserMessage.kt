package ph.appbuilders.offlinehealth.features.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ph.appbuilders.offlinehealth.app.theme.Palette
import ph.appbuilders.offlinehealth.fakes.FakeSamples
import ph.appbuilders.offlinehealth.fakes.PreviewFrame

private val BubbleShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 6.dp, bottomStart = 20.dp)

/** The user's sent message: right-aligned, max 86% wide, SurfaceStrong bubble. */
@Composable
fun UserMessage(text: String, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        Text(
            text = text,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .widthIn(max = maxWidth * 0.86f)
                .background(Palette.SurfaceStrong, BubbleShape)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = Palette.Ink,
        )
    }
}

@Preview(widthDp = 360) @Composable
private fun UserMessageLong() = PreviewFrame {
    UserMessage(FakeSamples.USER_CEB)
}

@Preview(widthDp = 360) @Composable
private fun UserMessageShort() = PreviewFrame { UserMessage(FakeSamples.USER_SHORT) }
