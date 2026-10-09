# Architecture: Offline Multilingual Health Helper

> v1, Oct 9, ~22:15. Source of truth for the **tech stack**. Product truth lives in `docs/PRD.md`.
> Verified facts behind these choices: `docs/PROJECT_FACTS.md`. Fork reasoning: `docs/DECISIONS.md`.

## What it does

A native Android app. Someone with no signal types a health worry in **Bisaya, Waray, Tagalog/Taglish, or English**.
The app checks it for danger signs instantly with a keyword glossary, then an **on-device Gemma 4 E2B model**
classifies it into one of 7 first-aid topics. The app shows a **pre-translated topic card** in the user's language.
The internet is used **once**, to download the model. After that, everything works in airplane mode.

## Tech stack

| Layer | Choice | Why (if it was a fork) |
|---|---|---|
| Language | Kotlin **2.4.0** | Forced: LiteRT-LM 0.17.1 is compiled with Kotlin 2.4 |
| Build | AGP **9.3.1**, Gradle **9.5.0**, JDK 17, `kotlin-gradle-plugin:2.4.0` on the buildscript classpath | Matches the working spike and the existing Vault project |
| UI | Jetpack Compose (BOM **2026.02.01**) + Material 3, Navigation Compose | LiteRT-LM has a native Kotlin API, so no bridge is needed (vs Flutter/RN) |
| On-device LLM runtime | **LiteRT-LM** `com.google.ai.edge.litertlm:litertlm-android:0.17.1` | Google's current runtime. MediaPipe LLM Inference is deprecated on Android |
| Model | **Gemma 4 E2B-it** `.litertlm` (Apache-2.0, ungated) | ~1B models failed the language test (PROJECT_FACTS.md) |
| JSON | kotlinx.serialization (plugin 2.4.0) | Content files + model output parsing |
| Settings | Jetpack DataStore (Preferences) | Language + backend choice only |
| Model download | Android **DownloadManager** (system service) | Resumable, survives app kill, shows a system notification. No custom HTTP code |
| Tests | JUnit 4 (local JVM unit tests) | Test-first for triage/merge/parse/content validation |
| Eval (laptop) | Python + Ollama `gemma4:e2b` as a proxy for the phone model | Fast scoring of the 20-message test set |
| Database / backend / hosting | **None** | No accounts, no history, no server (PRD §4) |
| Distribution | GitHub Release: debug-signed APK. Model fetched by the app from Hugging Face | Repo never holds model files |

minSdk 26, targetSdk/compileSdk 37. Versions are **pinned exactly**, never `latest.release`.

## Configuration (no secrets)

There are **no API keys or secrets** in this project, so there's no env-var contract.
The only environment-dependent values are build constants in `lib/llm/ModelSpec.kt`:

| Constant | Value |
|---|---|
| `MODEL_URL` | `https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/b3ca0d2f076785a8f4b2219ddbd2bdb99954eae1/gemma-4-E2B-it.litertlm` (pinned to a commit, not `main`) |
| `MODEL_SHA256` | `181938105e0eefd105961417e8da75903eacda102c4fce9ce90f50b97139a63c` (verified against the laptop download) |
| `MODEL_BYTES` | `2588147712` |
| Alt (GPU variant, 2.0 GB) | `gemma-4-E2B-it-gpu.litertlm`, sha256 `a53a59001894c58e6bdb5b9b227709f91a2e3e556baa7d85acf9c55402ba5cf5`. Swap in only if the phone test says GPU wins |

## Data model

No database. Content is **versioned JSON in `app/src/main/assets/content/`**, edited directly by the team (decision in
DECISIONS.md) and validated by unit tests on every build. Runtime state is in memory only.

### Enums (closed sets, defined once in `domain/model/`)
- `Language`: `CEB` (Bisaya), `WAR` (Waray), `TGL` (Tagalog/Taglish), `ENG`
- `TopicId`: `CHILD_DIARRHEA`, `FEVER`, `COUGH_BREATHING`, `WOUND_BLEEDING`, `BURN`, `PREGNANCY_WARNING`, `DENGUE_WARNING`, `NONE`
- `DangerSignId`: `CANNOT_DRINK`, `VOMITS_EVERYTHING`, `BLOOD_IN_STOOL`, `VERY_SLEEPY`, `DIFFICULTY_BREATHING`,
  `SEVERE_BLEEDING`, `PREGNANCY_BLEEDING`, `SEIZURE`, `CHEST_PAIN`, `UNCONSCIOUS`

### Content entities
```
TopicId 1---4 TopicCard        (exactly one card per Language)
DangerSignId 1---4 DangerMessage (exactly one message per Language)
GlossaryEntry *---1 TopicId | DangerSignId   (each entry points to exactly one target)
UiStrings: key 1---4 text      (one per Language)
```

