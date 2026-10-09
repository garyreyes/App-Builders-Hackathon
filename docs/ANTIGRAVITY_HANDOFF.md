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
- Four data-separation/review-gate tests in `training/test_local_cli.py` pass. Python compilation and package dependency checks pass. The CLI has **not** completed an end-to-end Sailor2 model run.
- Sailor2 publishes pretraining and supervised data. The stage-two SFT dataset viewer listed about 1,200 Waray, 1,198 Cebuano, and 1,131 Ilocano examples at inspection time. Do not assume those examples are independent of Sailor2's original training or suitable for evaluating improvement. The large Waray synthetic corpus is long passages, not chat pairs. See `docs/sailor2.md`.

## What has not happened

- No reviewed Waray train/test CSV exists in the repo. `training/reviewed_chat_template.csv` is header-only.
- No Sailor2 model has finished downloading or loading on this laptop. The official 1B weight file is about 1.98 GB; download attempts were stopped because transfer was unusually slow. A small ignored partial download may remain. Resume or retry when practical.
- No Sailor2 baseline, LoRA training, adapted-model comparison, GGUF export, Android phone test, or Waray speech model has been completed. Do not report any of these as done.
- The local branch has not been pushed or made into a pull request. The repo's normal shared-work process is branch plus PR.

## Next work for Antigravity

1. Read the files named at the top and inspect the code before editing. Respect the owner's AI-reviewed-prototype decision, which supersedes earlier human-only wording elsewhere in the repo.
2. Build an **AI-reviewed prototype CSV** in the template format. Use permitted sources. Label AI review with the actual model/version and method. Keep it under ignored `data/private/` unless sharing rights clearly allow publication. Create at least 20 distinct held-out test questions before training examples; do not reuse Sailor2 training examples as proof of improvement.
3. Run `training/local_cli.py check`, then baseline inference when model weights are available. Independently rate every baseline reply, documenting whether the reviewer is AI. Run `train` only if baseline failures justify an experiment. Use `--max-steps 10` for a first memory/compatibility pilot. The CLI saves its progress and run manifest under `outputs/`.
4. Inspect failures and compare the adapted model on the same held-out questions. AI judging is a prototype signal; report its limits. Arrange native-speaker review later if the model is to be described as Waray-validated.
5. Coordinate with the Android collaborator before changing the app/model handoff. A successful desktop adapter is not an Android-ready GGUF; export, quantization, hashes, and real offline phone measurements are still separate work.

## Short prompt to paste into Antigravity

> Open this local `App-Builders-Hackathon` checkout on branch `codex/waray-gpu-readiness`. Read `docs/ANTIGRAVITY_HANDOFF.md`, `AGENTS.md`, `READMEDENZ.md`, `docs/SCOPE.md`, and `training/README.md`. Continue the Waray-first Sailor2-1B-Chat model work using this laptop's RTX 4050. The owner permits an explicitly labeled AI-reviewed prototype because no fluent Waray reviewer is available; never label AI review as native-speaker review. Keep test prompts separate, record real source/rights and reviewer model/method, and do not claim training or Android compatibility until measured. Inspect the local CLI, prepare permitted prototype data, complete baseline and a small GPU pilot when feasible, and document results. Preserve the other collaborator's `docs/planning` branch and do not publish private data or weights.
