# Project decisions

`ARCHITECTURE.md` records what was chosen. This file records **why**. Revisit a decision when real device
measurements or Waray speaker evaluation contradict it, and record the changed evidence and date here.

## Summary table

| Decision | Reason | Status |
| --- | --- | --- |
| Fully offline Android app | User requirement. Model runs on the phone | Chosen |
| Android 9 / API 28 minimum, `arm64-v8a` | Extends the device range. Matches llama.cpp's documented native Android 28 build | Chosen; app integration untested |
| 6 GB RAM phone as first target | Team's representative midrange phone | Chosen; performance untested |
| Text chat before speech input | Lets each model's errors be measured separately | Chosen |
| **Product: offline health helper, 7 first-aid topics, 4 languages** | Owner's combined plan, 2026-10-09 ~22:50 | **Chosen** |
| **Sailor2-1B-Chat GGUF on llama.cpp** | Owner's combined plan. Published Waray coverage, 0.74 GB Q4_K_M | **Chosen**; laptop baseline tested (see below), phone untested |
| LoRA on reviewed Waray chat pairs | Adapts chat behavior without training a new foundation model | **Chosen**; ship only if it beats the baseline |
| Keyword glossary routes topics + danger signs | Sailor2-1B baseline failed JSON classification on Oct 9 | Chosen |
| Deterministic guardrail on every model reply | Sailor2-1B baseline invented doses and suggested antibiotics on Oct 9 | Chosen |
| App downloads the model once (SHA-256 verified) | Owner choice ("like `ollama pull`") | Chosen |
| Content edited as JSON directly, validated by tests | Owner choice | Chosen |
| Separate speech-to-text model | Text chat training cannot learn audio transcription | Planned (later milestone) |
| Keep model weights and raw/private data out of Git | Repository stays reviewable and respects sharing limits | Active |
| Preserve the `docs/planning` branch | Records the original BHW prenatal-visit concept | Active |
| **Content = "Health Library"; guardrail terms live in it** | Teammate's "unified information library for validation" idea. One verified source for facts *and* checks | Chosen |
| **The AI never asks for personal details** | Danger check must come first (≤ 1 s). Sailor2 failed structured extraction. Data minimization | Chosen |
| **Optional on-phone memory, used by the app, not the model (v1)** | Team wants the app to recognize the user. Keeps the LoRA template unchanged and private data out of model output | Chosen; build after core chat works |
| **Room for lists, DataStore for single values, one repository** | History and people grow; name/health center don't | Chosen; Room + KSP on AGP 9 unverified |
| **Health center from the user, 911 from content, never the model** | A 1B model would invent phone numbers | Chosen |
| **Store birth month, compute the group** | A stored "baby" goes stale and would apply the wrong danger rules | Chosen |
| **Demo AI via Ollama on the laptop, over USB (debug builds only)** | Trained GGUF not delivered yet and on-phone llama.cpp too risky before the Oct 10 freeze. Owner, Oct 10 ~01:45 | **Chosen for the demo**; release builds unchanged |
| **Guardrail strict: no medicine names, forms, or doses** | PRD metric (0 reach the screen). Owner, Oct 10 ~01:45, after the trained model was taught OTC names | Chosen; revisit after the trained-model swap |

## Details

### 2026-10-09: Combined plan (supersedes the two conflicting plans)
- **Decision:** the health-helper product scope from the `spike/android-llm` PRD, built on the language
  collaborator's model track (Sailor2-1B GGUF + llama.cpp + LoRA on Waray chat pairs).
- **Why:** owner decision after both plans were compared side by side.
- **Alternatives:** (a) general Waray assistant, no product use case (from `main`); (b) Gemma 4 E2B on LiteRT-LM,
  no training (from `spike/android-llm`). The LiteRT-LM spike app stays in `spikes/` as a record.

### 2026-10-09: Earlier pivots (kept for history)
- BHW visit tool → general offline health chatbot (team preference for a chatbot in native languages).
- Laptop demo → native Android, no laptop fallback (team wants a real phone app).

### 2026-10-09: Glossary routes, model writes, guardrail filters
- **Decision:** keyword glossary picks topics and danger signs. The model only writes the reply, grounded in the
  matched card. A pure-Kotlin `Guardrail` blocks doses, drug names, and diagnoses.
- **Why (measured):** Sailor2-1B baseline: classification JSON unusable (everything → "diarrhea", glossary words
  dumped as danger signs), but its Bisaya replies are more natural than Gemma's. It also invented ORS doses
  ("30 ml/kg") and recommended ibuprofen and antibiotics for a toothache. Gemma 4 E2B classified well but wrote English.
- **Alternatives:** model-based classification (failed on Sailor2), cards only with no model reply (safe but weak
  on the "local AI" criterion).

### 2026-10-09: App downloads the model on first launch
- **Why:** owner choice. Simplest for users and judges.
- **Alternatives:** import from phone storage (zero INTERNET permission), adb side-load (dev only), Ollama in Termux
  on the phone (judges would need a terminal).
