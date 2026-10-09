package ph.appbuilders.offlinehealth.features.chat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ph.appbuilders.offlinehealth.app.components.SurfaceButton
import ph.appbuilders.offlinehealth.app.theme.Palette
import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.domain.model.TopicId
import ph.appbuilders.offlinehealth.domain.model.TopicSummary
import ph.appbuilders.offlinehealth.fakes.FakeContentSource
import ph.appbuilders.offlinehealth.fakes.PreviewFrame

private val TileShape = RoundedCornerShape(14.dp)

/** The 7 topic shortcuts in a 2-column grid. Long labels wrap and both tiles in a row grow together. */
@Composable
fun TopicGrid(topics: List<TopicSummary>, onTopic: (TopicId) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        topics.chunked(2).forEach { pair ->
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { TopicTile(it, onTopic, Modifier.weight(1f).fillMaxHeight()) }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TopicTile(topic: TopicSummary, onTopic: (TopicId) -> Unit, modifier: Modifier) {
    SurfaceButton(onClick = { onTopic(topic.topicId) }, modifier = modifier.heightIn(min = 64.dp), shape = TileShape) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(painterResource(topic.topicId.iconRes()), null, Modifier.size(24.dp), tint = Palette.Ink)
            Text(
                topic.title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = Palette.Ink,
            )
        }
    }
}

@Preview(widthDp = 360) @Composable
private fun TopicGridBisaya() = PreviewFrame {
    TopicGrid(remember { FakeContentSource().topics(Language.CEB) }, onTopic = {})
}

@Preview(widthDp = 320, fontScale = 1.5f) @Composable
private fun TopicGridNarrowEnglish() = PreviewFrame(Language.ENG) {
    TopicGrid(remember { FakeContentSource().topics(Language.ENG) }, onTopic = {})
}
