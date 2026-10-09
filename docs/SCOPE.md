# Current scope and planning conflict

**Current direction, confirmed by the project owner on 2026-10-09:** build an offline **Android** app and prepare/train a Waray-capable model. Android 9 / API 28, `arm64-v8a`, and a 6 GB RAM phone are the first compatibility targets. The stronger second laptop will handle model testing and training.

The existing [`docs/planning` branch PRD](https://github.com/garyreyes/App-Builders-Hackathon/blob/docs/planning/docs/PRD.md) describes a different immediate plan: an offline **laptop** demo for Barangay Health Workers, with Android and fine-tuning deferred. That branch belongs to the other collaborator and must not be overwritten. The teammates should decide whether its prenatal health-visit use case is the Android app's use case; until then, the documents in this branch define the shared Android/model infrastructure without asserting a medical workflow.

## What is decided

- The app and models run fully offline on the Android phone after installation.
- Text chat comes first; microphone input requires a separate offline speech recognizer.
- Sailor2-1B-Chat is a candidate baseline because it includes Waray and has a small GGUF quantization. It still needs native-speaker and phone testing.
- Fine-tuning uses reviewed Waray chat examples and a held-out test set. The large Sailor2 Waray text corpus is source material to review, not ready-made chat training data.

## What the teammates must align

- Whether the Android app is a general Waray assistant, a BHW prenatal-visit tool, or both in stages.
- The exact screen/form fields and output format if it is a health-visit tool.
- Which teammate supplies Waray review and consented speech recordings.
- How the Android runtime will support API 28, since the current sample app requires API 33.

Do not claim a trained model, speech recognition, Android compatibility, or medical decision support until tested and documented.