| File | Entity | Key | Required fields / constraints |
|---|---|---|---|
| `topics/<topic_id>.json` | `TopicCard` ×4 | (`topicId`, `language`) | `title`, `atHome[]` (≥1), `goNowIf[]` (≥1), `source` (non-empty citation). All 4 languages present |
| `danger_signs.json` | `DangerMessage` ×4 per sign | (`dangerSignId`, `language`) | `text` non-empty. Every `DangerSignId` has all 4 languages |
| `glossary.json` | `GlossaryEntry` | normalized `term` (unique) | `term`, `target` (valid TopicId/DangerSignId), `stem` (bool, default false) |
| `ui_strings.json` | `UiString` | (`key`, `language`) | Every key used in code has all 4 languages |

**Enforced by `ContentValidationTest`** (fails the build): JSON parses, every enum value covered in every language,
no empty required fields, glossary terms unique after normalization, glossary targets valid, every topic and danger
sign reachable by at least one glossary term.

### Runtime types (in memory, never persisted)
- `TriageResult(dangers: Set<DangerSignId>, topics: List<TopicId>, source: KEYWORD | MODEL | MERGED, modelStatus)`
- `ChatTurn(userText, result, cards)`. Cleared when the app closes (PRD: no history).

### Persisted settings (DataStore)
- `language: Language` (unset → show language picker)
- `backend: CPU | GPU` (default from the phone test)
- Model presence is checked from the file on disk (size + sha256 verified once, then a marker file is written), not stored as a flag.

## Core flows

### 1. Send pipeline
```
user taps Send
  │
  ├─(sync, <1 ms)  Triage.keywordScan(text, glossary) → dangers_k, topics_k
  │                 UI immediately shows 🔴 DangerMessage(s) (if any) + top keyword TopicCard
  │
  └─(async)        Classifier.classify(text) via LlmEngine (Gemma 4 E2B on device)
                    → parse JSON → dangers_m, topic_m (unknown ids dropped; invalid JSON = no result)
                    Merge(k, m) → final TriageResult → UI updates
```

**Merge rules** (pure function, test-first):
- `dangers = dangers_k ∪ dangers_m`. Never subtract a keyword danger. False alarms are the safe direction.
- Primary topic = `topic_m` if valid and not `NONE`, else first of `topics_k`, else `NONE`.
- Alternate topics = remaining `topics_k` (shown as buttons).
- `NONE` with no dangers → the "not covered, go to the health center" UiString.

### 2. Model lifecycle
```
App start → model file present + verified? ── no → ModelSetupScreen: "Download AI (2.6 GB, Wi-Fi)"
   │                                                → DownloadManager → verify size + SHA-256 → marker file
   yes                                                (fail → delete file, show retry)
   ↓
LlmEngine.initialize() on a background dispatcher (CPU/GPU per setting) → status: WARMING → READY | FAILED
   FAILED → keyword-only mode (PRD §7), with a notice. App never crashes on model failure
```

### 3. Classifier contract
- One fresh `Conversation` per message, temperature 0, short system instruction (the prompt that scored
  5/6 topics and 3/3 dangers in the laptop test). It lists the topic ids, danger ids, and a short glossary hint.
- Output: `{"topic": "<topic_id|none>", "danger_signs": ["<danger_id>", ...]}`. The parser extracts the first `{…}`
  block, parses leniently, and drops unknown ids.
- The model's language label is **not** used. Language always comes from settings.
- "More detail" (Should): a second conversation with the matched card's English text + the user message. Reply shown
  labeled "AI summary (English)".
- User text is trimmed to 500 characters before the model.

## Permissions and security baseline

No auth, no payments, no backend, so the auth/payment hard-halts don't apply. Checklist for this stack:

- [ ] **INTERNET permission is used for one thing only:** the DownloadManager request to the pinned `MODEL_URL`.
      No other network code exists. `lib/llm/ModelDownloader.kt` is the only file that references a URL.
- [ ] **Model integrity:** size and SHA-256 are checked (`java.security.MessageDigest`, standard library, not
      hand-rolled) before first load. A mismatch deletes the file.
- [ ] **HTTPS only:** `android:usesCleartextTraffic="false"`.
- [ ] **No user text leaves the device**, is written to disk, or is logged. No `Log.*` of message content.
- [ ] `android:allowBackup="false"`, so settings aren't cloud-backed-up.
- [ ] Only `MainActivity` is exported. No other exported components.
- [ ] Dependencies pinned to exact versions. Nothing from untrusted repositories (google + mavenCentral only).
- [ ] Input length capped (500 chars) before inference.
- [ ] Medical-safety guardrails are enforced in code, not just the prompt: local-language replies only come from
      content files, danger detection never depends on the model alone, and there's always a disclaimer line.

## Folder structure

The spike (`spikes/android-llm/`) stays as a record. The real app is a new Gradle project at `android/`.

