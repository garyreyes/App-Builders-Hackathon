# Ollama demo bridge

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

`ollama/Modelfile.sailor2` is the Sailor2 variant (ChatML template). To test the trained model:

1. Put `waray-chat-v2-q8_0.gguf` in this `ollama/` folder (`*.gguf` is gitignored; never commit it).
2. Check the file: `Get-FileHash ollama\waray-chat-v2-q8_0.gguf` must print
   `8471FC7ABAEF03A47FA900B068E8BF7A92B5971B9B997E77E3CE56A07FB0D277`.
3. In `Modelfile.sailor2`, change only the `FROM` line to `FROM ./waray-chat-v2-q8_0.gguf`.
4. `ollama create health-chat -f ollama/Modelfile.sailor2`, then send messages in the app. No app change.
5. Switch back any time with `ollama create health-chat -f ollama/Modelfile`.

Training used no system prompt, so the trained model under the app's prompt is untested. Before choosing it for
the demo, run the same 10-question comparison and keep it only if it beats Gemma there. Its OTC-medicine habit
will make the strict guardrail hide many replies.
