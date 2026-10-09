# Project Facts

Durable, verified facts. Each one was tested live, not taken from docs.

## On-device LLM spike: Oct 9, 2026, ~21:30–22:00

### Runtime
- **LiteRT-LM** (`com.google.ai.edge.litertlm:litertlm-android`) is Google's current on-device LLM runtime.
  The MediaPipe LLM Inference API is deprecated/maintenance-only on Android.
- Pinned **0.17.1**. 0.18.0 was published Oct 6, 2026, too new to trust tonight.
- **0.17.1 is compiled with Kotlin 2.4.** With AGP 9.3.1 (built-in Kotlin 2.2) the build fails with
  "binary version of its metadata is 2.4.0, expected 2.2.0". Fix: add `kotlin-gradle-plugin:2.4.0` to the root
  `buildscript` classpath and set the compose plugin to 2.4.0. See `spikes/android-llm/build.gradle.kts`.
- Spike APK builds (`spikes/android-llm`, ~60 MB debug). It has **no INTERNET permission**.
- Models are side-loaded to `/sdcard/Android/data/ph.hackathon.spike/files/`.

### Model files (Hugging Face, checked via API)
| Model | File | Size | Gated |
|---|---|---|---|
| Gemma 4 E2B | `litert-community/gemma-4-E2B-it-litert-lm/gemma-4-E2B-it.litertlm` | 2588 MB (GPU variant 2008 MB) | **No** |
| Gemma 3 1B | `litert-community/Gemma3-1B-IT/gemma3-1b-it-int4.litertlm` | 584 MB | Yes (auto-approve, needs HF login) |
| Qwen3 0.6B | `litert-community/Qwen3-0.6B/Qwen3-0.6B.litertlm` | 614 MB | No |
| Gemma 3n E2B | `google/gemma-3n-E2B-it-litert-lm` | 3656 MB | Yes (manual) |

### Language quality (laptop, same model families via Ollama, temperature 0–0.3)
Test: grounded answer from an English "child diarrhea" guide card, plus JSON classification
(topic + danger signs) of 6 messages in Bisaya/Waray/Taglish/English.

| Model | Grounded answer | Classification |
|---|---|---|
| qwen3:0.6b | ❌ Echoes the question back | not tested |
| gemma3:1b | ⚠️ Understood "diarrhea", but replied in **Tagalog** to Bisaya/Waray. Answered a toothache question with diarrhea advice | ❌ Wrong topics, invented danger signs |
| qwen3:1.7b | not tested | ⚠️ Topic 3/6, danger signs mostly invented |
| **gemma4:e2b** | ✅ Correct and grounded, but replies in **English**. Off-topic → "go to the health center" ✅ | ✅ Topic 5/6. **Danger signs 3/3 found, 0 invented.** Toothache → `none` ✅ |

**Conclusions**
1. ~1B models are not usable for native-language understanding or for danger detection.
2. **Gemma 4 E2B** is the minimum viable model. It understands Bisaya/Waray/Taglish input well enough to classify.
3. No small model writes Bisaya/Waray reliably. **Replies in the user's language must be pre-translated
   content** (team native speaker). The model understands; it doesn't write the local-language reply.
4. Its language label is unreliable (says "Tagalog" for Bisaya). Language comes from the user's setting.
5. Danger signs must also have a **deterministic keyword check** (glossary) alongside the model. The model
   found 3/3 here, but the target is 10/10.

### Sailor2-1B-Chat baseline (Oct 9, ~22:55, laptop via Ollama, `bartowski/Sailor2-1B-Chat-GGUF` Q4_K_M)
File: `Sailor2-1B-Chat-Q4_K_M.gguf`, 738,628,576 bytes, sha256 `782e8abed13d51a2083eadfb2f6d94c2cd77940532f612a99e6f6bec9b3501d4`,
repo commit `9f8154a0ffdf04bb7f29e4f6c3cb938b9178dba3`, Apache-2.0. Same two tests as above, untouched model.

| Test | Result |
|---|---|
| JSON classification (6 messages) | ❌ Topic "diarrhea" for 5/6 (incl. breathing, pregnancy bleeding, toothache). Danger lists are dumped glossary words, not ids. Language always "Tagalog" |
| Grounded answer, Bisaya | ⚠️ **Replies in Bisaya** (more natural than Gemma 4), but **invents ORS doses** ("30 ml/kg bawat 3-4 oras") and steps not in the card |
| Grounded answer, Waray / Taglish | ⚠️ Replies in **English**. Invents doses ("20-30 ml/kg") |
| Off-topic (toothache, Bisaya) | ❌ **Recommends ibuprofen, acetaminophen, and antibiotics.** Does not refuse |

**Conclusions → design (see DECISIONS.md):** glossary routing instead of model classification. A deterministic
guardrail on every reply. The model is only called when a topic card matched. The LoRA training set must teach:
reply in the user's language, stay inside the card, no doses/medicines, refuse off-topic. These outputs are
regression fixtures for `GuardrailTest`.

### Runtime decision changed (Oct 9, ~22:50)
Owner chose **llama.cpp + Sailor2 GGUF** (language collaborator's track) over LiteRT-LM + Gemma 4. The LiteRT-LM
facts above stay true and are kept as a record. Gemma 4 E2B also exists as GGUF, so it's a fallback on the same runtime.

### Still unverified
- llama.cpp Android build for **API 28 / arm64-v8a** (official sample is minSdk 33).
- Sailor2-1B (and the adapted model) speed and memory on the **6 GB demo phone**, in airplane mode.
- Whether LoRA on reviewed pairs fixes language choice, invented doses, and off-topic refusals.
