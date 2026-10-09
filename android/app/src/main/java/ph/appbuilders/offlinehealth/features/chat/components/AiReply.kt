package ph.appbuilders.offlinehealth.features.chat.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.appbuilders.offlinehealth.R
import ph.appbuilders.offlinehealth.app.LocalUiText
import ph.appbuilders.offlinehealth.app.UiKey
import ph.appbuilders.offlinehealth.app.theme.Motion
import ph.appbuilders.offlinehealth.app.theme.Palette
import ph.appbuilders.offlinehealth.app.theme.rememberReducedMotion
import ph.appbuilders.offlinehealth.domain.model.AiReplyState

/**
 * The on-device AI's reply under the card: plain text, no bubble, so it reads as secondary to the card.
 * Withheld is a quiet pointer back to the card, never an error. Previews live in AiReplyPreviews.kt.
 */
@Composable
fun AiReply(state: AiReplyState, onDownloadAi: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (state !is AiReplyState.BasicMode) AiLabel()
        when (state) {
            AiReplyState.Thinking -> ThinkingLine()
            AiReplyState.WarmingUp -> WarmingLine()
            is AiReplyState.Streaming -> ReplyText(state.text, streaming = true)
            is AiReplyState.Done -> ReplyText(state.text, streaming = false)
            AiReplyState.Withheld -> QuietLine(R.drawable.ic_arrow_upward, LocalUiText.current[UiKey.AI_WITHHELD])
            AiReplyState.BasicMode -> BasicModeBlock(onDownloadAi)
        }
    }
}

@Composable
private fun AiLabel() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(painterResource(R.drawable.ic_chat_bubble), null, Modifier.size(18.dp), tint = Palette.InkSubtle)
        Text(LocalUiText.current[UiKey.AI_LABEL], style = MaterialTheme.typography.labelMedium, color = Palette.InkSubtle)
    }
}

@Composable
private fun ThinkingLine() {
    Row(
        modifier = Modifier.heightIn(min = 26.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ThinkingDots()
        Text(LocalUiText.current[UiKey.AI_THINKING], style = MaterialTheme.typography.bodyMedium, color = Palette.InkMuted)
    }
}

/** Dots pulse in opacity 0.3 → 1, staggered 150 ms, 1.2 s loop. Static (fading steps) with reduced motion. */
@Composable
private fun ThinkingDots() {
    val reduced = rememberReducedMotion()
    val transition = rememberInfiniteTransition(label = "thinking")
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        repeat(3) { i ->
            val alpha = if (reduced) 1f - i * 0.35f else transition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    tween(Motion.DOTS_HALF_CYCLE_MS),
                    RepeatMode.Reverse,
                    StartOffset(i * Motion.DOTS_STAGGER_MS),
                ),
                label = "dot $i",
            ).value
            Box(Modifier.size(7.dp).alpha(alpha).background(Palette.Ink, CircleShape))
        }
    }
}

@Composable
private fun WarmingLine() {
    val reduced = rememberReducedMotion()
    val turn = if (reduced) 0f else rememberInfiniteTransition(label = "warming").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(Motion.SPINNER_TURN_MS, easing = LinearEasing)),
        label = "spinner",
    ).value
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(
            painterResource(R.drawable.ic_progress_activity),
            null,
            Modifier.padding(top = 2.dp).size(20.dp).rotate(turn),
            tint = Palette.Ink,
        )
        Text(LocalUiText.current[UiKey.AI_WARMING], style = MaterialTheme.typography.bodyMedium, color = Palette.InkMuted)
    }
}

private const val CARET_ID = "caret"

/** Streaming appends text as it arrives; a 3 × 20 dp caret blinks at 1 s, then disappears when done. */
@Composable
private fun ReplyText(text: String, streaming: Boolean) {
    val style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 27.sp)
    if (!streaming) {
        Text(text, Modifier.semantics { liveRegion = LiveRegionMode.Polite }, style = style, color = Palette.Ink)
        return
    }
    val caret = mapOf(
        CARET_ID to InlineTextContent(Placeholder(7.sp, 20.sp, PlaceholderVerticalAlign.TextCenter)) { Caret() },
    )
    Text(
        buildAnnotatedString { append(text); appendInlineContent(CARET_ID) },
        style = style,
        color = Palette.Ink,
        inlineContent = caret,
    )
}

@Composable
private fun Caret() {
    val reduced = rememberReducedMotion()
    val visible = if (reduced) 1f else rememberInfiniteTransition(label = "caret").animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(Motion.CARET_BLINK_MS, easing = { if (it < 1f) 0f else 1f }), RepeatMode.Reverse),
        label = "blink",
    ).value
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
        Box(Modifier.size(width = 3.dp, height = 20.dp).alpha(visible).background(Palette.Ink, RoundedCornerShape(2.dp)))
    }
}

@Composable
private fun QuietLine(icon: Int, line: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(painterResource(icon), null, Modifier.padding(top = 3.dp).size(18.dp), tint = Palette.InkMuted)
        Text(line, style = MaterialTheme.typography.bodyMedium, color = Palette.InkMuted)
    }
}

@Composable
private fun BasicModeBlock(onDownloadAi: () -> Unit) {
    val text = LocalUiText.current
    Column(Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        HorizontalDivider(thickness = 1.dp, color = Palette.Divider)
        Text(text[UiKey.AI_BASIC], Modifier.padding(top = 12.dp), style = MaterialTheme.typography.bodyMedium, color = Palette.InkMuted)
        Row(
            modifier = Modifier
                .heightIn(min = 48.dp)
                .clip(MaterialTheme.shapes.small)
                .clickable(role = Role.Button, onClick = onDownloadAi)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(painterResource(R.drawable.ic_download), null, Modifier.size(20.dp), tint = Palette.Ink)
            Text(
                text[UiKey.AI_BASIC_CTA],
                style = MaterialTheme.typography.labelLarge.copy(textDecoration = TextDecoration.Underline),
                color = Palette.Ink,
            )
        }
    }
}