- **Cost accepted:** the pitch line becomes "internet used once for the model download, then airplane mode."

### 2026-10-09: Content edited as JSON directly
- **Why:** owner choice. No converter to build.
- **Mitigation:** `ContentValidationTest` fails the build on broken JSON or a missing translation.

### 2026-10-09: Train on the RTX 4050 laptop
- **Why:** the 1050 Ti (4 GB) is too tight for LoRA tooling. The RTX 4050 (6 GB) already has PyTorch CUDA verified,
  and LoRA on a 1B model fits.

### 2026-10-09 (late): Health Library + memory (teammate's "unified information library" idea)
- **Context:** a teammate proposed a unified information library for information validation, stored in SQLite. The
  team's goal, as clarified by the owner, is for the app to recognize the user and their preferences. This
  supersedes PRD v2.1's "chat history is not stored [ASSUMPTION]".
- **Decision 1, Health Library:** the planned content JSON *is* the library. `content/HealthLibrary.kt` is its only
  reader. Guardrail terms move into it (`guardrail_terms.json`) so the team edits them in 4 languages and tests
  check them. Whether "validation" also means myth-checking the user's beliefs is open (`docs/teammate-tasks.md`).
- **Decision 2, the AI never interviews:** a stressed user must see the 🔴 within 1 s. Sailor2-1B failed structured
  extraction on Oct 9 and invents details. Asking for personal data up front breaks data minimization (RA 10173).
  The app offers to remember a person *after* a useful answer, with fixed text and a consent sheet.
  **Alternative rejected:** model-led intake questions on first launch.
- **Decision 3, memory is used by the app, not the model (v1):** greeting, person chips, banner text, and the recent
  list are all app-rendered. The prompt stays unchanged, so the LoRA training template is untouched, the 2048-token
  context isn't spent on history, and private details can't leak through model output. **v2:** add a one-line
  person summary to the prompt and retrain with that exact template.
- **Decision 4, storage:** Room (SQLite) for `person` + `history_entry`, DataStore for single values, both behind
  `MemoryRepository`. **Alternative:** DataStore only. It rewrites the whole file on every save and suits a handful of
  values, not a growing history. **Fallback:** if Room's KSP plugin doesn't build on AGP 9.3.1 within a 15-min spike,
  use the framework `SQLiteOpenHelper` behind the same repository.
- **Decision 5, health center and phone numbers:** 911 ships in content. The user types their own health center
  once. The model never writes numbers, and the guardrail blocks phone-number patterns. A wrong number in an
  emergency is the worst hallucination. **v2:** a bundled facility directory updated with app releases.
- **Decision 6, birth month, not group:** the group is computed each time (`PersonGroup.of`), so danger rules stay
  age-correct as children grow.
- **Lesson reused:** the dropped BHW plan (`docs/planning`) stored full patient records. Here every stored field is optional and minimal.

## Demo AI bridge and strict guardrail (Oct 10, 2026, ~01:45)

- **Context:** the trained model (`waray-chat-v2-q8_0.gguf`, PR #7) was not delivered, and the app ran on fakes
  only. The feature freeze is 07:00.
- **Decision 1, Ollama on the laptop for the demo:** debug builds call `health-chat` in the laptop's Ollama through
  `adb reverse` (`ollama/README.md`). No internet is involved, but the model runs beside the phone, not on it; the
  pitch must say so. Release builds keep the fakes and no INTERNET permission. **Alternative rejected tonight:**
  llama.cpp on the phone (about 4–5 h of native work). It stays the target. The swap point is `LlmClient`.
- **Decision 2, the trained model is a laptop-side swap:** the app always asks for `health-chat`; replacing the
  Modelfile's `FROM` line plugs in any GGUF. The Modelfile pins Sailor2's default system prompt and greedy decoding
  because that is how the LoRA was trained and scored (verified: Ollama's chat prompt matches HF's template token for
  token on warm runs). The handoff's own SYSTEM prompt and `temperature 0.7` were not used: untested settings.
- **Decision 3, strict guardrail:** any medicine name, medicine form ("tablet", "syrup"), dose word or number+unit,
  named diagnosis, phone number, or (with a danger shown) downplaying phrase hides the whole reply. Streaming shows
  whole words only and holds back a trailing number until the next word is known, so nothing unsafe flashes on
  screen. Terms are a Kotlin object (`GuardrailTerms`) until `guardrail_terms.json` exists. **Evidence:** on the
  emulator, the stock model answered "what medicine and how many mg" with invented "10-20 mg/kg" child doses; the
  reply was withheld. **Open:** Run 03 was trained to name OTC medicines, so many of its replies will be withheld.
  Allowing OTC names without doses is an owner decision to make after measuring that.
- **Known limit:** the guardrail can't catch wrong advice that uses only ordinary words. The stock model told a
  Waray user to put a burned hand in hot water. The card above every reply is the safety net.
