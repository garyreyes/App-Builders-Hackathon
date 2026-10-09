# Handoff for Antigravity

Updated 2026-10-10. This file is the durable summary of the Codex chat for another coding agent. Antigravity cannot read the chat automatically. Open this repository folder in Antigravity and ask it to read this file, `AGENTS.md`, `READMEDENZ.md`, `docs/SCOPE.md`, and `training/README.md` before changing anything.

In Antigravity 2.0, add this checkout folder to a Project and start the conversation in **Local Mode** so the agent reads the current local branch. Google's [Projects guide](https://www.antigravity.google/docs/projects/) describes local folders and worktree modes. The handoff prompt is at the end of this file.

## Where the work is

- Local checkout on this laptop: open the `App-Builders-Hackathon` folder already cloned in the workspace. Keep machine-specific paths out of the public repository.
- Current local branch: `codex/waray-gpu-readiness`. Run 04 changes are in [draft pull request #9](https://github.com/garyreyes/Buha.ai/pull/9). Earlier pull request #7 was merged before Run 04. Private CSVs, comparison files, and model weights remain on this laptop.
- Keep `origin/docs/planning` intact. It describes a different laptop-first health-visit scope. Current owner direction is an offline Android Waray assistant; the product use case is still unresolved between collaborators.

## Owner decisions from this chat

1. Prioritize **Waray**, then **Cebuano** and **Ilocano**.
2. Use **Sailor2-1B-Chat** as the candidate model and this laptop's GPU for model work. The final app should run fully offline on Android 9 / API 28 or newer, `arm64-v8a`, with a 6 GB RAM phone as the first device test.
3. Run the expensive work from a local CLI so Codex chat need not stay active or spend chat tokens during training. Keep data, logs, and weights out of public Git.
4. The owner has **no fluent Waray reviewer** and explicitly permits AI-generated and AI-reviewed data for an **experimental prototype**. Record the reviewing model/version and method honestly, for example in `reviewed_by`. Do not claim AI review is native-speaker review or that it proves natural Waray. Independent sources/models should check examples and baseline replies; Sailor2 should not approve its own outputs. Native-speaker validation remains a later quality gate for a delivered app.
5. Preserve source attribution, actual usage rights, privacy, and held-out train/test separation. Do not invent source or rights fields. Avoid spending money on external model APIs without owner authorization; local and public resources are preferred.

## What Codex completed

- Cloned the public repository and read its project instructions. Fixed stale README references after `README.md` was renamed to `READMEDENZ.md`.
- Detected an RTX 4050 Laptop GPU with 6,141 MiB VRAM, 16 GB RAM, and CUDA driver 13.4. Installed a Python 3.14 environment under ignored `.venv/` with CUDA PyTorch 2.11.0, Transformers 4.57.6, TRL 0.29.1, PEFT 0.21.2, datasets 4.4.1, and bitsandbytes 0.50.2.
- Verified PyTorch CUDA forward/backward computation and a bitsandbytes NF4 4-bit CUDA operation. These prove the local GPU software path, **not** that Sailor2 training fits or improves the model.
- Added `training/gpu_smoke.py` for untouched model inference and `training/local_cli.py` for reviewed-data checks, baseline replies, 4-bit LoRA training, and same-question comparison. `training/requirements-local.txt` pins the non-PyTorch dependencies. `training/README.md` contains the exact CLI commands.
- The original four data-separation/review-gate tests in `training/test_local_cli.py` passed. At that time the CLI had not completed an end-to-end Sailor2 run; later runs are recorded below.

## What Antigravity completed (2026-10-09 to 2026-10-10)

- Downloaded official `sail/Sailor2-1B-Chat` weights (revision `51b48ecd7c0629e4c79dc927a0445e6b671d8692`, ~1.98 GB safetensors).
- Ran `training/gpu_smoke.py`: GPU smoke test verified CUDA execution (5.75s, 1,916 MiB peak VRAM, `outputs/gpu-smoke.json`).
- Baseline evaluation: 21 of 25 held-out prompts failed (84% failure rate) due to severe base hallucinations and vocabulary errors (`outputs/waray-baseline.csv`).
- Pilot LoRA Run 01: 10-step memory/software compatibility verification (`outputs/waray-run-01/`).
- Full LoRA Run 02: 3 epochs on 70 train rows (`outputs/waray-run-02/`), reaching 32% pass rate and yielding `waray-chat-v1-q8_0.gguf`.
- **Medical OTC Adaptation (Run 03):**
  - Expanded dataset to 145 rows (120 train, 25 strictly held-out test) with dedicated frontline **non-prescription (OTC) medical guidance** (Paracetamol, Ibuprofen, ORS, Antacids, Cetirizine, RICE method), strict referral boundaries for prescription drugs, and technical medical terms retained in English per owner directive.
  - Adapted **all 7 linear projection layers** (`q_proj`, `k_proj`, `v_proj`, `o_proj`, `gate_proj`, `up_proj`, `down_proj`) for 4 epochs (60 optimizer steps) on the RTX 4050 GPU (2,272.5 MiB peak VRAM).
  - Train loss reached 1.260 (final step loss 0.889) with **81.7% token accuracy**.
  - Held-out 25-prompt test results (`outputs/waray-run-03/comparison.csv`):
    - **All-criteria pass rate:** **10 / 25 (40%)** (+150% over baseline, +25% over Run 02).
    - **Naturalness:** **18 / 25 (72%)** (doubled from baseline 36%).
    - **Language choice:** **24 / 25 (96%)** (clean Waray with English medical terminology).
    - **No invented facts:** **12 / 25 (48%)** (up from 28% baseline).
  - Merged weights to float16 (`models/waray-sailor2-1b-v2-merged/`).
  - Exported production GGUF models:
    - `models/waray-chat-v2-q8_0.gguf` (1,056,199,072 bytes / ~0.98 GiB, SHA-256: `8471fc7abaef03a47fa900b068e8bf7a92b5971b9b997e77e3ce56a07fb0d277`)
    - `models/waray-chat-v2-f16.gguf` (1,982,376,352 bytes / ~1.85 GiB, SHA-256: `fa4b993bce52fd797ddc8f7d560fd5452c521cf99bc9da432922b7fd4eea904e`)
  - Updated progress tracking in `docs/PROGRESS_TRACKING.md` and delivery note in `docs/handoff.md`.

## Codex Run 04 and combined desktop assistant (2026-10-10)

- Added 145 AI-drafted, source-grounded training pairs with `training/expand_waray_data.py`, for 265 train and 25 unchanged test rows. The exact private run snapshot is `data/private/waray-reviewed-run04.csv`, SHA-256 `7db34b96626e0d551a1ca32764ac5d13cc1d25874005b535c08c740983141a30`. The current private CSV only corrects source metadata; prompts and replies are identical. No test prompt was appended to training.
- Trained Run 04 on the RTX 4050 with LoRA rank 16/alpha 32, all seven projections, four epochs, learning rate `2e-4`, cosine decay and 5% warmup. Loss 0.594; peak allocated GPU memory 2,339.7 MiB; 1,420.5 seconds. Adapter and comparisons are in ignored `outputs/waray-run-04/`.
- Fixed attention masks in model generation. The **model alone** scored **12/25 (48%)** on the same 25 questions in `comparison-masked.csv`; it still confused several facts and one medical reply.
- Added `training/offline_knowledge.py`, a desktop reference layer of short source-grounded fact/policy rules with model fallback. The **combined desktop assistant** scored **24/25 (96%)** in `comparison-assisted.csv`: 22 rule replies and three model replies. The failed reply concerned fever/headache OTC guidance. These ratings were performed by Codex as an **AI author self-review**, not by a native Waray speaker or clinician. The known failure categories informed the rules, so this is a regression result, not a fresh generalization estimate.
- Merged and exported local `models/waray-chat-v3-q8_0.gguf` (1,056,199,072 bytes; SHA-256 `97e54275b4814fde219d224d2ffe1cdeafc04339564036dbcff31e42f371d6b0`). The GGUF alone scores 12/25; the Android runtime needs the equivalent rule layer to reproduce the combined result. See `docs/handoff.md` and `docs/PROGRESS_TRACKING.md`.

## What has not happened

- No Android device test has occurred (must test on an Android 9 / API 28 `arm64-v8a` device with 6 GB RAM in airplane mode).
- Native-speaker validation has not occurred. AI review is an experimental prototype indicator only.
- Waray speech-to-text remains a separate future track.
- The Run 04 GGUF and fact/policy layer have not been tested together on Android. The desktop test does not establish API 28 compatibility, offline speed, or safe clinical behavior.

## Next work for the next agent

1. Port `training/offline_knowledge.py` or its equivalent stable fact/policy rules into the Android local runtime, then load `waray-chat-v3-q8_0.gguf` on an API 28+ `arm64-v8a` phone in airplane mode.
2. Measure offline response latency and memory footprint on the 6 GB RAM test device.
3. Collect a fresh independent Waray test set, and arrange native Waray and clinician review before public or medical quality claims.
