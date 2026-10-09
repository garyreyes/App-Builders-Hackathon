# Frontend brief: build the Android UI from the Claude Design canvas

> For a Claude Code session working **only on the frontend**, in the worktree
> `C:\Claude\App-Builder-Hackathon\App-Builders-Hackathon-frontend` on branch **`feat/frontend`**.
> Another session works on the core (llama.cpp, triage, guardrail) in the main folder at the same time.
> Stay inside the boundaries below so the two don't collide.
> Feature freeze **7:00 AM Sat Oct 10**. Everything merged must build.

## 1. Read first (in this order)

1. `AGENTS.md`, then `docs/ARCHITECTURE.md`. The stack, **folder structure**, and **layer rules** there are binding.
2. `docs/PRD.md` §3 (use cases), §7 (edge cases, every state the UI must handle), §8 (priorities).
3. The design brief that produced the canvas. It's **local only** (excluded from git, so it's not in this worktree):
   `C:\Claude\App-Builder-Hackathon\App-Builders-Hackathon\docs\design\handover.md`. Read it, but never copy it into git.
4. **The design itself, local export:** `docs/design/claude-design/HANDOFF.md` first (how to read the files, screen and
   component maps, non-negotiables), then the artboards in `docs/design/claude-design/design/*.dc.html`, starting with
   `Tokens.dc.html` and `Specs.dc.html`. They're HTML mockups: **read them as a spec, don't run them, and translate
   to Compose.** Backup: the live canvas https://claude.ai/artifact/6QdTYUFQ13hLRT5RA2kbNT. The export is
   **excluded from git** (`.git/info/exclude`). Don't commit it unless the owner says so.

### Where HANDOFF.md and this brief disagree, this brief wins

HANDOFF.md was written by the design tool without the project docs. Override it on these points:
- **No camera, photo, attach menu, or voice in v1.** Skip HANDOFF screens C14–C19, `AttachMenu`, `Camera`, build
  step 6, rule 6, the voice half of rule 5, and the `photo` / `PhotoAttached` / `Listening` parameters. **Composer:**
  no paperclip and no mic. The send button appears only when the field has text, so there's still never a disabled send.
- **Interfaces:** use §4 below, not HANDOFF §6 (`TriageEngine` / `AiHelper` / `ModelDownloader`). It covers the same
  states and follows ARCHITECTURE.md's layers, and the core session codes against §4.
- **Strings:** no `strings.xml` locale folders and no `AppCompatDelegate`. The app's own language comes from
  `LanguageSettings`, and all copy comes from `ContentSource.uiString(key, language)` (later JSON the teammate edits,
  per DECISIONS.md). Get copy to composables through a CompositionLocal or ViewModel state. Never hard-code it in a
  composable (same goal as HANDOFF §5).
- **Everything else in HANDOFF.md holds:** reading rules (§1), screen and component maps (§2–3, minus the items above),
  non-negotiables 1–4, 7, and 8, the ready-made Compose block in `Tokens.dc.html`, the Atkinson Hyperlegible Next fonts,
  and `@Preview`s for every state.

The planning and the visual direction are **done and pinned** by the canvas. Don't restyle it, don't re-roll a
direction, and don't run Impeccable's new-design flow. Translate faithfully. Red means danger only. No red cross.

## 2. What to build (in-scope artboards)

| Group       | Artboards                                                                                                                                                                                            |
| ----------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Foundations | `Tokens` → Compose theme (colors, type scale, spacing, radius). `Specs` → component sizes and paddings                                                                                               |
| Components  | `TopBar`, `Composer`, `UserMsg`, `DangerBanner`, `FirstAidCard`, `AIReply`, `LangSheet`, `TopicGrid`                                                                                                 |
| Language    | `Language`                                                                                                                                                                                           |
| Model setup | `Setup` + `SetupDefault`, `SetupDownloading`, `SetupVerifying`, `SetupDone`, `SetupNoInternet`, `SetupStorage`, `SetupFailed`, `SetupCheckFailed`                                                    |
| Chat        | `ChatEmpty`, `ChatTyping`, `Main` (C3: banner + card, AI thinking), `ChatWarming`, `ChatStreaming`, `ChatComplete`, `ChatWithheld`, `ChatNotCovered`, `ChatDangerOnly`, `ChatBasic`, `ChatEnglish`, `ChatNarrow` |
| Topics      | `Topics`, `TopicDetail`                                                                                                                                                                              |
| Settings    | `Settings`                                                                                                                                                                                           |

