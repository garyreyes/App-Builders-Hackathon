# Android ↔ model handoff

## Text chat (first milestone)

- **Input:** a UTF-8 text message, which may contain Waray, Filipino, English, or mixed language.
- **Output:** generated text for the chat screen.
- **Delivery:** a versioned, quantized `.gguf` model file plus its license and evaluation note.
- **Runtime:** local Android inference, such as the llama.cpp Android binding. The app must work without a network connection after model installation.
- **Supported phone baseline:** Android 9 / API 28 or newer, `arm64-v8a`; first performance target is a 6 GB RAM phone.
- **User control:** show errors when the model cannot load; keep a way to clear the conversation.

The current llama.cpp Android sample cannot be copied into an API 28 app unchanged: both its app and library modules declare `minSdk = 33`. The llama.cpp project documents an `android-28` NDK build for 64-bit ARM, so the Android team should adapt a compatible binding around that build and verify it on a real API 28 device. Do not lower `minSdk` alone and assume the sample is compatible.

The app team can build against a small placeholder GGUF model while the language team evaluates Waray quality. Replacing the file should not require redesigning the chat screen.

## Speech input (later milestone)

The Android app records a short utterance. A separate offline speech-to-text model produces a transcript. The app shows that transcript for correction, then sends the corrected text through the same chat input. Speech recognition and chat must be evaluated separately.

## Model delivery note

### Delivered model: `waray-chat-v2` (2026-10-10) — Medical OTC & Regional Assistant

| Field | Value |
| --- | --- |
| File name and SHA-256 | `waray-chat-v2-q8_0.gguf` (`8471fc7abaef03a47fa900b068e8bf7a92b5971b9b997e77e3ce56a07fb0d277`)<br>`waray-chat-v2-f16.gguf` (`fa4b993bce52fd797ddc8f7d560fd5452c521cf99bc9da432922b7fd4eea904e`) |
| Base model and license | [sail/Sailor2-1B-Chat](https://huggingface.co/sail/Sailor2-1B-Chat) (`51b48ecd7c0629e4c79dc927a0445e6b671d8692`), Apache 2.0 |
| Training or adaptation method | 4-bit LoRA (r=8, alpha=16, 4 epochs, 60 steps, train loss 1.260 / final step 0.889) targeting **all linear projection layers** (`q_proj`, `k_proj`, `v_proj`, `o_proj`, `gate_proj`, `up_proj`, `down_proj`) merged to float16 and exported via `llama.cpp` |
| Domain capabilities | **Frontline Medical Guidance (OTC ONLY):** Safe guidance for over-the-counter medicines (Paracetamol 500mg, Ibuprofen, ORS, Antacids, Cetirizine, RICE method) with technical terms kept in English; strict refusal of prescription drugs (antibiotics, hypertension maintenance); referral to Barangay Health Workers (BHW) or health centers; offline status honesty |
| Data source and permission | Prototype dataset (`data/private/waray-reviewed.csv`, SHA-256: `ec1508c1...`, 120 train rows, 25 held-out test rows, reviewed by `ai:gemini-3.8-flash; method=syntactic-and-lexical-cross-validation-waray; domain=medical-otc-and-general`); private local hackathon prototype per owner authorization |
| Quantization | `Q8_0` (1,056,199,072 bytes / ~0.98 GiB) and `F16` (1,982,376,352 bytes / ~1.85 GiB) |
| Test phone and RAM | Untested on device; compatibility target is Android 9 / API 28, `arm64-v8a`, 6 GB RAM phone |
| Waray test set version and results | 25 held-out test prompts (`outputs/waray-run-03/comparison.csv`): **10/25 (40%) all-criteria pass** (+6 over baseline), **18/25 (72%) naturalness** (doubled vs baseline), **24/25 (96%) Waray language choice**, **12/25 (48%) hallucination-free** |
| Offline response time | Desktop GPU generation average ~7.5s; on-device Android offline response time unmeasured |

### Previous delivered model: `waray-chat-v1` (2026-10-09)

| Field | Value |
| --- | --- |
| File name and SHA-256 | `waray-chat-v1-q8_0.gguf` (`5b1f4fb9abb108851bb932ec28c5b199339ad80c5769673b010f7e6fbf54e2b7`)<br>`waray-chat-v1-f16.gguf` (`42b61b910b5fcf5b23ac1667c3311e5f85790ebbe0a9c3a3767b8a31c06f04b8`) |
| Quantization | `Q8_0` (1,056,199,040 bytes / ~0.98 GiB) and `F16` (1,982,376,320 bytes / ~1.85 GiB) |
| Waray test set results | 8/25 (32%) all-criteria pass, 10/25 hallucination-free, 11/25 naturalness |

## Health guardrails (app side)

The app, not the model, owns safety: the keyword glossary decides topics and danger signs, danger warnings are pre-translated content, and every generated reply passes a deterministic guardrail (no doses, medicine names, or diagnoses) before display. The model is only asked to reply when a topic card matched, and the prompt includes that card. A delivered model that triggers the guardrail often on the held-out set is not an improvement.

The guardrail's word lists (drug names, dose units, diagnosis phrases, and "downplay" phrases such as "no need to go") live in the Health Library (`assets/content/guardrail_terms.json`), so the language collaborator can extend them in all 4 languages. The guardrail also blocks phone numbers in model replies. Numbers come only from content (911) or the user's saved health center.

## Prompt template (frozen for v1)

The app's prompt is **system prompt + matched card (English + user's language) + user text**. It contains **no patient context and no saved memory** in v1. "Who is this for?" and memory are used only by the app (triage, cards, banner). Train and evaluate the LoRA with this exact template. If v2 adds a person line to the prompt, both sides update this section first, then retrain with the new template. Teammate tasks for this change: [teammate-tasks.md](teammate-tasks.md).

Do not assume Ollama runs inside the Android app. It can be used to test models on a computer; the phone app needs its own local inference runtime.


