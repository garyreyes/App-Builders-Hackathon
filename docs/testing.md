# Evaluation and device testing

## Text model quality

Keep test prompts separate from every training file. Start with the [Waray chat template](../evaluation/waray_chat_template.csv). A fluent Waray speaker is preferred for the eventual quality claim. For the owner-approved experimental prototype, AI-written prompts are allowed if the generating model and method are recorded and the prompts are independent of training data. Cover normal conversation, mixed languages, local terms, unclear questions, and different Waray varieties if relevant.

For each model version, save the exact prompt, actual reply, reviewer identity or AI model/method, and ratings for:

- Meaning and factual correctness.
- Naturalness of Waray.
- Whether it stayed in the requested language.
- Whether it admitted uncertainty when appropriate.

Compare the baseline and adapted model on the **same held-out prompts**. A lower training loss alone does not prove the app improved. AI ratings can guide a prototype comparison but do not establish native-speaker quality.

## Android acceptance check

Record the phone model, Android API level, CPU architecture, RAM, model file and hash, app version, context size, and test date. On the device:

1. Install/load the model and note load time and failures.
2. Put the phone in airplane mode and send Waray and mixed-language text prompts.
3. Record response time, visible memory pressure, crashes, and whether the phone becomes too hot for ordinary use.
4. Repeat after restarting the app; confirm it still works without any network connection.

The first target is a 6 GB phone, but API 28 compatibility must be checked separately on an API 28 phone or emulator. An emulator can catch install and API errors; only a real phone can establish usable speed and memory behavior.

## Speech input, later

Use consented Waray recordings with accurate transcripts. Evaluate the recognizer on speakers and recordings excluded from training. Record transcription mistakes separately from the chat model's answer mistakes. The app must show an editable transcript before sending it to chat.
