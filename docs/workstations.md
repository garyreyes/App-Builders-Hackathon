# Working from another laptop

## High-spec laptop for model work

1. Install Git and clone [the shared repository](https://github.com/garyreyes/App-Builders-Hackathon) with `git clone https://github.com/garyreyes/App-Builders-Hackathon.git`. Open the cloned folder as the project workspace.
2. Read `AGENTS.md`, `training/README.md`, and `docs/testing.md`.
3. Check the laptop's GPU model and VRAM before selecting LoRA settings. Install a supported Python, PyTorch/CUDA, Transformers, TRL, PEFT, and datasets environment there when training data is ready. Record versions and commands in the training run notes.
4. Keep the raw dataset and model downloads outside Git. Save reproducible scripts, configuration, dataset manifests, evaluation results, and model hashes in the repository when sharing rights allow.
5. Run the untouched baseline and independent evaluation before any fine-tuning. The owner permits clearly labeled AI review for a prototype; native-speaker quality remains unverified. The current project has no completed training run.

## GPU checked on 2026-10-09

This laptop has an NVIDIA GeForce RTX 4050 Laptop GPU with 6,141 MiB of VRAM, 16 GB of system RAM, and about 95 GB of free disk space at the time of the check. The CUDA driver reports version 13.4. These numbers make a small 1B LoRA experiment plausible, but actual training memory use and speed still need measurement. Use short sequences and batches of one when an approved training set is ready. The 8B and 20B Sailor2 models are not local training targets on this GPU.

A local Python 3.14 environment now has PyTorch 2.11.0+cu128, Transformers 4.57.6, and Accelerate 1.15.0. PyTorch detected the RTX 4050 and completed a CUDA forward/backward tensor check (32.3 MiB peak allocated in that check). `training/gpu_smoke.py` passed syntax compilation, but its Sailor2 inference check did not finish: the official 1B model weighs about 1.98 GB, and the download was stopped after progress remained too slow. No Sailor2 weights were loaded, no model evaluation was completed, and no fine-tuning was started.

GitHub synchronizes committed project files between laptops. A `.gitignore`d dataset or model file will **not** arrive with `git clone`; transfer those through a permitted file location and verify the hash.

## This Codex chat

GitHub does not contain this conversation. To view or continue a local Codex chat from another laptop, use the same ChatGPT account and workspace and connect the laptops through the ChatGPT desktop app's remote connections if that feature is available to you. The current host must be awake and online for remote access. In the desktop app, look under **Settings → Connections** for **Control this PC** on the current laptop and **Control other devices** on the other laptop.

To move this chat's execution to the faster laptop, first save this Git repository as a project on both hosts, then use the chat's run-location selector to hand off to the destination host. The official [remote connections guide](https://learn.chatgpt.com/docs/remote-connections) describes matching projects and host handoff. Availability varies by rollout. If remote handoff is unavailable, clone the repo on the faster laptop and start a new chat there; `AGENTS.md` and these docs carry the project context.
