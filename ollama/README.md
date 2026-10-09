# Running the AI

## On the phone (the real product: no laptop, airplane mode)

The app runs **Gemma 4 E2B on the phone itself** with Google LiteRT-LM when the model file is in its storage.
Nothing else is needed: no Ollama, no cable, no internet. Any build (debug or release) uses it.

```powershell
# Phone connected (USB, or Wireless debugging: adb pair <ip:port> <code>, then adb connect <ip:port>)
cd android; ./gradlew :app:installDebug
adb shell mkdir -p /sdcard/Android/data/ph.appbuilders.offlinehealth/files
adb push ..\models\gemma-4-E2B-it.litertlm /sdcard/Android/data/ph.appbuilders.offlinehealth/files/
```

The model file is `litert-community/gemma-4-E2B-it-litert-lm/gemma-4-E2B-it.litertlm` on Hugging Face (2,588 MB,
not gated). Restart the app: the top bar shows **AI starting…** while the model loads (about 25 s on the emulator),
then **Offline** with the AI ready. If the file is missing, debug builds fall back to the Ollama bridge below.

Measured on the emulator (x86_64, 4 GB RAM, CPU only): replies take 35-70 s. A real phone's GPU is expected to be
much faster; not measured yet.

# Ollama demo bridge (laptop)

Debug builds of the app get their AI replies from **Ollama on the laptop**, over the USB cable. No internet is
used: the phone can stay in airplane mode. Release builds don't include this (no INTERNET permission, per the PRD).
The model running on the phone itself (llama.cpp) is the next step; this bridge is for the demo.

Every reply still goes through the app's guardrail (`domain/Guardrail.kt`): any medicine name, dose, diagnosis,
or phone number hides the whole reply, and the topic card stays on screen.

## Run it

```powershell
# 1. Create the model the app calls (name: health-chat). Re-run after editing the Modelfile.
ollama create health-chat -f ollama/Modelfile

# 2. Phone or emulator connected by USB: let it reach the laptop's Ollama at 127.0.0.1:11434.
adb reverse tcp:11434 tcp:11434

# 3. Install and open the debug build.
cd android; ./gradlew :app:installDebug
```

The top bar shows **AI ready** when the app reached Ollama, or **Basic mode** when it didn't (cards still work).
After fixing the connection, send another message (or use the debug menu's "reconnect to Ollama").
`adb reverse` is lost when the cable is unplugged or adb restarts: run step 2 again.

## Which model

`ollama/Modelfile` (the default) is **Gemma 4 E2B** (`gemma4:e2b`, already pulled). It was chosen on Oct 10 after a
10-question comparison of four setups (docs/PROJECT_FACTS.md): with the app's prompt and the checked card it gives
short, safe answers that follow the card. Its Waray is weaker than Sailor2's (it mixes in Bisaya/Tagalog).

The app sends the system prompt itself (`HealthPrompt.kt`: safety rules + the matched card's checked steps + the
user's language) and turns thinking off, so the Modelfile's own `SYSTEM` is only a fallback.

## Try the trained Waray model

`ollama/Modelfile.sailor2` loads the local Run 04 v3 GGUF from `models/` and uses Sailor2's ChatML template.
The GGUF is local and Git-ignored. Verify its SHA-256 before creating a separate Ollama model:

```powershell
Get-FileHash models\waray-chat-v3-q8_0.gguf -Algorithm SHA256
# Expected: 97E54275B4814FDE219D224D2FFE1CDEAFC04339564036DBCFF31E42F371D6B0
ollama create health-chat-v3 -f ollama/Modelfile.sailor2
adb reverse tcp:11434 tcp:11434
cd android
.\gradlew.bat :app:installDebug -PpreferOllama=true -PollamaModel=health-chat-v3
```

Open the debug app and choose Waray. The debug menu identifies the source as `laptop via Ollama
(health-chat-v3)`. The two Gradle properties affect only the debug build: `preferOllama` selects the
laptop even if the Gemma file is on the phone, and `ollamaModel` selects the local Ollama model name.
Build without those properties to restore the usual phone-Gemma-first behavior. This bridge runs the
model on the laptop over adb; it is not an on-phone GGUF runtime or a phone airplane-mode test.

Run 04's model-only result was 12/25 on the known Waray set. The 24/25 combined desktop result also
used `training/offline_knowledge.py`, which has not been ported to Android. The app's prompt and strict
medical guardrail can change or hide the model's replies. Compare the same health questions against
Gemma before choosing a demo default; keep native Waray and clinical review pending.
In a local v3 Ollama smoke test, an unguarded burn reply suggested an unsuitable remedy and an OTC drug
despite the system prompt. An initial app run also showed an incorrect burn-cooling duration. The debug
v3 path now holds the whole reply until the guardrail checks it; burn replies with a duration other than
the checked card's 20 minutes or with salt, milk, or hot water are withheld. This is a narrow safety check,
not clinical validation. The checked card remains available when a reply is withheld.
