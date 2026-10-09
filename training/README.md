# Waray chat training workflow

## The decision

Start from [Sailor2-1B-Chat](https://huggingface.co/sail/Sailor2-1B-Chat), which already has Waray exposure. Do **supervised fine-tuning with LoRA** on short, reviewed Waray **health** conversations covering the app's 7 first-aid topics (see `docs/PRD.md`). Training teaches the model how to answer in this app: reply in the user's language, stay inside the matched topic card, **never give doses or medicine names**, and send off-topic questions to the health center. The Oct 9 baseline failed all four (see `docs/PROJECT_FACTS.md`), so the test set must check each one. It does not need to relearn all of Waray from scratch.
The owner permits an AI-reviewed experimental prototype if each row clearly names the reviewing model and method. Native-speaker review is still required before claiming the delivered app's Waray quality.

The Sailor2 `sea-synthetic/waray` files contain long generated passages, not chat pairs. Do not dump the whole corpus into this fine-tuning run. A speaker may adapt a useful passage into a question and a checked answer, with its source recorded.

## Data to prepare

Use the header in [reviewed_chat_template.csv](reviewed_chat_template.csv). One row is one user message and the answer the assistant should give. `split` must be `train` or `test`. For an AI-reviewed prototype, check `assistant_reply` with an independent model or source where possible, and set `reviewed_by` to an honest identifier such as `ai:model-name/version; method=...`. Do not use Sailor2 to approve its own output. Record the source and rights; never invent either field. Keep private data outside Git.

Make the test rows first and keep them out of training. Include natural greetings, questions, mixed Waray/Filipino/English, local vocabulary, and cases where the model should admit uncertainty. Include multiple Waray varieties if the app is meant to support them. Exact number depends on quality; begin with a few dozen test questions and expand from measured failures. AI-generated test questions must come from a source or model separate from the training examples and remain untouched by training.

## Run order

1. **Baseline:** run the untouched Sailor2-1B-Chat GGUF on the test questions. Have an independent reviewer rate correctness, naturalness, language choice, and invented facts; identify whether that reviewer is AI or human. Record response time on the phone.
2. **Prepare data:** remove duplicates, private information, empty answers, and examples the reviewer rejects. Keep `train` and `test` separate. Convert approved `train` rows into the chat format expected by the trainer.
3. **Fine-tune:** use Hugging Face TRL `SFTTrainer` with PEFT/LoRA on a CUDA GPU with enough memory. Use short sequences and small batches for the 1B model. Train on a computer or GPU notebook, never on the phone.
4. **Compare:** run the same untouched test questions against the baseline and adapted models. Keep the adapted model only if independent ratings improve without harming basic chat behavior. AI ratings establish a prototype signal, not native-speaker quality.
5. **Deliver:** merge the LoRA adapter into the base model, export GGUF, quantize to Q4_K_M, record a SHA-256 hash and license, then measure memory, speed, and offline operation on the 6 GB phone.

The earlier PC check found a GTX 1050 Ti with 4 GB VRAM. The current training laptop has an RTX 4050 with 6 GB VRAM; see [workstation notes](../docs/workstations.md). LoRA on Sailor2-1B-Chat runs here: Run 03 trained in 2,272 MiB peak VRAM (see [progress tracking](../docs/PROGRESS_TRACKING.md)). To try a delivered GGUF in the app, use `ollama/Modelfile.sailor2` and compare it against the demo model on the same 10 questions ([ollama/README.md](../ollama/README.md)).

To check GPU inference without changing model weights, create a local `.venv`, install a CUDA-enabled PyTorch build plus `transformers`, `accelerate`, and `huggingface_hub`, then run `python training/gpu_smoke.py` from the repository root. The script downloads the official 1B chat model into the normal Hugging Face cache and writes its revision, GPU memory use, timing, and sample reply to ignored `outputs/gpu-smoke.json`. Its English sample prompt only verifies that the software path works; it is not the required Waray baseline evaluation.

Sailor2 also publishes [stage-two supervised data](https://huggingface.co/datasets/sailor2/sailor2-sft-stage2). Its dataset viewer reports 1,200 Waray, 1,198 Cebuano, and 1,131 Ilocano examples. This is useful reference material, but published examples are not automatically native-speaker-reviewed or approved for this app. Check source rights, sample quality, and training/test overlap before use. No reviewed training or held-out test rows are in this repository yet.

## Unattended local CLI

`local_cli.py` performs its computation entirely on this laptop. It makes no Codex or paid model API calls, so leaving a training command running uses no chat tokens. It writes progress to the terminal and saves the adapter, a run manifest, and a comparison CSV under ignored `outputs/`. The command never uploads model weights or examples.

On this Windows laptop, install the reproducible environment once from the repository root:

```powershell
py -m venv .venv
.\.venv\Scripts\python.exe -m pip install torch==2.11.0+cu128 --index-url https://download.pytorch.org/whl/cu128
.\.venv\Scripts\python.exe -m pip install -r training\requirements-local.txt
```

Fill a private copy of `training/reviewed_chat_template.csv` with reviewed examples. An AI-reviewed prototype is allowed, but mark its reviewer honestly. Include at least 20 separate `test` questions and keep them out of `train`. Every row needs a reviewer code, source, and rights note. The CLI checks these fields and duplicate prompts; it cannot establish language quality, privacy, or actual permission from text fields alone.

```powershell
.\.venv\Scripts\python.exe training\local_cli.py check --data data\private\waray-reviewed.csv
.\.venv\Scripts\python.exe training\local_cli.py baseline --data data\private\waray-reviewed.csv --output outputs\waray-baseline.csv
```

Have an independent reviewer read every baseline reply in `outputs/waray-baseline.csv`, enter `yes` or `no` in the four rating columns, and fill `reviewed_by` with a person or an explicit AI model/method identifier. Do not put reviewed test rows into training. If the baseline has at least one recorded failure, the following single command trains a 4-bit LoRA adapter with short sequences and batch size one, then produces same-question responses for comparison:

```powershell
.\.venv\Scripts\python.exe training\local_cli.py train --data data\private\waray-reviewed.csv --baseline-review outputs\waray-baseline.csv --output outputs\waray-run-02 --epochs 3
```

The CLI refuses incomplete reviews, changed test prompts, reused output paths, and training/test prompt overlap. `--max-steps 10` can cap a pilot run, or `--epochs <N>` sets full training epochs. An independent reviewer must rate the adapted replies in `outputs/<run>/comparison.csv`. AI ratings support an experimental comparison only; do not call them native-speaker validation.

### Adapter merge and GGUF export for Android

Once an adapted run is evaluated, merge the LoRA weights into the base model and convert to GGUF format:

```powershell
# 1. Merge LoRA adapter into base model weights
.\.venv\Scripts\python.exe training\merge_adapter.py --adapter outputs\waray-run-02\adapter --output models\waray-sailor2-1b-merged

# 2. Convert to GGUF (Q8_0 for compact high quality ~1.05 GB, or F16 ~1.98 GB)
.\.venv\Scripts\python.exe .venv\llama.cpp\convert_hf_to_gguf.py models\waray-sailor2-1b-merged --outfile models\waray-chat-v1-q8_0.gguf --outtype q8_0
.\.venv\Scripts\python.exe .venv\llama.cpp\convert_hf_to_gguf.py models\waray-sailor2-1b-merged --outfile models\waray-chat-v1-f16.gguf --outtype f16
```

This desktop process does not replace the offline Android phone checks. Test on an API 28+ 6 GB phone in airplane mode. For Cebuano and Ilocano, use separate reviewed CSV files and output folders after the Waray run is evaluated.

### Separate medical chatbot pipeline (`ruslanmv/ai-medical-chatbot`)

For clinical medical dialogues, a dedicated pipeline ingests and curates dialogues from [`ruslanmv/ai-medical-chatbot`](https://huggingface.co/datasets/ruslanmv/ai-medical-chatbot) by Ruslan Magana Vsevolodovna (see [`docs/DATASET_CREDITS.md`](../docs/DATASET_CREDITS.md)):

```powershell
# 1. Prepare and filter medical dataset
.\.venv\Scripts\python.exe training\prepare_medical_data.py

# 2. Check dataset integrity
.\.venv\Scripts\python.exe training\medical_cli.py check --data data\private\medical-chat-reviewed.csv

# 3. Train medical LoRA adapter
.\.venv\Scripts\python.exe training\medical_cli.py train --data data\private\medical-chat-reviewed.csv --baseline-review outputs\medical-baseline.csv --output outputs\medical-run-01 --epochs 3 --all-linear --max-length 384

# 4. Merge adapter and export GGUF
.\.venv\Scripts\python.exe training\merge_adapter.py --adapter outputs\medical-run-01\adapter --output models\medical-sailor2-1b-merged
.\.venv\Scripts\python.exe .venv\llama.cpp\convert_hf_to_gguf.py models\medical-sailor2-1b-merged --outfile models\medical-chat-v1-q8_0.gguf --outtype q8_0
```

## Separate speech track

Speech recognition needs recorded Waray audio and matching transcripts. Text chat fine-tuning will not make a microphone understand Waray.

