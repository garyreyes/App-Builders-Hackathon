package ph.appbuilders.offlinehealth.features.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ph.appbuilders.offlinehealth.R
import ph.appbuilders.offlinehealth.app.LocalUiText
import ph.appbuilders.offlinehealth.app.UiKey
import ph.appbuilders.offlinehealth.app.components.RowTitle
import ph.appbuilders.offlinehealth.app.theme.Palette
import ph.appbuilders.offlinehealth.domain.model.TopicCard
import ph.appbuilders.offlinehealth.fakes.FakeContentSource
import ph.appbuilders.offlinehealth.fakes.PreviewFrame
import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.domain.model.TopicId

/**
 * The topic card, folded into one row under an AI answer: the checked steps are one tap away (the topic page)
 * while the AI reply leads. Danger signs never hide here; they are in the red banner above.
 */
@Composable
fun CheckedSteps(card: TopicCard, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val text = LocalUiText.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clip(MaterialTheme.shapes.large)
            .background(Palette.Surface)
            .clickable(role = Role.Button, onClick = onOpen)
            .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.size(44.dp).background(Palette.Paper, CircleShape), contentAlignment = Alignment.Center) {
            Icon(painterResource(card.topicId.iconRes()), null, Modifier.size(22.dp), tint = Palette.Ink)
        }
        Column(Modifier.weight(1f)) {
            Text(text[UiKey.CHAT_CHECKED_STEPS], style = MaterialTheme.typography.labelMedium, color = Palette.InkSubtle)
            Text(card.title, style = RowTitle, color = Palette.Ink)
            Text(
                "${text[UiKey.CARD_HOME]} · ${text[UiKey.CARD_GO_NOW]}",
                style = MaterialTheme.typography.bodyMedium,
                color = Palette.InkMuted,
            )
        }
        Icon(painterResource(R.drawable.ic_chevron_right), null, Modifier.size(22.dp), tint = Palette.InkSubtle)
    }
}

@Preview(widthDp = 360) @Composable
private fun CheckedStepsPreview() = PreviewFrame {
    CheckedSteps(FakeContentSource().card(TopicId.CHILD_DIARRHEA, Language.ENG), onOpen = {})
}