**Do not build (out of scope for v1):** `Camera`, `ChatPhoto`, `ChatPhotoSent`, `AttachMenu`, `ChatMenu`,
`ChatBasicMenu` (attach and photo flows, no vision model), and `ChatListening` (no voice in v1, PRD §4). If
`Composer` or a menu shows camera, photo, attach, or mic controls, leave them out.

Suggested PR order: **(1)** scaffold + theme, **(2)** components, **(3)** chat screen with every state,
**(4)** language + setup + topics + settings. Keep each PR small and buildable. Stop wherever 6:00 AM lands.

## 3. Boundaries: what's yours vs the core session's

| Yours (frontend)                                                                            | Not yours (core session, don't create these files)                                      |
| ------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------- |
| `android/` scaffold: `settings.gradle.kts`, root + `app/` build files, Gradle wrapper       | `android/llama/` module (llama.cpp + JNI)                                               |
| `app/` package: `MainActivity`, `app/AppContainer.kt`, `app/AppNavigation.kt`, `app/theme/` | `domain/Triage.kt`, `domain/Guardrail.kt`, `domain/PromptBuilder.kt`                    |
| `features/*/`: Screens, `components/`, ViewModels                                           | `content/HealthLibrary.kt` + real `assets/content/*.json` (teammate writes the content) |
| `domain/model/`: the types in §4                                                            | `lib/llm/*`, `lib/memory/*`, `lib/settings/SettingsStore.kt`                            |
| Service **interfaces** (§4) + **fakes** in `fakes/`                                         | Real service implementations                                                            |

The core session implements your interfaces and swaps the fakes **only in `AppContainer`**. If you need something
outside your column, define an interface and fake it. Don't build the real thing.

## 4. The contract (create these exactly; the core session codes against them)

Package root `ph.appbuilders.offlinehealth`. Enums `Language`, `TopicId`, `DangerSignId` exactly as listed in
`docs/ARCHITECTURE.md` → Data model.

```kotlin
// domain/model/
data class TopicCard(val topicId: TopicId, val title: String, val atHome: List<String>,
                     val goNowIf: List<String>, val source: String)
data class DangerMessage(val id: DangerSignId, val text: String)
data class TopicSummary(val topicId: TopicId, val title: String)
sealed interface AiReplyState {               // names match AIReply.dc.html
    data object Thinking : AiReplyState
    data object WarmingUp : AiReplyState       // sent while the model is still starting
    data class Streaming(val text: String) : AiReplyState
    data class Done(val text: String) : AiReplyState
    data object Withheld : AiReplyState        // guardrail blocked it: quiet "Follow the card above"
    data object BasicMode : AiReplyState       // no model on this phone
}
data class ChatResult(
    val dangers: List<DangerMessage>,          // banner shows max 3
    val card: TopicCard?,                      // null + dangers = danger only (C10); null + none = not covered (C9)
    val matchedSigns: Set<Int>,                // indexes into card.goNowIf to highlight
    val otherTopics: List<TopicSummary>,       // "Also about" chips
    val ai: AiReplyState?,                     // null = no AI block (no topic matched)
)
enum class AiStatus { READY, STARTING, BASIC } // TopBar pill
sealed interface SetupState {                  // names match Setup.dc.html
    data class Intro(val sizeMb: Int) : SetupState
    data class Downloading(val doneMb: Int, val totalMb: Int) : SetupState
    data object Verifying : SetupState
    data object Done : SetupState
    data object NoInternet : SetupState
    data class NotEnoughStorage(val neededMb: Int, val freeMb: Int) : SetupState
    data class DownloadFailed(val doneMb: Int, val totalMb: Int) : SetupState
    data object CheckFailed : SetupState
}

// interfaces (put each next to its feature, e.g. features/chat/ChatService.kt)
interface ChatService {   // first emission = instant triage result (ai = Thinking/WarmingUp/BasicMode), then AI updates
    fun send(text: String, language: Language): Flow<ChatResult>
    val aiStatus: StateFlow<AiStatus>
}
interface ModelSetupService {
    val state: StateFlow<SetupState>
    fun startDownload(); fun cancel(); fun retry(); fun useBasicMode()
}
interface ContentSource {  // core's HealthLibrary implements this later
    fun topics(language: Language): List<TopicSummary>
    fun card(topicId: TopicId, language: Language): TopicCard
    fun uiString(key: String, language: Language): String
}
interface LanguageSettings { val language: StateFlow<Language?>; fun setLanguage(language: Language) }
```

**Fakes (`fakes/Fake*.kt`, clearly named, wired in `AppContainer`):** every in-scope state must be reachable from
the real UI, so screenshots and the demo can show each one.

- `FakeChatService`: text with _dugo / blood_ → danger + diarrhea card + streamed reply. _toothache / ngipon_ →
  not covered. _seizure / kombulsyon_ → danger only. _withheld_ → card + `Withheld`. Anything else → diarrhea card +
  reply streamed word by word (~80 ms per word). `aiStatus` starts `STARTING` and turns `READY` after 3 s. A
  debug-only toggle switches to `BASIC`.
- `FakeModelSetupService`: walks Intro → Downloading (fake progress) → Verifying → Done, with a way to
  trigger each error state (e.g. a long-press or a debug-only menu).
- `FakeContentSource`: English, plus the Bisaya sample from handover §8, for the 7 topics (titles + one full card).
  Mark every string as placeholder.

## 5. Build setup (copy from `spikes/android-llm`, with these changes)

- Copy the Gradle **wrapper** (Gradle 9.5.0) and the `settings.gradle.kts` repository setup from `spikes/android-llm/`.
- AGP **9.3.1**, built-in Kotlin. Compose compiler plugin **2.2.10**, matching ARCHITECTURE.md. **Drop** the spike's
  Kotlin 2.4 buildscript bump and the LiteRT-LM dependency. If the build reports a Kotlin/Compose version mismatch,
  use the version the error names and note it in the PR.
- `namespace`/`applicationId` `ph.appbuilders.offlinehealth`. **`minSdk = 28`**, `compileSdk`/`targetSdk` 37 (as in the spike).
  `ndk { abiFilters += "arm64-v8a" }`. JDK 17. Compose BOM **2026.02.01** + Material 3.
- **Pin every dependency exactly.** No `+`, no `latest`. Keep dependencies few. Prefer simple state-driven navigation
  (sealed screen + `when`) over a navigation library.
- **Offline app:** bundle the **Atkinson Hyperlegible Next** TTFs (Regular, SemiBold, Bold, ExtraBold) in `res/font/`
  with their OFL license. No downloadable Google Fonts. Icons are the Material Symbols named in `Tokens.dc.html`
  (Outlined, weight 400, 24), as vector drawables for just the icons used. Avoid the huge extended icon set.
- Manifest: **no `INTERNET` permission yet** (the core session adds it with the model downloader).
  `allowBackup=false`, `usesCleartextTraffic=false`, only `MainActivity` exported.

## 6. Rules that apply to every file

- **Layers:** Screens/components only render state and forward events. ViewModels hold UI state and call **one**
  service. No triage, guardrail, or medical logic anywhere in your column.
- **Code shape:** files 200–400 lines (800 max), functions < 50 lines, nesting ≤ 4 levels. Never mutate state:
  `copy()` / new lists only.
- **UX floor:** design at 360×800 dp. Check **320 dp width** and **font scale 1.5×**. Touch targets ≥ 48 dp, body ≥ 16 sp,
  WCAG AA (aim for AAA on danger text). Danger is **never color-only** (icon + bold text + red). Motion is feedback only.
- No tests for layout or copy (ARCHITECTURE testing policy). Don't log user text.

## 7. Done means

- `cd android && ./gradlew :app:assembleDebug` succeeds, and the APK installs and runs on the API 28+ arm64 phone (or an emulator if no phone).
- Every in-scope artboard state has been reached on the device and visually compared with its artboard. List any
  differences in the PR.
- The PR to `main` from `feat/frontend` (see `docs/collaboration.md`) lists: artboards done, what's faked, any
  version changes, and what wasn't verified. Never claim something was tested on the phone unless it was.
