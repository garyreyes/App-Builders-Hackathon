package ph.appbuilders.offlinehealth.app

import android.content.Context
import android.content.pm.ApplicationInfo
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import ph.appbuilders.offlinehealth.BuildConfig
import ph.appbuilders.offlinehealth.content.ContentSource
import ph.appbuilders.offlinehealth.content.DemoContentSource
import ph.appbuilders.offlinehealth.domain.model.SetupState
import ph.appbuilders.offlinehealth.fakes.FakeChatService
import ph.appbuilders.offlinehealth.fakes.FakeLanguageSettings
import ph.appbuilders.offlinehealth.fakes.FakeModelSetupService
import ph.appbuilders.offlinehealth.fakes.FakeTriage
import ph.appbuilders.offlinehealth.features.chat.ChatService
import ph.appbuilders.offlinehealth.features.chat.InstantResult
import ph.appbuilders.offlinehealth.features.chat.components.MenuAction
import ph.appbuilders.offlinehealth.features.modelsetup.ModelSetupService
import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.lib.llm.HealthPrompt
import ph.appbuilders.offlinehealth.lib.llm.LiteRtClient
import ph.appbuilders.offlinehealth.lib.llm.LlmChatService
import ph.appbuilders.offlinehealth.lib.llm.LlmClient
import ph.appbuilders.offlinehealth.lib.llm.OllamaClient
import ph.appbuilders.offlinehealth.lib.settings.LanguageSettings

/**
 * Manual wiring, one instance per process. The core session swaps each fake for its real implementation
 * here and only here (frontend brief §3). Nothing else in the UI knows which implementation it has.
 *
 * Where the AI runs, first match wins:
 * 1. Debug builds with preferOllama=true: the selected laptop model over adb, even if Gemma is installed.
 * 2. ON THE PHONE: the Gemma 4 E2B file is in the app's storage → LiteRT-LM, fully offline (any build).
 * 3. Other debug builds: the laptop's Ollama over adb (see ollama/README.md).
 * 4. Otherwise the fakes.
 */
class AppContainer(context: Context) {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    // The three topics with written cards; no placeholder card can reach the screen.
    private val demoContent = DemoContentSource()
    val content: ContentSource = demoContent

    /** Starts with no language, so every launch of the fake shows the first-run picker and setup. */
    val languageSettings: LanguageSettings = FakeLanguageSettings(initial = null)

    private val instantResult = InstantResult(demoContent)

    private val phoneModel = File(context.getExternalFilesDir(null), PHONE_MODEL_FILE)

    /** Which AI the app is using, for the debug menu and the demo ("on this phone" vs "laptop"). */
    val aiSource: String =
        when {
            BuildConfig.USE_OLLAMA && BuildConfig.PREFER_OLLAMA -> "laptop via Ollama (${BuildConfig.OLLAMA_MODEL})"
            phoneModel.isFile -> "on this phone (Gemma 4 E2B, LiteRT-LM)"
            BuildConfig.USE_OLLAMA -> "laptop via Ollama (${BuildConfig.OLLAMA_MODEL})"
            else -> "none (fakes)"
        }

    private val llmClient: LlmClient? =
        when {
            BuildConfig.USE_OLLAMA && BuildConfig.PREFER_OLLAMA -> OllamaClient(BuildConfig.OLLAMA_URL, BuildConfig.OLLAMA_MODEL)
            phoneModel.isFile -> LiteRtClient(phoneModel, context.cacheDir.path)
            BuildConfig.USE_OLLAMA -> OllamaClient(BuildConfig.OLLAMA_URL, BuildConfig.OLLAMA_MODEL)
            else -> null
        }

    private val ollamaChat: LlmChatService? =
        llmClient?.let { client ->
            LlmChatService(
                client = client,
                triage = instantResult::of,
                scope = appScope,
                // Grounded on the checked English card (the reviewed source), answered in the user's language.
                prompt = { result, language ->
                    HealthPrompt.build(result.card?.let { demoContent.card(it.topicId, Language.ENG) }, language)
                },
                // V3 is experimental: hold its whole reply until the guardrail has checked the final text.
                holdUntilReviewed = BuildConfig.USE_OLLAMA && BuildConfig.PREFER_OLLAMA,
            )
        }
    private val fakeChat = FakeChatService(FakeTriage(content), appScope)
    val chatService: ChatService = ollamaChat ?: fakeChat

    // With a real model (phone file or Ollama) setup starts Done: no fake download on stage.
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
                MenuAction("Debug: reconnect AI ($aiSource)") { ollamaChat.warmUp() },
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

    private companion object {
        /** Side-loaded with adb into /sdcard/Android/data/ph.appbuilders.offlinehealth/files/ (ollama/README.md). */
        const val PHONE_MODEL_FILE = "gemma-4-E2B-it.litertlm"
    }
}
