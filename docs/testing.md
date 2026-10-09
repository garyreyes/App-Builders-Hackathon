# Evaluation and device testing

## Text model quality

Keep test prompts separate from every training file. Start with the [Waray chat template](../evaluation/waray_chat_template.csv). A fluent Waray speaker is preferred for the eventual quality claim. For the owner-approved experimental prototype, AI-written prompts are allowed if the generating model and method are recorded and the prompts are independent of training data. Cover normal conversation, mixed languages, local terms, unclear questions, and different Waray varieties if relevant.

For each model version, save the exact prompt, actual reply, reviewer identity or AI model/method, and ratings for:

- Meaning and factual correctness.
- Naturalness of Waray.
- Whether it stayed in the requested language.
- Whether it admitted uncertainty when appropriate.

Compare the baseline and adapted model on the **same held-out prompts**. A lower training loss alone does not prove the app improved. AI ratings can guide a prototype comparison but do not establish native-speaker quality.

The held-out results for `waray-chat-v1` (evaluated 2026-10-09) are documented in [`PROGRESS_TRACKING.md`](PROGRESS_TRACKING.md) and [`outputs/waray-run-02/comparison.csv`](../outputs/waray-run-02/comparison.csv), achieving an 8/25 (32%) all-criteria pass rate compared to 4/25 (16%) for the untouched Sailor2 baseline.

## Android acceptance check

Record the phone model, Android API level, CPU architecture, RAM, model file and hash, app version, context size, and test date. On the device:

1. **Side-load the delivered GGUF model:**
   ```powershell
   adb push models/waray-chat-v1-q8_0.gguf /sdcard/Download/waray-chat-v1-q8_0.gguf
   ```
2. Verify the SHA-256 hash matches `5b1f4fb9abb108851bb932ec28c5b199339ad80c5769673b010f7e6fbf54e2b7`.
3. Put the phone into **airplane mode**.
4. Open the app, load the model, and record:
   - Cold load latency (ms)
   - Free RAM before and during model load
   - Memory pressure warnings or crashes
5. Test generation with held-out prompts (e.g. `Maupay nga aga ha imo!`) and record:
   - First-token latency (ms)
   - Tokens per second
   - Peak memory consumption
   - Thermal stability during repeated turns

The first target is a 6 GB phone, but API 28 compatibility must be checked separately on an API 28 phone or emulator. An emulator can catch install and API errors; only a real phone can establish usable speed and memory behavior.

## Speech input, later

Use consented Waray recordings with accurate transcripts. Evaluate the recognizer on speakers and recordings excluded from training. Record transcription mistakes separately from the chat model's answer mistakes. The app must show an editable transcript before sending it to chat.
