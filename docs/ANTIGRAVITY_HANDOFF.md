# Handoff for Antigravity

Updated 2026-10-09. This file is the durable summary of the Codex chat for another coding agent. Antigravity cannot read the chat automatically. Open this repository folder in Antigravity and ask it to read this file, `AGENTS.md`, `READMEDENZ.md`, `docs/SCOPE.md`, and `training/README.md` before changing anything.

In Antigravity 2.0, add this checkout folder to a Project and start the conversation in **Local Mode** so the agent reads the current local branch. Google's [Projects guide](https://www.antigravity.google/docs/projects/) describes local folders and worktree modes. The handoff prompt is at the end of this file.

## Where the work is

- Local checkout on this laptop: open the `App-Builders-Hackathon` folder already cloned in the workspace. Keep machine-specific paths out of the public repository.
- Current local branch: `codex/waray-gpu-readiness`. The CLI commits are `e0984aa` and `5703760`; this handoff and AI-review correction are a subsequent change.
- The branch has **not been pushed to GitHub**. Opening the GitHub `main` branch on another computer will not show this work. On this laptop, open the current checkout. On another computer, first share/push this branch through the team's approved GitHub workflow, or transfer this checkout. Do not assume GitHub contains this chat or its latest files.
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
- Four data-separation/review-gate tests in `training/test_local_cli.py` pass. Python compilation and package dependency checks pass. The CLI had not completed an end-to-end Sailor2 model run.

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

## What has not happened

- No Android device test has occurred (must test on an Android 9 / API 28 `arm64-v8a` device with 6 GB RAM in airplane mode).
- Native-speaker validation has not occurred. AI review is an experimental prototype indicator only.
- Waray speech-to-text remains a separate future track.
- The local branch `codex/waray-gpu-readiness` has not been pushed to GitHub.

## Next work for the next agent

1. Test loading `waray-chat-v2-q8_0.gguf` on an Android phone (or API 28 arm64 emulator/device) via the adapted llama.cpp Android runtime in airplane mode.
2. Measure offline response latency and memory footprint on the 6 GB RAM test device.
3. Coordinate with a fluent Waray speaker for native human evaluation of model responses before public delivery.


