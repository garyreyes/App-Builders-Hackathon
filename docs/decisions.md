# Project decisions

| Decision | Reason | Status |
| --- | --- | --- |
| Fully offline Android app | User requirement; model and speech recognition run on the phone | Chosen |
| Android 9 / API 28 minimum, `arm64-v8a` | Extends the possible device range while matching llama.cpp's documented native Android 28 build | Chosen; app integration untested |
| 6 GB RAM phone as first target | User's representative midrange phone | Chosen; performance untested |
| Text chat before speech input | Lets each model's errors be measured separately | Chosen |
| Sailor2-1B-Chat as baseline | Small model with published Waray coverage and GGUF quantizations | Baseline measured on desktop GPU (2026-10-09): 21/25 failures across 25 held-out prompts; heavy factual hallucination and vocabulary confusion |
| LoRA on reviewed Waray chat pairs | Adapts chat behavior without training a new foundation model | Active prototype; Run 04 used 265 train rows and rank 16/alpha 32 on the RTX 4050, scored 12/25 model-only, and was exported as a local `Q8_0` GGUF. Native-speaker and phone testing pending. |
| Offline fact and policy layer before model fallback | Run 04's 1B LoRA model still confused stable geography and vocabulary and produced unsafe or irrelevant details | Desktop reference adopted 2026-10-10: source-grounded `training/offline_knowledge.py` scored 24/25 with the model as fallback in an AI self-review. This is a combined assistant result on known question categories; Android port, fresh benchmark, native review, and clinical review pending. |
| AI review for an experimental prototype | No fluent Waray reviewer is currently available; the owner asked for an AI-led path on 2026-10-09 | Active prototype; reviewing model and method recorded in data and reviews; native-speaker validation still required |
| Separate speech-to-text model | Text chat training cannot learn audio transcription | Planned |
| Keep model weights and raw/private data out of Git | Repository stays reviewable and respects sharing limits | Active |
| Android and model training are the current build direction | Project owner confirmed this on 2026-10-09 after reviewing the laptop-first PRD | Chosen; product use case still open |
| Preserve the `docs/planning` branch | It records the other collaborator's BHW prenatal-visit concept and differs from the current build direction | Active |

Revisit a decision when real device measurements or Waray speaker evaluation contradict it. Record the changed evidence and date here.