```
android/
  build.gradle.kts, settings.gradle.kts, gradle.properties, gradlew(.bat), gradle/wrapper/
  app/
    build.gradle.kts
    src/main/
      AndroidManifest.xml
      assets/content/
        topics/child_diarrhea.json … dengue_warning.json   ← team edits these
        danger_signs.json
        glossary.json
        ui_strings.json
      java/ph/appbuilders/offlinehealth/
        MainActivity.kt              ← entry point: sets content, nothing else
        app/
          AppContainer.kt            ← manual dependency wiring (no Hilt)
          AppNavigation.kt           ← routes between screens. Thin
          theme/                     ← colors, type, spacing tokens
        features/
          onboarding/                ← language picker
            LanguagePickerScreen.kt
          modelsetup/                ← download + verify + status
            ModelSetupScreen.kt
            ModelSetupViewModel.kt
          chat/                      ← main screen
            ChatScreen.kt
            components/              ← DangerBanner, TopicCardView, MessageInput, StatusPill
            ChatViewModel.kt
            ChatService.kt           ← orchestrates keyword triage → classifier → merge
          topics/                    ← browse cards without typing
            TopicListScreen.kt
        domain/                      ← PURE Kotlin: no Android imports, fully unit-tested
          model/                     ← Language, TopicId, DangerSignId, TriageResult, content data classes
          Triage.kt                  ← normalize + keyword scan
          Merge.kt                   ← merge rules above
          ClassifierPrompt.kt        ← prompt builder + output parser
        content/
          ContentRepository.kt       ← loads + caches assets/content JSON
        lib/
          llm/
            LlmEngine.kt             ← the ONLY file that imports com.google.ai.edge.litertlm
            ModelSpec.kt             ← URL, sha256, bytes
            ModelDownloader.kt       ← DownloadManager + verification. Only network code in the app
          settings/SettingsStore.kt  ← DataStore
    src/test/java/ph/appbuilders/offlinehealth/
      domain/TriageTest.kt, MergeTest.kt, ClassifierPromptTest.kt
      content/ContentValidationTest.kt   ← reads the real assets/content files
tools/
  eval/
    test_set.json                    ← 20 messages + expected topic/dangers (team-written)
    run_eval.py                      ← scores keyword triage + gemma4:e2b via Ollama, prints metrics table
docs/  (PRD, ARCHITECTURE, PROJECT_FACTS, DECISIONS, …)
spikes/android-llm/                  ← on-device feasibility spike (kept for history)
```

### Layer rules (binding)
| Layer | Android equivalent | May | Must not |
|---|---|---|---|
| UI | `*Screen.kt`, `components/` | Render state, forward clicks to the ViewModel | Call services/lib, hold business rules, import LiteRT-LM |
| Routing / thin layer | `*ViewModel.kt`, `AppNavigation.kt` | Hold UI state, call one service, map results to UI state | Contain triage/merge logic |
| Business logic | `*Service.kt`, `domain/` | All decisions (triage, merge, parsing) | Touch Android UI. `domain/` must not import Android at all |
| Infrastructure | `lib/`, `content/` | Talk to LiteRT-LM, DownloadManager, DataStore, assets | Contain medical/business rules |

**Where new code goes:** a new screen → `features/<name>/`. A new medical rule → `domain/` with a test first.
Anything touching the model, network, or storage → `lib/`. A new phrase → `assets/content/*.json` (all 4 languages,
or `ContentValidationTest` fails).

### Structure check
- Feature-based, not type-based ✅
- Each entity defined once (`domain/model/`) ✅
- One shared failure path: `LlmEngine` status (`WARMING/READY/FAILED`) → keyword-only mode ✅
- The SDK is wrapped once (`lib/llm/LlmEngine.kt`) ✅
- Every outbound call is in `lib/` ✅

## Testing policy
- **Test-first (correctness-critical):** `Triage` (every danger term in the glossary must trigger its sign),
  `Merge` (never drops a keyword danger), `ClassifierPrompt` parsing (bad JSON → no result, unknown ids dropped),
  `ContentValidationTest`, and the SHA-256 check in `ModelDownloader`.
- **No tests:** layout, colors, copy, animation. Those go through review and design passes.
- **Eval:** `tools/eval/run_eval.py` must report the PRD §5 metrics before the 7:00 AM freeze.

## Open decisions
1. **Model variant** (`.litertlm` 2.6 GB CPU-capable vs `-gpu` 2.0 GB) and **default backend**: decide after the
   on-device speed test on the 6 GB demo phone. Recorded in PROJECT_FACTS.md.
2. **App/package name:** `ph.appbuilders.offlinehealth` is a working placeholder **[ASSUMPTION]**. The display name
   changes when the product name is chosen. The package id is fixed after the first release APK.
3. **Card sources:** which DOH/WHO documents each card cites (content task, not code).
