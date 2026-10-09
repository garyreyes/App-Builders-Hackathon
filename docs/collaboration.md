# Collaboration and GitHub workflow

## Shared repository

Both collaborators use the existing public [garyreyes/App-Builders-Hackathon](https://github.com/garyreyes/App-Builders-Hackathon) repository. Clone it, or pull the latest changes if it is already on your computer. The repository owner must give each collaborator write access before they can push their own branches. Do not paste credentials into files or chats.

The Android collaborator owns the app and local runtime; the language collaborator owns data preparation, model evaluation, and model delivery. Both review changes to the interface in [handoff.md](handoff.md). Read [SCOPE.md](SCOPE.md) and [PRD.md](PRD.md) before starting. The `docs/planning` branch is an older concept; keep it intact.

## Daily workflow

1. Open a small GitHub issue describing one concrete result.
2. Pull `main`, then create a branch for that issue, such as `codex/android-chat` or `codex/waray-evaluation`.
3. Commit changes and open a pull request. The other collaborator reviews the changed files and runs the relevant check before merging.
4. Record the result, limitations, and next task in the pull request or a short note under `docs/`.

Do not commit `.gguf` weights, raw recordings, private data, API keys, or whole Sailor2 corpora. The `.gitignore` excludes common large model/audio formats and `data/raw/` and `data/private/`. Share an allowed model file separately and put its URL or location, license, version, SHA-256, and test report in a small Git-tracked manifest. Data that cannot legally be shared stays with its authorized holder. This repository is public, so review any small examples for permission and personal information before committing them.

## For the friend's agent

Give the agent this repository and the following task prompt, adjusting the final sentence to the actual assignment:

> Read `AGENTS.md`, `README.md`, `docs/SCOPE.md`, `docs/ARCHITECTURE.md`, and `docs/handoff.md`. The current build direction is a fully offline Android health helper (7 first-aid topics, 4 languages; see `docs/PRD.md`) for API 28+ on 64-bit ARM, with a 6 GB phone as the first test target. Sailor2-1B-Chat GGUF on llama.cpp is the model; LoRA on reviewed Waray health chat pairs. The `docs/planning` branch is an older concept; preserve it. Work on the Android app and local model runtime. Measure compatibility on a real device, and document findings. Your current assignment is: [specific task].

The agent should inspect the actual code and device before claiming a feature works. The written project brief is enough to start even if it cannot see this chat.
