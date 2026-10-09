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

### Still unverified
- Gemma 4 E2B speed and memory on the **6 GB RAM demo phone** (load time, time to first token).
