# Teammate tasks: Health Library, "Who is this for?", and memory

> For the language/content collaborator. Written Oct 9, late. The app side is already designed in
> [ARCHITECTURE.md](ARCHITECTURE.md) and [DECISIONS.md](DECISIONS.md). This file lists only **what we need from you**,
> **questions for you**, and **what we left out**. Deliver work on a branch + PR ([collaboration.md](collaboration.md)).

## What changed, in short

1. **Your "unified information library" idea is in.** The content JSON is now called the **Health Library**. It holds
   the verified first-aid facts *and* the words the safety filter checks AI replies against.
2. **"Who is this for?"** Chips under the answer (Baby · Child · Adult · Pregnant) can **add** danger warnings
   (e.g. fever in a young baby). They never remove one.
3. **Memory (optional, on the phone only).** If the user turns it on, the app remembers their name, the people they
   care for, their nearest health center, and past questions. **The AI never asks for personal details**, and in v1
   the model never sees the memory. The app uses it. Stored in SQLite (Room), as you suggested.

## What we need from you (in priority order)

Put files in `android/app/src/main/assets/content/` (the path is fixed even if the `android/` folder isn't there yet).
The JSON shapes below are examples. If the loader needs a small change, the app PR will say so.

**1. `guardrail_terms.json` (Must).** Words that make the app **hide** an AI reply. Kinds: `DRUG` (medicine
names), `DOSE_UNIT` (ml, kutsara, tableta…), `DIAGNOSIS` ("may dengue ka"…), and `DOWNPLAY` (phrases that say
it's not serious, like "no need to go" or "okay ra"). `DOWNPLAY` is needed in **all 4 languages**: the app blocks those
replies when a 🔴 warning is showing.
```json
[
  { "kind": "DRUG", "term": "paracetamol" },
  { "kind": "DOSE_UNIT", "term": "kutsara" },
  { "kind": "DOWNPLAY", "term": "dili kinahanglan moadto", "language": "CEB" }
]
```
(Examples only, please check the wording.) Start from the Oct 9 Sailor2 outputs in [PROJECT_FACTS.md](PROJECT_FACTS.md).

**2. UI strings for the danger banner (Should), in `ui_strings.json` × 4 languages.** "Go to {healthCenter} now",
"or call 911", and the generic "Go to the nearest health center now".

**3. "Who is this for?" words + rules (Should).**
- **Glossary words** that hint at the person: `anak`, `bata`, `baby`, `buntis`, `mabdos`, `burod`… Please check and add
  real Waray/Bisaya/Tagalog words. Example: `{ "term": "burod", "target": "PREGNANT", "stem": false }`.
- **Age bands:** we proposed WHO IMCI's (young infant < 2 months, child 2 months–5 years). **Please confirm or correct from your source.**
- **`patient_rules.json`:** what changes per group, **each with its source**. Rules can only add.
```json
[
  { "group": "PREGNANT", "when": "FEVER", "adds": "PREGNANCY_WARNING", "source": "<DOH/WHO document + section>" }
]
```
If a rule needs a **new danger sign** (e.g. fever in a young infant), tell us an id like `YOUNG_INFANT_FEVER` and
write its danger message in 4 languages. We'll add it to the app's list.

**4. Test set (Should).** Add a `patient_group` column to [`evaluation/waray_chat_template.csv`](../evaluation/waray_chat_template.csv)
(after `topic`) and a few rows where the person matters, e.g. a young baby with fever → expected 🔴.

**5. Memory text (Could), × 4 languages.** Greeting ("Maayong gabii, {name}"), chip labels with age ranges, the
"Save {name} so I remember next time?" offer, the consent sheet (what's saved, stays on this phone, others using this
phone can see it, delete anytime), and the "Forget everything" confirmation. The app PR will list the final keys.

## Questions for you

1. **By "information validation", did you also mean fact-checking things the user believes** (e.g. home remedies
   on burns)? If yes, we'll add a `myths.json` (per topic "don't do this" + source) and show it on the card.
2. **Age bands and patient rules:** which DOH/WHO document do you want to cite?
3. **Defaults OK?** Past questions are deleted after **30 days**. "Pregnant" turns off after **10 months**.

## Please don't change

- **The prompt / training template.** In v1 the prompt has **no patient or memory line** (see [handoff.md](handoff.md)),
  so train and evaluate the LoRA exactly as before. `training/reviewed_chat_template.csv` stays as it is.

## Left out of v1 (deferred to v2)

| Item | Why it waits |
|---|---|
| Memory or patient context in the model prompt | Needs a new training template + retrain, and there's no time before the freeze |
| App lock | Must use Android's own device lock (`BiometricPrompt`), never a homemade PIN. Not needed for the demo |
| Encrypted database (SQLCipher) | App-private storage + no backup is enough for v1 |
| Bundled health-facility directory (names + numbers) | Needs a licensed data source and app updates. v1 uses 911 + the user's own entry |
| Repeat-visit hints ("you asked about Mia's fever 2 days ago…") | Needs duration rules from the cards' sources |
| Myth-checking (`myths.json`) | Waiting on your answer to question 1 |
