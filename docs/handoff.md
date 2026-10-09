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

For each model version, record:

| Field | Value |
| --- | --- |
| File name and SHA-256 | TBD |
| Base model and license | TBD |
| Training or adaptation method | None / prompt / LoRA |
| Data source and permission | TBD |
| Quantization | TBD |
| Test phone and RAM | TBD |
| Waray test set version and results | TBD |
| Offline response time | TBD |

Do not assume Ollama runs inside the Android app. It can be used to test models on a computer; the phone app needs its own local inference runtime.
