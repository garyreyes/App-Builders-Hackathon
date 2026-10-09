# Decisions

Why each real fork went the way it did. `ARCHITECTURE.md` records the winner; this records the reasoning.

## 2026-10-09: Pivot from BHW visit tool to general offline health chatbot
- **Decision:** general medical chatbot for people with no signal (team choice).
- **Why:** team preference for a chatbot that understands native languages.
- **Alternatives:** BHW visit-record tool (stronger "specific user" and "why local" scores, recommended by Claude);
  conversational BHW intake; patient-facing prenatal bot. Risks accepted: broader user, medical-advice safety,
  a crowded idea. Mitigated by grounded cards + rule-based triage.

## 2026-10-09: Native Android, no laptop fallback
- **Decision:** native Android app, on-device model, text only. No fallback to laptop AI + phone browser.
- **Why:** team wants a real phone app. A pocket device is also the realistic product for this user.
- **Alternatives:** laptop + hotspot browser (lowest risk, reused the verified Ollama setup), Windows only.
  Declined despite the deadline risk.

## 2026-10-09: Gemma 4 E2B over ~1B models
- **Decision:** Gemma 4 E2B `.litertlm`.
- **Why:** measured. gemma3:1b/qwen3 0.6B–1.7B misclassified topics and invented danger signs. Gemma 4 E2B got
  topics 5/6 and dangers 3/3 with none invented (PROJECT_FACTS.md).
- **Alternatives:** Gemma 3 1B (584 MB, much faster to download, fails the language test), Gemma 3n E2B (3.6 GB, gated).

## 2026-10-09: The model understands, pre-translated content speaks
- **Decision:** local-language replies come only from team-written content files. The model classifies and may add
  an English summary.
- **Why:** no small model wrote Bisaya/Waray reliably in testing. A wrong medical sentence in the user's language
  is the worst failure.
- **Alternatives:** model-generated local-language replies (rejected on evidence).

## 2026-10-09: App downloads the model itself on first launch
- **Decision:** DownloadManager fetches the pinned HF file once, verifies SHA-256. The app holds INTERNET permission
  for that purpose only.
- **Why:** team choice ("like `ollama pull`"). Simplest for users and judges, with no file handling.
- **Alternatives:** import from phone storage (keeps zero internet permission, a stronger offline proof), adb
  side-load (dev only), Ollama in Termux on the phone (judges would need a terminal; abandons the working runtime).
- **Cost accepted:** the "app can't reach the internet at all" pitch line becomes "the internet is used once,
  for the model download, then everything works in airplane mode."

## 2026-10-09: Content edited as JSON directly
- **Decision:** the teammate edits `assets/content/*.json` directly.
- **Why:** team choice. No converter script to build.
- **Alternatives:** spreadsheet → CSV → converter (safer for a non-coder).
- **Mitigation:** `ContentValidationTest` fails the build on broken JSON or a missing translation, naming the file.
