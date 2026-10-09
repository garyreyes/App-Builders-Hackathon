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
