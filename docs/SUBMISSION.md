# Judge handoff: offline Android demo

Use the [Android judge demo release](https://github.com/garyreyes/Buha.ai/releases/tag/android-judge-demo-v1)
for the APK and trained GGUF. The [README](../README.md#fast-path-for-judges) has the ADB commands to install,
copy the model, and open the app. The release assets are separate because the 1 GB model must stay out of Git.

**What to say:** Offline Health Helper is a local Android first-aid prototype. It checks danger signs with Kotlin,
shows first-aid cards, and runs a Waray-adapted Sailor2-1B model through llama.cpp inside the Android app. A
deterministic guardrail can hide unsafe model replies. There is no account, server, cloud AI call, or Internet
permission in the APK. The model file is copied to the device once before use.

**Five-minute demo:** Install the release APK and GGUF before presenting. Turn on airplane mode, open the app,
choose Winaray, and show the offline status. Ask about a child's fever or diarrhea: the instant card and danger
checks remain available even if the model answer is withheld. Show the model source in the app's debug menu and
try a short chat reply. Keep a screenshot or recording of the on-device run ready because emulator CPU generation
can be slow and the guardrail may withhold a reply. Do not describe the laptop as the inference engine.

**Evidence and limits:** On an Android x86_64 API 37 emulator with airplane mode on, the trained v3 GGUF loaded
and generated a Waray reply. The APK requested no Internet permission. One fever answer was withheld by the
guardrail; a simple vocabulary answer was inaccurate. The model-only desktop result was 12/25 on the held-out
Waray questions. The 24/25 desktop result included a separate rule layer that is not yet in Android. A physical
ARM64 phone, Android 9/API 28 runtime, and clinical review remain unverified. Present it as
a hackathon prototype, not a medical device or a clinically validated assistant.
