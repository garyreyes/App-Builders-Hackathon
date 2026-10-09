package ph.appbuilders.offlinehealth.app

import android.content.Context
import android.content.pm.ApplicationInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import ph.appbuilders.offlinehealth.content.ContentSource
import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.fakes.FakeChatService
import ph.appbuilders.offlinehealth.fakes.FakeContentSource
import ph.appbuilders.offlinehealth.fakes.FakeLanguageSettings
import ph.appbuilders.offlinehealth.features.chat.ChatService
import ph.appbuilders.offlinehealth.features.chat.components.MenuAction
import ph.appbuilders.offlinehealth.lib.settings.LanguageSettings

/**
 * Manual wiring, one instance per process. The core session swaps each fake for its real implementation
 * here and only here (frontend brief §3). Nothing else in the UI knows which implementation it has.
 */
class AppContainer(context: Context) {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val content: ContentSource = FakeContentSource()

    val languageSettings: LanguageSettings = FakeLanguageSettings(initial = Language.CEB)

    private val fakeChat = FakeChatService(content, appScope)
    val chatService: ChatService = fakeChat

    private val debuggable = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

    /** Debug builds only: controls over the fakes so every state can be shown. Empty once real services land. */
    val debugActions: List<MenuAction> =
        if (!debuggable) {
            emptyList()
        } else {
            listOf(
                MenuAction("Debug: basic mode on/off") { fakeChat.toggleBasicMode() },
                MenuAction("Debug: restart AI warm-up") { fakeChat.restartWarmUp() },
            )
        }
}
