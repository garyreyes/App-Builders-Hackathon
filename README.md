# Buha.ai

**An AI first-aid helper that runs entirely on the phone, in Waray, Bisaya, Tagalog, and English.**
For a parent in Samar or Leyte whose child is sick at night, during a typhoon, with no signal and the health
center far away. They type what is happening in their own language; the app answers right away, offline.

AppBuildersPH Hackathon 2026 · Team: [@garyreyes](https://github.com/garyreyes) (app) and
[@yezdenz](https://github.com/yezdenz) (model training).

## Why local AI

- **Offline is the whole point.** The moment someone needs this (typhoon, brownout, far sitio) is the moment the
  signal is gone. A cloud chatbot fails exactly then. This app passes the airplane-mode test.
- **Privacy.** A child's illness or a pregnancy is sensitive health data. Nothing leaves the phone: no account,
  no server, no analytics.

## What it does

1. **Instant danger check** (no AI, < 1 ms): keyword triage in 4 languages. Danger signs such as a seizure,
   blood in the stool, or trouble breathing show a red **"Go to the health center NOW · Call 911"** banner first.
2. **AI answer, on the device:** Gemma 4 E2B answers any health question in the user's chosen language, in a few
   short steps. Follow-up questions work ("What if it's a baby?").
3. **Grounded on checked first aid:** when the question matches a topic (child diarrhea, fever, burns), the model
   is given that topic's checked DOH/WHO steps and told not to contradict them. The checked card sits under the
   answer, one tap away.
4. **Safety guardrail on every reply:** any medicine name, dose, diagnosis, phone number, emergency number other
   than 911, or known first-aid myth (ice/butter/toothpaste on burns, "don't use water") hides the reply, and the
   checked card shows instead. Evidence for why: the untuned base model invented "10-20 mg/kg" child doses and
   told a user to put ice on a burn.

```
message ──► triage (keywords) ──► danger banner + topic card      (instant, no AI)
        └─► prompt = safety rules + checked card + user's language
            ──► Gemma 4 E2B on the phone (LiteRT-LM) ──► guardrail ──► reply, or the full card if blocked
```

## Run it

**Needs:** Android Studio (or JDK 17 + Android SDK 37), an arm64 Android phone (6 GB RAM suggested) **or** the
Android emulator (x86_64, 4 GB RAM), and about 3 GB free on the device.

```powershell
# 1. Build and install
cd android
./gradlew :app:installDebug

# 2. Put the model on the device (one time; the only step that needs internet is downloading this file)
#    https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm  ->  gemma-4-E2B-it.litertlm (2,588 MB, not gated)
adb shell mkdir -p /sdcard/Android/data/ph.appbuilders.offlinehealth/files
adb push gemma-4-E2B-it.litertlm /sdcard/Android/data/ph.appbuilders.offlinehealth/files/

# 3. Open the app, pick a language. The top bar shows "AI starting…", then "Offline" when the model is loaded.
```

Turn on airplane mode: everything keeps working.

**Faster on a laptop (optional):** debug builds without the model file use the laptop's GPU through Ollama
instead (`ollama create health-chat -f ollama/Modelfile` and `adb reverse tcp:11434 tcp:11434`). Still no internet.
See [ollama/README.md](ollama/README.md).

**Tests:** `cd android; ./gradlew :app:testDebugUnitTest` (57 unit tests: guardrail, triage, streaming, prompt).

## What runs where

| Part | Where | Internet? |
|---|---|---|
| Triage, danger banner, cards, guardrail | Phone (Kotlin) | Never |
| AI replies: Gemma 4 E2B via LiteRT-LM | Phone | Never |
| Optional laptop mode: Gemma 4 E2B via Ollama | Laptop, over USB/adb | Never |
| Model file | Downloaded once | Once, before use |

## Models, tools, and data (disclosures)

- **Gemma 4 E2B** (Google): the demo model. On-device file from `litert-community/gemma-4-E2B-it-litert-lm`;
  `gemma4:e2b` in Ollama for laptop mode.
- **Sailor2-1B-Chat** (Sea AI Lab), with a **LoRA fine-tune on Waray** by @yezdenz (code in [training/](training/README.md), results in
  [docs/PROGRESS_TRACKING.md](docs/PROGRESS_TRACKING.md): 10/25 vs 4/25 held-out Waray prompts, AI-reviewed). Gemma replaced it in the demo after a 10-question side-by-side test (see "How we chose the model").
- **Runtimes and frameworks:** Google LiteRT-LM 0.17.1 (Android), Ollama 0.40.1 (laptop), Kotlin 2.4,
  Jetpack Compose. Training: PyTorch, Transformers, PEFT, TRL, bitsandbytes, llama.cpp (GGUF export).
- **Content:** first-aid cards based on DOH/WHO guidance. Waray, Bisaya, and Tagalog text is AI-drafted and **not
  yet reviewed by a native speaker** (marked on each card).
- **Training data** ([credits](docs/DATASET_CREDITS.md)): AI-generated and AI-reviewed Waray pairs (honestly labeled), and
  [`ruslanmv/ai-medical-chatbot`](https://huggingface.co/datasets/ruslanmv/ai-medical-chatbot) for a separate
  medical experiment that is not in the app.
- **AI development tools:** Claude Code (Anthropic) for the app; the training track used OpenAI Codex and Google
  Antigravity ([handoff notes](docs/ANTIGRAVITY_HANDOFF.md)).
- **No cloud AI API is used anywhere** in the app.

## How we chose the model

Same 10 questions (4 languages; burns, diarrhea, fever, a danger case, 3 questions with no card, a medicine trap,
an emergency-number question), temperature 0:

| Setup | Result |
|---|---|
| Sailor2-1B, default prompt | Ice and "no water" for burns, invented child doses, wrong emergency numbers |
| Sailor2-1B + our prompt + checked card | Follows the burn card, but long, named medicines, invented remedies |
| **Gemma 4 E2B + our prompt + checked card** | **Short, safe, follows the card, no medicine, 911** (Waray weaker) |
| Qwen3 4B + our prompt + card | Leaked its reasoning into the reply, ~10 s |

Details: [docs/PROJECT_FACTS.md](docs/PROJECT_FACTS.md) and [docs/DECISIONS.md](docs/DECISIONS.md).

## Honest status

- Verified on the **Android emulator** (on-device Gemma, no laptop connection): correct burn and medicine-trap
  answers, Waray UI, danger banner. Emulator CPU replies take 35–70 s; **a real phone has not been tested yet**.
- Laptop mode (Ollama on the RTX 4050): replies in about 1 s.
- Gemma's Waray mixes in Bisaya/Tagalog; the trained Sailor2 Waray model can be swapped in for testing
  ([ollama/README.md](ollama/README.md)).
- Covered topics with checked cards: child diarrhea, fever, burns. Other questions get an AI answer with the same
  guardrail, and danger signs are always checked.
- Not a doctor and not a diagnosis tool.

## More

[PRD](docs/PRD.md) · [Architecture](docs/ARCHITECTURE.md) · [Decisions](docs/DECISIONS.md) ·
[Project facts](docs/PROJECT_FACTS.md) · [Running the AI](ollama/README.md) · [Training notes](READMEDENZ.md)
