# PRD: Offline Health Visit Assistant (working name)

> Status: **v1, confirmed Oct 9 (compressed pass).** Built from decisions made in planning chat and
> `PROJECT-PROPOSAL.md`. Items still marked **[ASSUMPTION]** were not explicitly confirmed.
> Context: AppBuildersPH Hackathon 2026 (Local AI). Hard deadline **10:00 AM, Sat Oct 10, 2026**.

---

## 1. Problem statement

Barangay Health Workers (BHWs) do home visits in remote puroks and sitios where there is often
**no cell signal**. Patients describe how they feel in **Waray, Bisaya, or Taglish**. The BHW's records
are in **English**.

Today the BHW:
- translates in their head while writing in a paper notebook, so details get lost;
- can't let the patient verify the record, because the patient can't read English;
- can miss **danger signs** in a rushed note (e.g. headache + blurred vision in pregnancy);
- re-copies everything by hand at night for the midwife's report.

Cloud AI can't fix this: there is no signal where the visits happen, health data is *sensitive personal
information* under the Data Privacy Act (RA 10173), and BHWs are volunteers with no budget for per-use AI.

**Why now:** the hackathon challenge is "AI that stays useful when the cloud disappears." Small open
models (3–4B) can now run on an ordinary laptop GPU, which makes on-device multilingual intake possible.

---

## 2. User types

| User | Role in the system | Touches the app? |
|---|---|---|
| **BHW** (primary) | Records the visit, reviews the filled form, reads back to the patient, saves | Yes, the main operator |
| **Patient / mother** (secondary) | Speaks in their language, confirms the readback, receives the referral slip | Indirectly: speaks into it, hears/sees readback |
| **Midwife at the Barangay Health Station** (tertiary) | Receives exported records, acts on 🔴 referrals | Only via exported records (Could-have view) |
| **Hackathon judge** (constraint, not a product user) | Sets up and runs it from the README | Setup + demo flow |

---

## 3. Core use cases

**Demo program: prenatal check-in visits.** (Sick-child visits are a Could-have.)

1. **Record a visit.** The BHW picks their name (BHW name picker) and presses record. The patient (or the BHW,
   repeating what the patient said) speaks in Waray/Bisaya/Taglish. Typing is always available as a fallback.
   The app **auto-detects** the language and shows it ("Detected: Waray"). The BHW can change it with one tap.
2. **Get a structured record.** The app transcribes on-device and the local LLM fills the visit record:
   name, age, months pregnant, symptoms, onset/duration, medicines, plus BHW-typed measurements
   (BP, temperature, weight). It keeps both the original-language transcript and the English summary.
3. **Read back and confirm.** The app shows the record back in the patient's language, mostly using
   fixed phrases a native speaker translated, with only short values model-generated. It asks
   "Is this right?" The BHW can edit any field. The patient confirms.
4. **Flag danger signs.** Fixed rules (not AI judgment) check the record and transcript against an
   official prenatal danger-sign list:
   - 🟢 **OK**: advice + next visit date
   - 🟡 **Watch**: return in a few days
   - 🔴 **Refer**: referral slip, with an English half for the midwife and a patient-language half for the mother
5. **Review today's visits and export.** List of today's records, follow-ups due, and an export file
   for the health station computer (USB/file, no internet).

---

## 4. Out of scope for v1 (hackathon build)

- Diagnosis or treatment recommendations. The app **records and flags**, it never diagnoses.
- HIV, mental health, reproductive health beyond prenatal check-ins, adolescent health
- Full household profiling surveys
- Any cloud AI, cloud sync, or cloud backup
- Training or fine-tuning a model
- Live sync over the station Wi-Fi router (pitched as the next step, not built)
- Phone app (demo is a laptop web app; phones are the pitched next step)
- User accounts/login/passwords. Only a simple BHW name picker, so records show who recorded them
- Spoken readback in v1 core. The readback is shown on screen and the BHW reads it aloud (TTS stays a Could-have)

---

## 5. Success metrics

