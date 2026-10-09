# Dataset Credits & Attribution

This document records the external datasets, benchmarks, and data sources utilized across model training and evaluation tracks in this repository.

---

## 1. AI Medical Chatbot Dataset (`ruslanmv/ai-medical-chatbot`)

- **Dataset Name:** AI Medical Chatbot
- **Hugging Face Hub:** [`https://huggingface.co/datasets/ruslanmv/ai-medical-chatbot`](https://huggingface.co/datasets/ruslanmv/ai-medical-chatbot)
- **Dataset Viewer:** [`https://huggingface.co/datasets/ruslanmv/ai-medical-chatbot/viewer/default/train`](https://huggingface.co/datasets/ruslanmv/ai-medical-chatbot/viewer/default/train)
- **Author & Project Creator:** Ruslan Magana Vsevolodovna ([@ruslanmv](https://huggingface.co/ruslanmv))
- **Original Repository / Project:** [GitHub: ruslanmv/AI-Medical-Chatbot](https://github.com/ruslanmv/AI-Medical-Chatbot)
- **Dataset Description:** A corpus of ~256,916 real-world doctor-patient dialogues across medical specialties, including patient inquiries, description summaries, and verified medical practitioner responses.
- **Role in this Project:**
  - Used as the primary clinical dialogue reference for the dedicated medical chatbot training track (`training/medical_cli.py`).
  - Filtered and curated to train the assistant on frontline primary healthcare, symptom assessment (fever, colds, cough, headache, digestive distress, minor trauma, allergies), lifestyle/hydration advice, and medical communication standards.
  - Adapted with strict boundaries to ensure the offline assistant prescribes **only Over-The-Counter (non-prescription) medicines** and clearly directs users to licensed physicians or Barangay Health Workers (BHW) for prescription-only needs or danger signs.
- **Citation / Attribution:**
  > Ruslan Magana Vsevolodovna. *AI Medical Chatbot Dataset*. Hugging Face Datasets (2023). Available at: `https://huggingface.co/datasets/ruslanmv/ai-medical-chatbot`.

---

## 2. Sailor2 Base Model & Multi-Language Resources

- **Model Name:** Sailor2-1B-Chat
- **Hugging Face Hub:** [`https://huggingface.co/sail/Sailor2-1B-Chat`](https://huggingface.co/sail/Sailor2-1B-Chat)
- **Authors & Organization:** Sea AI Lab (SAIL), Singapore
- **License:** Apache License 2.0
- **Role in this Project:** Base foundation model for lightweight, offline Southeast Asian language and edge device deployment on `arm64-v8a` Android devices.

---

## 3. Prototype Waray-First Evaluation & Alignment Dataset

- **Source:** Local domain-curated evaluation and prototype alignment dataset (`data/private/waray-reviewed.csv`)
- **Review Methodology:** Evaluated and cross-validated with `ai:gemini-3.8-flash; method=syntactic-and-lexical-cross-validation-waray; domain=medical-otc-and-general`
- **Domain Coverage:** Eastern Visayas regional geography, local cultural concepts (Binagol, Salukara, Moron, Natong), polite conversation, offline status honesty, and Waray frontline OTC medical phrasing with clinical terminology maintained in English.
- **Attribution & Rights:** Private local hackathon prototype authorized by project owner; strictly maintained with held-out test split isolation.

### Run 04 expansion

- **Rows:** 145 new, synthetic train-only pairs drafted by `ai:OpenAI Codex GPT-6` using `training/expand_waray_data.py`; 265 total train and 25 unchanged test. The exact run snapshot is private (`data/private/waray-reviewed-run04.csv`, SHA-256 `7db34b96626e0d551a1ca32764ac5d13cc1d25874005b535c08c740983141a30`). The current private CSV has corrected source metadata and the same prompts/replies.
- **Source grounding:** [Tacloban City on San Juanico Bridge](https://tacloban.gov.ph/san-juanico-bridge/), [Santa Rita history](https://santarita.gov.ph/history/), [Samar province profile](https://samar.gov.ph/samar-profile/), [Northern Samar profile](https://saad.da.gov.ph/phase-2/northern-samar/), [Biliran municipality profile](https://biliran.gov.ph/municipality/), [Philippine Statistics Authority region listing](https://psa.gov.ph/classification/psgc/cities/0800000000), [Calbayog on Waray variation](https://calbayog.gov.ph/the-waray-language-the-voice-of-calbayog/), [PNA/DOT on regional food](https://www.pna.gov.ph/articles/1267861), [Eastern Samar provincial food description](https://easternsamar.gov.ph/about-eastern-samar/), [PIA on sagmani](https://pia.gov.ph/features/e-visayas-msmes-break-into-global-markets-make-local-flavors-global-opportunities/), and [DOST on gabi leaves and coconut cream](https://techtrans.gov.ph/utility-models/thermally-processed-coconut-cream-based-dish).
- **Rights and review:** The added questions and replies are original synthetic paraphrases for private local prototype testing. Source facts and links are attributed per row. No source article text or third-party personal data was copied into training. AI review is not native-speaker review; a native Waray speaker and clinician should review language and medical behavior before public use.
