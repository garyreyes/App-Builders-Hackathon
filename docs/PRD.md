# PRD: Offline Multilingual Health Helper (working name)

> Status: **v2.1, Oct 9, ~23:00. Combined plan:** this product scope + the language collaborator's model track
> (Sailor2-1B GGUF on llama.cpp, LoRA on Waray chat pairs). Evidence: `docs/PROJECT_FACTS.md`. Stack: `docs/ARCHITECTURE.md`.
> Items marked **[ASSUMPTION]** were not explicitly confirmed.
> Context: AppBuildersPH Hackathon 2026 (Local AI). Hard deadline **10:00 AM, Sat Oct 10, 2026**.

---

## 1. Problem statement

When someone gets sick or hurt **where there is no signal**, they're on their own. That includes a far sitio,
an island, or a town whose towers are down after a typhoon or during a brownout. The question they face is:
**"Is this serious? Do we travel to the health center now, or what do we do at home?"**

- Cloud chatbots (ChatGPT, Gemini) **need signal**, which is exactly what's missing.
- Official health guidance (DOH/WHO) is mostly in **English**. Many people think and speak in **Bisaya or Waray**.
- Health questions are **private** (pregnancy, a child's illness). Sending them to a foreign server is a privacy
  cost, and health data is *sensitive personal information* under the Data Privacy Act (RA 10173).

**Why local AI:** **offline** first (the user's moment has no signal by definition), **privacy** second.

## 2. User types

| User | Moment | Touches the app? |
|---|---|---|
| **Resident / caregiver** (primary): a parent, family member, or neighbor with an Android phone | Someone at home is sick or hurt and there's no signal | Yes, types in their own language |
| **Team as content maintainers** | Build time: write topic cards, glossary, and translations | Edits content files, not the app UI |
| **Hackathon judge** (constraint, not a product user) | Installs the APK and model, runs the demo with Wi-Fi off | Setup + demo |

Pitch story uses one concrete moment, e.g. *a mother in a far sitio in Leyte, at night, after a typhoon,
whose child has had diarrhea for three days.*

## 3. Core use cases

1. **Set language once.** On first launch the user picks **Bisaya, Waray, Tagalog/Taglish, or English**. Changeable later.
2. **Ask in their own words.** The user types a message (any of the 4 languages, mixed is OK).
3. **Danger check first.** Instantly, before the AI finishes, a keyword check (team glossary) looks for danger signs
   (bleeding, can't breathe, seizure, very sleepy child, bleeding in pregnancy…). If one is found, a 🔴 card shows
   **"Go to the health center / call 911 now"** in the user's language. It's fixed text from a native speaker.
4. **Topic match.** The same glossary picks the matching topic(s). (The small model failed classification in testing,
   so routing is deterministic.)
5. **Grounded answer.** The app shows the matching **topic card in the user's language** (pre-translated), with
   at-home steps and "go now if…" signs. Then the on-device model (**Sailor2-1B, LoRA-adapted on Waray health chat
   pairs**) writes a short reply **in the user's language**, grounded in that card. A **guardrail** hides any reply
   that contains doses, medicine names, or a diagnosis. The card always stays.
6. **Not covered.** If the topic is `none`, the app says, in the user's language, that it can't help with this
   and to go to the nearest health center. It never guesses.
7. **Browse without typing.** Topic buttons let a user open a card directly (recognition over recall).

**Topic set for v1 (7 + none):** child diarrhea/dehydration, fever, cough & difficulty breathing, wounds & bleeding,
burns, pregnancy warning signs, dengue warning signs.

## 4. Out of scope for v1

- Diagnosis, prescriptions, or medicine doses. The app gives **first-aid steps and when to go**, nothing more.
- Model replies that mention doses, medicine names, or diagnoses (blocked by the guardrail). Danger warnings are
  never model-written. They're always pre-translated.
- Topics outside the 7-topic set
- Voice input/output (phone-sized Bisaya/Waray speech models are too heavy)
- Any cloud AI, sync, analytics, or **internet access by the app at all**
- Accounts and login. Chat history is not stored after the app closes. **[ASSUMPTION]**
- Windows app and iOS (Android only, decided Oct 9)
- The BHW visit-record tool from PRD v1 (considered and dropped)

## 5. Success metrics

Measured on a team test set before the 7:00 AM freeze: **20 messages** across the 4 languages, including
**8 with danger signs** and **4 off-topic** questions. The native speaker writes and grades them.

| Metric | Target |
|---|---|
| Danger-sign messages that show 🔴 | **8/8** (keyword check + model). A miss is the one unacceptable failure |
| False 🔴 on non-danger messages | ≤ 2 of 12 |
| Correct topic card shown | ≥ 70% |
| Off-topic → "not covered, go to health center" | 4/4 |
| Time to 🔴 / keyword card after tapping send | ≤ 1 s |
| Time to first AI reply token on the demo phone (6 GB RAM) | ≤ 30 s |
| Model replies with a dose, medicine name, or diagnosis that reach the screen | **0** (guardrail) |
| Adapted (LoRA) model vs baseline on the held-out Waray set, speaker-rated | Better, or the baseline ships |
| Full flow in airplane mode | 10/10 rehearsal runs |
| Judge setup (install APK + get model onto phone) | ≤ 20 min hands-on |

## 6. Constraints

- **Deadline:** feature freeze 7:00 AM, submit 9:30 AM, hard deadline 10:00 AM Sat Oct 10. One submission, no edits.
- **Rules:** core AI on-device, airplane-mode test, full disclosure of models/libraries/AI tools, no outside help.
- **Platform:** native Android, **API 28+ / arm64-v8a**. Demo phone has **6 GB RAM**.
- **Runtime:** **llama.cpp** built for Android (NDK). Unverified on the phone until the Phase 0 spike.
- **Model:** **Sailor2-1B-Chat Q4_K_M GGUF** (0.74 GB, Apache-2.0), downloaded once by the app. **LoRA fine-tune**
  on native-speaker-reviewed Waray health chat pairs, trained on the RTX 4050 laptop.
- **Language reality (tested Oct 9):** Sailor2-1B writes Bisaya more naturally than Gemma 4, but it fails JSON
  classification and invents doses/medicines. So: glossary routing + guardrail + language from the user's setting.
- **Budget:** ₱0. **Team:** 2 people (one native Bisaya/Waray speaker writes content and translations).
- **Repo:** public by 10 AM. Model files never committed.

## 7. Edge cases and failure states

| Situation | Required behavior |
|---|---|
| Model still loading (first launch can take a while) | Keyword triage and topic buttons work immediately. "AI warming up…" shown. Send still works with keyword-only results |
| Model fails to load (low RAM) | App stays useful in **keyword-only mode**: danger check + topic cards. A clear notice says so. Never crashes |
| Model file missing | Setup screen explains how to add it. Keyword mode still works |
| No danger keyword, but the model flags one | Show 🔴. False alarms are the safe direction |
| Model returns invalid JSON or an unknown topic | Treat as `none`, but keyword-matched cards and 🔴 still show |
| Model is slow | Keyword results show first. AI result updates the screen when ready |
| Several topics match | Show the top card, with the other matches as buttons |
| Message in a language not in the 4 | Model still classifies. Card shows in the user's chosen language |
| Empty or very long message | Empty send disabled. Long text trimmed before the model |
| User picked the wrong language | Language switch is always one tap away on the chat screen |

## 8. Feature prioritization (MoSCoW)

**Must have**
- Language picker (Bisaya, Waray, Tagalog/Taglish, English)
- Chat screen: type a message, see results
- Keyword danger triage (glossary) → 🔴 card in the user's language
- On-device Sailor2-1B reply grounded in the matched card, behind the guardrail
- LoRA training run on reviewed Waray health chat pairs + held-out comparison against the baseline
- 7 topic cards, pre-translated into 4 languages, with sources cited
- "Not covered → go to health center" response
- Always-visible "not a doctor, no diagnosis" notice
- Works in airplane mode, with no INTERNET permission
- README with setup (APK + model) and the full disclosures list

**Should have**
- Topic browse buttons
- Keyword-only mode when the model can't load
- Test-set script that scores the 20 messages (proof for the pitch)

**Could have**
- GPU backend toggle (if faster on the demo phone)
- Quick-reply chips for follow-up questions
- Emergency hotlines card

**Won't have (this version)**
- Everything in section 4

## Open questions

1. Final product name.
2. Which DOH/WHO public documents each topic card cites.
3. How the 2+ GB model gets onto the phone (side-load vs file picker vs bundled). Decided in `ARCHITECTURE.md`.
