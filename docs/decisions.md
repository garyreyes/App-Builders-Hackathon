# Project decisions

| Decision | Reason | Status |
| --- | --- | --- |
| Fully offline Android app | User requirement; model and speech recognition run on the phone | Chosen |
| Android 9 / API 28 minimum, `arm64-v8a` | Extends the possible device range while matching llama.cpp's documented native Android 28 build | Chosen; app integration untested |
| 6 GB RAM phone as first target | User's representative midrange phone | Chosen; performance untested |
| Text chat before speech input | Lets each model's errors be measured separately | Chosen |
| Sailor2-1B-Chat as baseline | Small model with published Waray coverage and GGUF quantizations | Candidate; quality untested |
| LoRA on reviewed Waray chat pairs | Adapts chat behavior without training a new foundation model | Planned; no approved training set yet |
| Separate speech-to-text model | Text chat training cannot learn audio transcription | Planned |
| Keep model weights and raw/private data out of Git | Repository stays reviewable and respects sharing limits | Active |
| Android and model training are the current build direction | Project owner confirmed this on 2026-10-09 after reviewing the laptop-first PRD | Chosen; product use case still open |
| Preserve the `docs/planning` branch | It records the other collaborator's BHW prenatal-visit concept and differs from the current build direction | Active |

Revisit a decision when real device measurements or Waray speaker evaluation contradict it. Record the changed evidence and date here.