Measured on our own test set (10 patient stories written by the team's native speaker) before 7:00 AM Oct 10.

| Metric | Target (confirmed: "safest" bar) | Why this bar |
|---|---|---|
| Field extraction accuracy (Bisaya + Taglish) | ≥ 70% of fields correct | Achievable tonight. Anything above is a pitch bonus |
| Field extraction accuracy (Waray) | ≥ 70% to be shown in the demo; otherwise pitched as "in progress" | Same bar. Don't demo a language that fails |
| Danger-sign recall on test stories that contain one | **10/10 flagged 🔴** (not lowered) | Rule-based, so achievable. A missed danger sign is the one unacceptable failure |
| False 🔴 on stories with no danger sign | ≤ 2 in 10 | Leaning toward false alarms is the safe direction |
| End of speech → filled form (warm models, demo laptop) | ≤ 30 seconds | Room for speech + LLM on a cold-ish GPU |
| Full flow with Wi-Fi **off** | 10/10 rehearsal runs succeed | Required by the rules |
| Judge setup from README | ≤ 20 min of hands-on steps (downloads excluded) | Multiple installs (Ollama + Python + models) |
| Hackathon outcome | Selected as a finalist (top 10–15) | Goal |

---

## 6. Constraints

- **Deadline:** feature freeze 7:00 AM, submit 9:30 AM, hard deadline 10:00 AM Sat Oct 10. One submission, no edits.
- **Rules:** core AI must run on-device. The airplane-mode test must pass. Every model, library, and AI tool must be disclosed. No outside help.
- **Hardware:** demo laptop RTX 4050 (6 GB VRAM), 16 GB RAM, Windows 11. Models must also run (slower) on a judge's laptop.
- **Models (installed, verified on GPU):** Ollama + `gemma3:4b` / `qwen3:4b`; Meta MMS-1B-all (ceb, war, tgl); Whisper large-v3-turbo.
- **Language reality (tested Oct 9):** small models misread Waray without help. A team-written **Waray/Bisaya health glossary** in the prompt fixed the test sentence, so the glossary is a required component.
- **Repo:** public by 10 AM. Model files and personal notes stay out of git.
- **Budget:** ₱0. All tools free/open-source.
- **Team:** 2 people. Split by AI part and screens.

---

## 7. Edge cases and failure states

| Use case | What can go wrong | Required behavior |
|---|---|---|
| Record | Speech-to-text output is garbled or empty | Show the transcript so the BHW can see it failed, then offer **edit transcript** or **type instead**. Never silently fill a form from garbage |
| Record | Mic permission denied / no mic | Typed input works, with a clear message |
| Record | Language auto-detected wrong (Waray vs Bisaya sound alike) | Detected language is always shown. One tap switches it and re-runs. Readback never uses an unconfirmed language |
| Record | Mixed languages in one answer (Taglish + Waray) | Accept it; glossary covers both. Low confidence → fields left blank, not guessed |
| Structure | LLM leaves a required field empty | Field shows "not mentioned" and the BHW is prompted to ask the patient. **Never invented** |
| Structure | LLM returns invalid output | Structured output is enforced. On failure, retry once, then fall back to transcript + manual form |
| Structure | Models still loading (cold start 25–60 s) | Loading state at app start with "warming up" status. Recording disabled until ready |
| Readback | Patient says the readback is wrong | BHW edits the field, readback regenerates, patient re-confirms |
| Readback | Model-written value in Waray is wrong | Only short values are generated. Labels and sentences are fixed native-speaker translations |
| Flags | Danger sign said but missed by the LLM | Rules check **both** the extracted fields **and** glossary keywords in the raw transcript |
| Flags | Measurement typed wrong (e.g. BP 1400/90) | Range validation on measurement inputs |
| Save/export | App closed mid-visit | Draft is kept until saved or discarded **[ASSUMPTION]** |
| Demo | Judge laptop has no GPU | Runs on CPU, slower. README states expected speed honestly |

---

## 8. Feature prioritization (MoSCoW)

**Must have**
- Automatic language detection (Waray / Bisaya / Taglish), shown on screen with a one-tap override
- Voice input with on-device speech-to-text, plus typed fallback
- Local LLM → structured prenatal visit record, using glossary-assisted prompting
- Editable review screen showing transcript + English record
- Readback in patient language, **shown on screen** for the BHW to read aloud (fixed translated phrases + short model values)
- Rule-based danger-sign flags 🟢🟡🔴 with a bilingual referral slip
- Everything working with Wi-Fi off
- README with setup steps and the disclosures list

**Should have**
- BHW name picker (no passwords), stamped on every record
- Today's visit list with follow-ups due
- Export records to a file for the health station
- Test-set script that scores extraction accuracy (proof for the pitch)

**Could have**
- Spoken readback (text-to-speech in the patient's language)
- Sick-child visit program
- Midwife view: 🔴 first, counts per purok
- Photo of an old notebook page → digitized record (vision model)

**Won't have (this version)**
- Everything in section 4

---

## Open questions

1. Final product name.
2. ~~Readback shown or spoken?~~ **Decided:** shown on screen. TTS is a Could-have.
3. ~~Success metrics~~ **Decided:** the safer bar in section 5.
4. Official prenatal danger-sign list: which DOH/WHO public document we cite.
5. **Auto-detect risk:** Whisper can't identify Waray/Bisaya. Detection needs Meta's MMS language-ID model or a
   text-level check after transcription. Must be tested in the first-hour test.
