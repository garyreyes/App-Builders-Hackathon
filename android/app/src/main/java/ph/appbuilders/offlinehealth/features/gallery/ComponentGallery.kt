package ph.appbuilders.offlinehealth.features.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ph.appbuilders.offlinehealth.app.ProvideUiText
import ph.appbuilders.offlinehealth.app.UiText
import ph.appbuilders.offlinehealth.app.theme.Palette
import ph.appbuilders.offlinehealth.app.theme.Space
import ph.appbuilders.offlinehealth.domain.model.AiReplyState
import ph.appbuilders.offlinehealth.domain.model.AiStatus
import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.domain.model.TopicId
import ph.appbuilders.offlinehealth.fakes.FakeContentSource
import ph.appbuilders.offlinehealth.fakes.FakeSamples
import ph.appbuilders.offlinehealth.features.chat.components.AiReply
import ph.appbuilders.offlinehealth.features.chat.components.Composer
import ph.appbuilders.offlinehealth.features.chat.components.DangerBanner
import ph.appbuilders.offlinehealth.features.chat.components.FirstAidCard
import ph.appbuilders.offlinehealth.features.chat.components.LanguageSheet
import ph.appbuilders.offlinehealth.features.chat.components.LanguageSheetContent
import ph.appbuilders.offlinehealth.features.chat.components.TopBar
import ph.appbuilders.offlinehealth.features.chat.components.TopicGrid
import ph.appbuilders.offlinehealth.features.chat.components.UserMessage

// Dev-only: every component state from the canvas Components row, on one scrolling page, so each can be compared
// with its artboard on a phone. Section labels are for the developer, not user copy. Not reachable in the shipped
// flow once navigation lands.

@Composable
fun ComponentGallery() {
    var language by rememberSaveable { mutableStateOf(Language.CEB) }
    var sheetOpen by remember { mutableStateOf(false) }
    val source = remember { FakeContentSource() }
    ProvideUiText(UiText(language, source::uiString)) {
        Column(
            Modifier
                .fillMaxSize()
                .background(Palette.Paper)
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState()),
        ) {
            TextButton(onClick = { language = if (language == Language.CEB) Language.ENG else Language.CEB }) {
                Text("Gallery language: ${language.name} (tap to switch)")
            }
            TopBarSection(onLanguageClick = { sheetOpen = true })
            ChatBlocksSection(language, source)
            AiReplySection(language)
            Section("TopicGrid") { TopicGrid(source.topics(language), onTopic = {}) }
            Section("LangSheet (content)") { LanguageSheetContent(onPick = { language = it }) }
            Section("FirstAidCard · flat (Topics)") {
                FirstAidCard(source.card(TopicId.CHILD_DIARRHEA, language), emptySet(), flat = true)
            }
        }
        if (sheetOpen) LanguageSheet(onPick = { language = it; sheetOpen = false }, onDismiss = { sheetOpen = false })
    }
}

@Composable
private fun TopBarSection(onLanguageClick: () -> Unit) {
    Label("TopBar · ready / starting / basic / compact")
    AiStatus.entries.forEach { status ->
        TopBar(status, {}, onLanguageClick, {}, {})
    }
    TopBar(AiStatus.READY, {}, onLanguageClick, {}, {}, showDivider = true, compact = true)
}

@Composable
private fun ChatBlocksSection(language: Language, source: FakeContentSource) {
    val english = language == Language.ENG
    var draft by rememberSaveable { mutableStateOf("") }
    Label("Composer · empty (type to see send)")
    Composer(draft, { draft = it }, onSend = { draft = "" })
    Label("Composer · typing")
    Composer(if (english) FakeSamples.USER_ENG else FakeSamples.USER_CEB, {}, {})
    Section("UserMsg") { UserMessage(if (english) FakeSamples.USER_ENG else FakeSamples.USER_CEB) }
    Section("DangerBanner") {
        DangerBanner(if (english) FakeSamples.dangerEngThree else FakeSamples.dangerCeb, onCall = {})
    }
    Section("FirstAidCard · matched") {
        val matched = if (english) setOf(0, 1, 3) else setOf(2)
        FirstAidCard(source.card(TopicId.CHILD_DIARRHEA, language), matched)
    }
}

@Composable
private fun AiReplySection(language: Language) {
    val english = language == Language.ENG
    val done = if (english) FakeSamples.REPLY_ENG else FakeSamples.REPLY_CEB
    listOf(
        "thinking" to AiReplyState.Thinking,
        "warming" to AiReplyState.WarmingUp,
        "streaming" to AiReplyState.Streaming(if (english) done.take(80) else FakeSamples.REPLY_CEB_PARTIAL),
        "done" to AiReplyState.Done(done),
        "withheld" to AiReplyState.Withheld,
        "basic" to AiReplyState.BasicMode,
    ).forEach { (name, state) -> Section("AIReply · $name") { AiReply(state, onDownloadAi = {}) } }
}

@Composable
private fun Section(name: String, content: @Composable ColumnScope.() -> Unit) {
    Label(name)
    Column(Modifier.padding(horizontal = Space.gutter, vertical = Space.md), content = content)
}

@Composable
private fun Label(name: String) {
    HorizontalDivider(thickness = 1.dp, color = Palette.Divider)
    Text(
        name,
        modifier = Modifier.padding(horizontal = Space.gutter, vertical = Space.xs),
        style = MaterialTheme.typography.labelMedium,
        color = Palette.InkSubtle,
    )
}
