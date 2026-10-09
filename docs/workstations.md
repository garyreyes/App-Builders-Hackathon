# Working from another laptop

## High-spec laptop for model work

1. Install Git and clone [the shared repository](https://github.com/garyreyes/App-Builders-Hackathon) with `git clone https://github.com/garyreyes/App-Builders-Hackathon.git`. Open the cloned folder as the project workspace.
2. Read `AGENTS.md`, `training/README.md`, and `docs/testing.md`.
3. Check the laptop's GPU model and VRAM before selecting LoRA settings. Install a supported Python, PyTorch/CUDA, Transformers, TRL, PEFT, and datasets environment there when training data is ready. Record versions and commands in the training run notes.
4. Keep the raw dataset and model downloads outside Git. Save reproducible scripts, configuration, dataset manifests, evaluation results, and model hashes in the repository when sharing rights allow.
5. Run the untouched baseline and speaker-reviewed evaluation before any fine-tuning. The current project has no completed training run.

GitHub synchronizes committed project files between laptops. A `.gitignore`d dataset or model file will **not** arrive with `git clone`; transfer those through a permitted file location and verify the hash.

## This Codex chat

GitHub does not contain this conversation. To view or continue a local Codex chat from another laptop, use the same ChatGPT account and workspace and connect the laptops through the ChatGPT desktop app's remote connections if that feature is available to you. The current host must be awake and online for remote access. In the desktop app, look under **Settings → Connections** for **Control this PC** on the current laptop and **Control other devices** on the other laptop.

To move this chat's execution to the faster laptop, first save this Git repository as a project on both hosts, then use the chat's run-location selector to hand off to the destination host. The official [remote connections guide](https://learn.chatgpt.com/docs/remote-connections) describes matching projects and host handoff. Availability varies by rollout. If remote handoff is unavailable, clone the repo on the faster laptop and start a new chat there; `AGENTS.md` and these docs carry the project context.
