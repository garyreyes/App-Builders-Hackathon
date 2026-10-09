# Instructions for agents working in this repository

Read [README.md](README.md), [docs/architecture.md](docs/architecture.md), and [docs/decisions.md](docs/decisions.md) before changing the app or model workflow. This file is the project brief for any agent working with either collaborator. The repository documents decisions; the original chat history is not required to understand the project.

Read [docs/SCOPE.md](docs/SCOPE.md) before following other planning documents. The user confirmed Android and model training as the current build on 2026-10-09. The `docs/planning` branch has a laptop-only health-visit PRD with conflicting scope; do not silently substitute that scope. Preserve that collaborator's branch and ask the two teammates to reconcile product details that remain open.

## Goal and constraints

- Build a Waray assistant that runs fully offline on Android. Text chat is the first deliverable; Waray speech input follows as a separate component.
- The minimum Android version is Android 9 / API 28 on `arm64-v8a`. Test first on a 6 GB RAM phone. Do not claim performance or compatibility without measuring on the device.
- The current llama.cpp Android sample has `minSdk = 33`. Its Android binding must be adapted or replaced to support API 28; changing the number alone is insufficient. See [docs/handoff.md](docs/handoff.md).
- Sailor2-1B-Chat is the candidate baseline, not an approved final model. A third-party Q4_K_M GGUF is available for testing. See [docs/sailor2.md](docs/sailor2.md).

## Work boundaries

- Keep Android UI/runtime work separate from model and language work through the contract in [docs/handoff.md](docs/handoff.md).
- Start by measuring the untouched model against a held-out Waray test set. Only fine-tune when reviewed examples and baseline results justify it. Follow [training/README.md](training/README.md).
- Use native-speaker-reviewed Waray answers for supervised training. Preserve train/test separation, source attribution, and permissions. Sailor2's long generated Waray passages are not ready-made chat pairs.
- Speech recognition requires audio plus transcripts and its own evaluation. Chat fine-tuning does not improve transcription.
- Keep large model files, raw audio, private data, secrets, and machine-specific paths out of Git. Keep small manifests, scripts, documentation, and shareable reviewed data in Git only when rights permit.

## How to leave work for the next agent

- Update the relevant documentation when a decision or interface changes.
- Record model version, source, license, quantization, hash, test set version, phone details, memory, speed, and offline result for each delivered build.
- State what ran, what was measured, and what remains unverified. Never report a planned training run or phone test as completed.
- Use a branch and pull request for shared changes in the existing GitHub repository. See [docs/collaboration.md](docs/collaboration.md).
