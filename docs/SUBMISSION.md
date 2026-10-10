# Submission kit

Prepared text for the submission form, the demo script, and Q&A answers. Both teammates read the form text
before submitting (one submission only, no edits).

## Form fields

**Project name:** Offline Health Helper

**Short description:**
An AI first-aid helper that runs on-device on Android in Waray, Bisaya, Tagalog, and English. A parent whose child
is sick during a typhoon, with no signal, types what is happening in their own language. The app instantly flags
danger signs ("Go to the health center NOW · Call 911") and Gemma 4 E2B, running on the device, answers with short
steps grounded on checked DOH/WHO first-aid cards. A safety guardrail hides any reply with a medicine, dose,
diagnosis, or known first-aid myth.

**Why does this product benefit from running AI locally?**
Offline and privacy are both must-haves. The moment a family needs first-aid help (typhoon, brownout, a far
sitio) is exactly when the signal is gone, so a cloud chatbot fails when it matters most; ours passes the
airplane-mode test. And a child's illness or a pregnancy is sensitive health data: nothing leaves the phone, with
no account, server, or analytics.

**What runs locally:** everything. Keyword triage and danger signs, the first-aid cards, the safety guardrail, and
the AI model (Gemma 4 E2B via Google LiteRT-LM on the Android device, verified on the Android emulator, not yet on a
physical phone; optional laptop mode runs the same model in Ollama on a local laptop, used for the live demo because
it is faster).

**What requires internet:** only downloading the model file once before first use.

**Models used:** Gemma 4 E2B (Google; LiteRT-LM `.litertlm` on device, `gemma4:e2b` in Ollama). Sailor2-1B-Chat
(Sea AI Lab): the base model was compared against Gemma and lost; our teammate trained a Waray LoRA fine-tune of
it (4/25 → 10/25 held-out Waray prompts, AI-reviewed), which is not in the app yet.

**Technologies & frameworks:** Kotlin, Jetpack Compose, Google LiteRT-LM 0.17.1, Ollama 0.40.1, JUnit.
Training: PyTorch, Hugging Face Transformers, PEFT, TRL, bitsandbytes, llama.cpp (GGUF export).

**APIs & cloud services:** none. No cloud AI API anywhere in the product.

**Existing code & assets:** open-source libraries above; first-aid guidance based on DOH/WHO materials; Hugging Face
dataset `ruslanmv/ai-medical-chatbot` used only in a separate training experiment (credited in docs/DATASET_CREDITS.md). All app code
was written during the hackathon (see commit history).

**AI development tools:** Claude Code (Anthropic) for the Android app; OpenAI Codex and Google Antigravity for the
training scripts (docs/ANTIGRAVITY_HANDOFF.md).

## Demo plan (5 minutes)

**Which AI mode to show.** On the emulator, the on-device model takes 35–70 s per reply (CPU only), too slow for
stage. Use **laptop mode** live (Gemma 4 on the laptop GPU, about 1 s once loaded, still fully offline) and **say
out loud that the live demo runs the model on the laptop, not the phone**; show the
**on-device run in the video**. To switch the emulator to laptop mode, hide the model file; to switch back, restore it:

```powershell
adb shell mv /sdcard/Android/data/ph.appbuilders.offlinehealth/files/gemma-4-E2B-it.litertlm /sdcard/Android/data/ph.appbuilders.offlinehealth/files/gemma.off
adb shell mv /sdcard/Android/data/ph.appbuilders.offlinehealth/files/gemma.off /sdcard/Android/data/ph.appbuilders.offlinehealth/files/gemma-4-E2B-it.litertlm
```

Laptop mode also needs: `ollama create health-chat -f ollama/Modelfile` (once) and `adb reverse tcp:11434 tcp:11434`.
Restart the app after switching.

**Script**
1. (30 s) The moment: "Typhoon in Samar. Towers are down. A mother's child has diarrhea for three days. The health
   center is an hour away. ChatGPT needs signal. This doesn't."
2. (15 s) **Turn off Wi-Fi on the laptop** in front of the judges.
3. (60 s) Pick **Winaray**. Show the screen in Waray. Type: *"Tulo na ka adlaw nga nagkakalibang an akon anak,
   may dugo na."* → red banner (blood in the stool) instantly, then the AI answer, then the checked card.
4. (45 s) Follow-up: *"What if he can't drink?"* → the AI remembers the conversation; banner adds the danger sign.
5. (45 s) Safety: *"What medicine and how many mg?"* → the AI declines and gives first-aid steps; explain the
   guardrail and the evidence (base model invented child doses, ice on burns).
6. (45 s) How it's local: "What you just saw ran on this laptop's GPU. The same app also runs Gemma 4 E2B inside
   Android with LiteRT-LM" (show the emulator video clip; say it is the emulator, 35–70 s per reply on CPU); model
   comparison table; Waray fine-tune by our teammate (trained, not in the app yet).
7. (30 s) Close: "Every family in Eastern Visayas has a phone. Not every family has signal."

## Q&A prep

- **Why local?** Offline is when it's needed; health data is private. Both must-haves.
- **What runs offline?** Everything after the one-time model download.
- **Who is it for?** Parents and caregivers in Waray-speaking provinces, at night or during typhoons, far from care.
- **Why Gemma and not your fine-tuned model?** We compared Gemma against the **base** Sailor2-1B on the same 10
  questions; Gemma with our grounded prompt was the only setup with short, safe answers. The fine-tuned model file
  wasn't ready in time to test in the app. On its own held-out set it went from 4/25 to 10/25 Waray prompts passing
  (judged by an AI, not a native speaker), so it's our next step for better Waray.
- **How do you stop wrong medical advice?** Danger signs never depend on the AI; answers are grounded on checked
  cards; a deterministic guardrail hides medicines, doses, diagnoses, myths, and wrong emergency numbers.
- **Was it tested on a real phone?** Not yet; verified on the Android emulator running the model on-device.
- **Is the Waray reviewed?** Not by a native speaker yet; it is marked as unreviewed in the app.
