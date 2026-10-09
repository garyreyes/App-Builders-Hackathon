# Offline Waray health helper

A two-person project: an offline Android health helper (7 first-aid topics; Waray, Bisaya, Tagalog/Taglish, English) with text chat first and speech input later. The first milestone is text chat that works with airplane mode on. Speech input comes after that works on the target phone.

This project uses the existing public repository [garyreyes/App-Builders-Hackathon](https://github.com/garyreyes/App-Builders-Hackathon). The current owner direction is the **combined plan**: health-helper scope ([PRD](docs/PRD.md)) + Sailor2-1B GGUF on llama.cpp + LoRA on reviewed Waray health chat pairs. See [scope](docs/SCOPE.md).

**Android target:** Android 9 / API 28 minimum, 64-bit ARM (`arm64-v8a`), with 6 GB RAM as the first test device. This is a chosen compatibility target, not a claim that every Android 9 phone will have enough memory or acceptable speed.

## Start here

- [Agent instructions](AGENTS.md) — project context for either collaborator's coding agent.
- [Scope and branch status](docs/SCOPE.md) — which plan is current and which planning document conflicts.
- [Architecture](docs/ARCHITECTURE.md) and [app/model handoff](docs/handoff.md) — what the Android app and local models must exchange.
- [Collaboration](docs/collaboration.md) — GitHub branches, reviews, file sharing, and a prompt for the Android collaborator's agent.
- [Training workflow](training/README.md), [Sailor2 sources](docs/sailor2.md), and [testing](docs/testing.md) — how to build and judge the Waray model.
- [Other laptop setup](docs/workstations.md) and [decisions](docs/DECISIONS.md) — where heavier model work runs and why current choices were made.

## Who does what

| Model and language work | Android app work |
| --- | --- |
| Collect permitted Waray examples and have speakers check them. | Build the chat screen and local model loader. |
| Test a small existing model, then fine-tune only if tests show a need. | Load a GGUF model from phone storage and show its replies. |
| Deliver a versioned GGUF file and test results. | Later, record speech and show an editable transcript before sending it to chat. |

See [the handoff contract](docs/handoff.md) for what both sides must agree on.

## Milestones

1. **Text baseline:** choose a small, licensed chat model; run it on a computer and on the target Android phone. Record model version, phone RAM, response time, and whether it works in airplane mode.
2. **Waray evaluation:** make a held-out set of questions and expected behavior. AI review is allowed for an experimental prototype when labeled; native-speaker review is needed for a delivered quality claim. Keep test examples separate from training examples and record model replies and reviewer judgments.
3. **Improve the chat model:** use [the training workflow](training/README.md). Fine-tune Sailor2-1B-Chat with LoRA only after collecting reviewed Waray chat pairs. An explicitly labeled AI-reviewed set is allowed for an experimental prototype; do not present it as native-speaker validation. Deliver and retest a compressed GGUF model on the phone.
4. **Speech input:** collect or license Waray audio with accurate transcripts. Test an offline speech recognizer on the phone. Let users correct its transcript before the chat model receives it.

## Working together

- Keep code, documentation, and small data manifests in this Git repository. Create one branch per change and review each other's pull requests.
- Keep model weights, raw audio, and private or restricted datasets out of Git. Share only files that both collaborators are allowed to access and use.
- Give each delivered model a version (for example, `waray-chat-v1.gguf`) and a short note with its base model, license, data version, and test results.
- Do not publish recordings, private messages, or other people's writing without the needed permission.

## Current status

- Draft project structure and app/model handoff are documented for both collaborators to review.
- Target phone class: API 28+, 64-bit ARM, 6 GB RAM for the first test.
- Sailor2 Waray dataset and 1B chat model located; see [source notes](docs/sailor2.md).
- The first PC check found a GTX 1050 Ti with 4 GB video memory. The training laptop has an RTX 4050 with 6 GB; see [workstation notes](docs/workstations.md). The Waray LoRA (Run 03) is trained and exported to GGUF; results are in [progress tracking](docs/PROGRESS_TRACKING.md).
- Product use case decided: offline health helper ([PRD](docs/PRD.md)). Sailor2-1B baseline tested on the laptop; results in [PROJECT_FACTS](docs/PROJECT_FACTS.md). The demo app uses Gemma 4 E2B after a side-by-side test; the trained Waray model can be swapped in with `ollama/Modelfile.sailor2` (see [ollama/README.md](ollama/README.md)).

Dataset handoff: add the file to a private location and tell the model collaborator its path, or attach it in the project chat. CSV, JSON, XLSX, TXT, and audio with transcripts can all be inspected. Include where it came from and what permission you have to use it. We will normalize its format after seeing the real data.
