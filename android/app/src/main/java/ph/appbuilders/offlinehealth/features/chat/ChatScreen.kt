package ph.appbuilders.offlinehealth.features.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ph.appbuilders.offlinehealth.app.theme.Palette
import ph.appbuilders.offlinehealth.app.theme.Space
import ph.appbuilders.offlinehealth.app.theme.rememberReducedMotion
import ph.appbuilders.offlinehealth.domain.model.AiStatus
import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.domain.model.TopicId
import ph.appbuilders.offlinehealth.domain.model.TopicSummary
import ph.appbuilders.offlinehealth.features.chat.components.Composer
import ph.appbuilders.offlinehealth.features.chat.components.LanguageSheet
import ph.appbuilders.offlinehealth.features.chat.components.MenuAction
import ph.appbuilders.offlinehealth.features.chat.components.ReplyBelowChip
import ph.appbuilders.offlinehealth.features.chat.components.TopBar
import ph.appbuilders.offlinehealth.features.chat.components.rememberCompactTopBar

/** Everything the chat screen can ask for. AppNavigation fills these in. */
class ChatActions(
    val onDraftChange: (String) -> Unit,
    val onSend: () -> Unit,
    val onExample: (String) -> Unit,
    val onTopic: (TopicId) -> Unit,
    val onLanguagePick: (Language) -> Unit,
    val onOpenTopics: () -> Unit,
    val onOpenSettings: () -> Unit,
    val onCall911: () -> Unit,
)

/** The heart of the app (C1–C13): top bar, the conversation, and the composer. Renders state only. */
@Composable
fun ChatScreen(
    state: ChatUiState,
    aiStatus: AiStatus,
    topics: List<TopicSummary>,
    actions: ChatActions,
    debugItems: List<MenuAction> = emptyList(),
) {
    val items = remember(state.turns) { chatItems(state.turns) }
    val listState = rememberLazyListState()
    val compact = rememberCompactTopBar()
    var sheetOpen by rememberSaveable { mutableStateOf(false) }
    ScrollToNewTurn(state.turns, items, listState, bannerWins = compact)
    Column(Modifier.fillMaxSize().background(Palette.Paper).safeDrawingPadding()) {
        TopBar(
            status = aiStatus,
            onStatusClick = actions.onOpenSettings,
            onLanguageClick = { sheetOpen = true },
            onTopicsClick = actions.onOpenTopics,
            onSettingsClick = actions.onOpenSettings,
            showDivider = listState.canScrollBackward,
            compact = compact,
            debugItems = debugItems,
        )
        Box(Modifier.weight(1f)) {
            ChatList(items, topics, actions, listState, compact)
            ReplyBelow(items, listState, Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp))
        }
        Composer(state.draft, actions.onDraftChange, actions.onSend)
    }
    if (sheetOpen) {
        LanguageSheet(onPick = { actions.onLanguagePick(it); sheetOpen = false }, onDismiss = { sheetOpen = false })
    }
}

@Composable
private fun ChatList(
    items: List<ChatItem>,
    topics: List<TopicSummary>,
    actions: ChatActions,
    listState: LazyListState,
    compact: Boolean,
) {
    val gutter = if (compact) Space.md else Space.gutter
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = gutter, end = gutter, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        itemsIndexed(items, key = { _, item -> item.key }) { index, item ->
            ChatRow(item, topics, actions, compact, Modifier.padding(top = extraTopSpace(item, index)))
        }
    }
}

/** The empty state breathes more (28 dp between sections), and each new turn gets a little extra room. */
private fun extraTopSpace(item: ChatItem, index: Int) = when {
    item is ChatItem.Intro -> 12.dp
    item is ChatItem.Examples || item is ChatItem.Shortcuts -> 8.dp
    item is ChatItem.User && index > 0 -> 12.dp
    else -> 0.dp
}

/**
 * After sending, scroll so the user's bubble sits just under the top bar. At large font sizes the banner wins.
 * One short vibration when a danger banner appears. Runs once per turn, so rotation doesn't repeat it.
 */
@Composable
private fun ScrollToNewTurn(turns: List<ChatTurn>, items: List<ChatItem>, listState: LazyListState, bannerWins: Boolean) {
    val last = turns.lastOrNull() ?: return
    var handled by rememberSaveable { mutableLongStateOf(-1L) }
    val haptic = LocalHapticFeedback.current
    val reduced = rememberReducedMotion()
    LaunchedEffect(last.id) {
        if (last.id == handled) return@LaunchedEffect
        handled = last.id
        val hasBanner = last.result?.dangers?.isNotEmpty() == true
        if (hasBanner) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val key = if (bannerWins && hasBanner) "banner-${last.id}" else "user-${last.id}"
        val index = items.indexOfFirst { it.key == key }.coerceAtLeast(0)
        if (reduced) listState.scrollToItem(index) else listState.animateScrollToItem(index)
    }
}

/** Shows "AI reply below" while the newest reply is still coming and sits below the visible area. */
@Composable
private fun ReplyBelow(items: List<ChatItem>, listState: LazyListState, modifier: Modifier) {
    val scope = rememberCoroutineScope()
    val target by remember(items) {
        derivedStateOf {
            val ai = items.indexOfLast { it is ChatItem.Ai && it.state.isPending() }
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: Int.MAX_VALUE
            if (ai >= 0 && ai > lastVisible) ai else -1
        }
    }
    if (target >= 0) {
        ReplyBelowChip(onClick = { scope.launch { listState.animateScrollToItem(target) } }, modifier = modifier)
    }
}
