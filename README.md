# Offline Health Helper

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
2. **AI answer, on the device:** the Waray-trained Sailor2 1B GGUF answers health questions in the user's chosen language, in a few
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
            ──► trained Sailor2 on the phone (llama.cpp) ──► guardrail ──► reply, or the full card if blocked
```

## Run it

### Fast path for judges

Download `OfflineHealth-debug.apk` and `waray-chat-v3-q8_0.gguf` from the
[Android judge demo release](https://github.com/garyreyes/Buha.ai/releases/tag/android-judge-demo-v1).
With an Android phone or emulator connected through ADB, run these commands from the download folder:

```powershell
adb install -r OfflineHealth-debug.apk
adb shell am start -n ph.appbuilders.offlinehealth/.MainActivity
adb shell mkdir -p /sdcard/Android/data/ph.appbuilders.offlinehealth/files
adb push waray-chat-v3-q8_0.gguf /sdcard/Android/data/ph.appbuilders.offlinehealth/files/
adb shell am force-stop ph.appbuilders.offlinehealth
adb shell am start -n ph.appbuilders.offlinehealth/.MainActivity
```

Choose **Winaray** on the first screen. After the model loads, the chat works in airplane mode. The GGUF is
1,056,199,072 bytes; SHA-256: `97e54275b4814fde219d224d2ffe1cdeafc04339564036dbcff31e42f371d6b0`.
The APK does not request Internet permission. If the model file is absent, the app opens in basic mode.

### Build from source

**Needs:** Android Studio (or JDK 17 + Android SDK 37, NDK 29.0.13113456, and CMake 3.31.6), an arm64 Android phone
(6 GB RAM suggested) **or** the Android emulator (x86_64, 4 GB RAM), and about 2 GB free on the device.

```powershell
# 1. Fetch the pinned llama.cpp source, build, and install
git -c core.longpaths=true submodule update --init --recursive
cd android
./gradlew :app:installDebug

# 2. Put the private trained model on the device once; the app does not download it.
adb shell mkdir -p /sdcard/Android/data/ph.appbuilders.offlinehealth/files
adb push ../models/waray-chat-v3-q8_0.gguf /sdcard/Android/data/ph.appbuilders.offlinehealth/files/

# 3. Open the app, pick a language. The top bar shows "AI starting…", then "Offline" when the model is loaded.
```

Turn on airplane mode: everything keeps working.

The APK has no Internet permission. If the GGUF is absent, the app uses its basic local mode. A Gemma LiteRT
file can also run locally as a fallback; the trained Sailor2 GGUF takes priority when both files are present.

**Tests:** `cd android; ./gradlew :app:testDebugUnitTest` (57 unit tests: guardrail, triage, streaming, prompt).

## What runs where

| Part | Where | Internet? |
|---|---|---|
| Triage, danger banner, cards, guardrail | Phone (Kotlin) | Never |
| AI replies: trained Sailor2 via llama.cpp | Phone | Never |
| Optional Gemma 4 E2B via LiteRT-LM | Phone | Never |
| Model file | Copied to phone once | Never at runtime |

## Models, tools, and data (disclosures)

- **Gemma 4 E2B** (Google): an optional on-device fallback when the trained Sailor2 GGUF is absent.
- **Sailor2-1B-Chat** (Sea AI Lab), with a **LoRA fine-tune on Waray** by @yezdenz (code in [training/](training/README.md), results in
  [docs/PROGRESS_TRACKING.md](docs/PROGRESS_TRACKING.md): see Run 04 results and limitations). The v3 Q8_0 GGUF is the current Android model.
- **Runtimes and frameworks:** llama.cpp at pinned commit `6184e92`, optional Google LiteRT-LM 0.17.1, Kotlin 2.4,
  Jetpack Compose. Training: PyTorch, Transformers, PEFT, TRL, bitsandbytes, llama.cpp (GGUF export).
- **Content:** prototype first-aid cards based on DOH/WHO guidance, with AI-drafted regional-language text.
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

- Verified on the **Android x86_64 emulator**, airplane mode on and no Internet permission: the v3 GGUF loaded and
  generated a Waray reply. One fever answer was withheld by the guardrail; a simple lexical reply was inaccurate.
  **A real phone has not been tested yet**, and the Android app has not reproduced the desktop 24/25 rule-assisted score.
- Covered topics with checked cards: child diarrhea, fever, burns. Other questions get an AI answer with the same
  guardrail, and danger signs are always checked.
- Not a doctor and not a diagnosis tool.

## More

[PRD](docs/PRD.md) · [Architecture](docs/ARCHITECTURE.md) · [Decisions](docs/DECISIONS.md) ·
[Project facts](docs/PROJECT_FACTS.md) · [Running the AI](ollama/README.md) · [Training notes](READMEDENZ.md)
