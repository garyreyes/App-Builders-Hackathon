# Current scope

**Resolved 2026-10-09 ~22:50 by the project owner: combined plan.** Build an **offline Android health helper**
(7 first-aid topics; Bisaya, Waray, Tagalog/Taglish, English; product scope in [`docs/PRD.md`](PRD.md)) on the
model track below: **Sailor2-1B-Chat GGUF on llama.cpp**, with **LoRA fine-tuning on reviewed Waray health chat pairs**.
Stack and safety design: [`docs/ARCHITECTURE.md`](ARCHITECTURE.md). Android 9 / API 28, `arm64-v8a`, and a 6 GB RAM
phone remain the first compatibility targets. The `docs/planning` branch (original BHW concept) is preserved, not current.

## What is decided

- The app and models run fully offline on the Android phone after installation.
- Text chat comes first; microphone input requires a separate offline speech recognizer.
- Sailor2-1B-Chat is a candidate baseline because it includes Waray and has a small GGUF quantization. It still needs native-speaker and phone testing.
- Fine-tuning uses reviewed Waray chat examples and a held-out test set. The large Sailor2 Waray text corpus is source material to review, not ready-made chat training data.

## What the teammates must align

- ~~Product use case~~ **Decided:** offline health helper (see PRD).
- Which teammate supplies Waray review and consented speech recordings.
- How the Android runtime will support API 28, since the current sample app requires API 33.
- **New (Oct 9, late):** Health Library + "Who is this for?" + optional on-phone memory. The app side is in
  [ARCHITECTURE.md](ARCHITECTURE.md). The language collaborator's tasks, open questions, and deferred items are in
  [teammate-tasks.md](teammate-tasks.md).

Do not claim a trained model, speech recognition, Android compatibility, or medical decision support until tested and documented.
