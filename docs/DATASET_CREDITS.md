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
