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

## Plug in the trained model

The app always asks for `health-chat`, so swapping the model never touches the app.

1. Put `waray-chat-v2-q8_0.gguf` in this `ollama/` folder (`*.gguf` is gitignored; never commit it).
2. Check the file: `Get-FileHash ollama\waray-chat-v2-q8_0.gguf` must print
   `8471FC7ABAEF03A47FA900B068E8BF7A92B5971B9B997E77E3CE56A07FB0D277`.
3. In `Modelfile`, change only the `FROM` line to `FROM ./waray-chat-v2-q8_0.gguf`.
4. `ollama create health-chat -f ollama/Modelfile`, then send a message in the app.

Keep the Modelfile's `TEMPLATE`, `SYSTEM`, and `temperature 0` as they are: training sent no system message (so
Sailor2's default system prompt was used) and the held-out scores were measured with greedy decoding. A different
system prompt or temperature is an untested model. The handoff's suggested Modelfile changes both.

After the swap, run about 10 demo questions and count how often the reply is withheld: the trained model was taught
to name OTC medicines, which the strict guardrail hides. Changing that is an owner decision (`docs/DECISIONS.md`).
