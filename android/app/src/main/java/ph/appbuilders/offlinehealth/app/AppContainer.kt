package ph.appbuilders.offlinehealth.app

import android.content.Context
import android.content.pm.ApplicationInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import ph.appbuilders.offlinehealth.BuildConfig
import ph.appbuilders.offlinehealth.content.ContentSource
import ph.appbuilders.offlinehealth.domain.model.SetupState
import ph.appbuilders.offlinehealth.fakes.FakeChatService
import ph.appbuilders.offlinehealth.fakes.FakeContentSource
import ph.appbuilders.offlinehealth.fakes.FakeLanguageSettings
import ph.appbuilders.offlinehealth.fakes.FakeModelSetupService
import ph.appbuilders.offlinehealth.fakes.FakeTriage
import ph.appbuilders.offlinehealth.features.chat.ChatService
import ph.appbuilders.offlinehealth.features.chat.components.MenuAction
import ph.appbuilders.offlinehealth.features.modelsetup.ModelSetupService
import ph.appbuilders.offlinehealth.lib.llm.OllamaChatService
import ph.appbuilders.offlinehealth.lib.llm.OllamaClient
import ph.appbuilders.offlinehealth.lib.settings.LanguageSettings

/**
 * Manual wiring, one instance per process. The core session swaps each fake for its real implementation
 * here and only here (frontend brief §3). Nothing else in the UI knows which implementation it has.
 *
 * Debug builds talk to the model in Ollama on the laptop (BuildConfig.USE_OLLAMA, see ollama/README.md).
 * Release builds stay on the fakes until the model runs on the phone.
 */
class AppContainer(context: Context) {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val content: ContentSource = FakeContentSource()

    /** Starts with no language, so every launch of the fake shows the first-run picker and setup. */
    val languageSettings: LanguageSettings = FakeLanguageSettings(initial = null)

    private val triage = FakeTriage(content)

    private val ollamaChat: OllamaChatService? =
        if (BuildConfig.USE_OLLAMA) {
            OllamaChatService(
                client = OllamaClient(BuildConfig.OLLAMA_URL, BuildConfig.OLLAMA_MODEL),
                triage = { text, language -> triage.script(text, language).result },
                scope = appScope,
            )
        } else {
            null
        }
    private val fakeChat = FakeChatService(triage, appScope)
    val chatService: ChatService = ollamaChat ?: fakeChat

    // With Ollama the model already lives on the laptop, so setup starts Done (no fake download on stage).
    private val fakeSetup = FakeModelSetupService(
        scope = appScope,
        onModelReady = { ollamaChat?.warmUp() ?: fakeChat.onModelReady() },
        onBasicMode = { ollamaChat?.useBasicMode() ?: fakeChat.useBasicMode() },
        initial = if (ollamaChat != null) SetupState.Done else SetupState.Intro(FakeModelSetupService.SIZE_MB),
    )
    val modelSetup: ModelSetupService = fakeSetup

    private val debuggable = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

    /** Debug builds only: controls over the AI so every state can be shown. Empty once real services land. */
    val debugActions: List<MenuAction> =
        when {
            !debuggable -> emptyList()
            ollamaChat != null -> listOf(
                MenuAction("Debug: AI basic mode") { ollamaChat.useBasicMode() },
                MenuAction("Debug: reconnect to Ollama") { ollamaChat.warmUp() },
            )
            else -> listOf(
                MenuAction("Debug: basic mode on/off") { fakeChat.toggleBasicMode() },
                MenuAction("Debug: restart AI warm-up") { fakeChat.restartWarmUp() },
            )
        }

    /** Debug builds only: put the fake setup in an error state. AppNavigation also opens the setup screen. */
    val debugSetupActions: List<MenuAction> =
        if (!debuggable) {
            emptyList()
        } else {
            listOf(
                "no internet" to SetupState.NoInternet,
                "not enough storage" to SetupState.NotEnoughStorage(neededMb = 740, freeMb = 410),
                "download failed" to SetupState.DownloadFailed(doneMb = 312, totalMb = 740),
                "file check failed" to SetupState.CheckFailed,
            ).map { (label, state) -> MenuAction("Debug: setup, $label") { fakeSetup.show(state) } }
        }
}
