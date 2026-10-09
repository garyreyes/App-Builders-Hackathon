# Waray chat training workflow

## The decision

Start from [Sailor2-1B-Chat](https://huggingface.co/sail/Sailor2-1B-Chat), which already has Waray exposure. Do **supervised fine-tuning with LoRA** on short, native-speaker-reviewed Waray conversations. Training teaches the model how to answer in this app. It does not need to relearn all of Waray from scratch.

The Sailor2 `sea-synthetic/waray` files contain long generated passages, not chat pairs. Do not dump the whole corpus into this fine-tuning run. A speaker may adapt a useful passage into a question and a checked answer, with its source recorded.

## Data to prepare

Use the header in [reviewed_chat_template.csv](reviewed_chat_template.csv). One row is one user message and the answer the assistant should give. `split` must be `train` or `test`. A speaker must check `assistant_reply`, and `reviewed_by` must identify the reviewer or a private reviewer code. Record the source and rights. Keep private data outside Git.

Make the test rows first and keep them out of training. Include natural greetings, questions, mixed Waray/Filipino/English, local vocabulary, and cases where the model should admit uncertainty. Include multiple Waray varieties if the app is meant to support them. Exact number depends on quality; begin with a few dozen test questions and expand from measured failures.

## Run order

1. **Baseline:** run the untouched Sailor2-1B-Chat GGUF on the test questions. Have speakers rate correctness, naturalness, language choice, and invented facts. Record response time on the phone.
2. **Prepare data:** remove duplicates, private information, empty answers, and examples speakers reject. Keep `train` and `test` separate. Convert approved `train` rows into the chat format expected by the trainer.
3. **Fine-tune:** use Hugging Face TRL `SFTTrainer` with PEFT/LoRA on a CUDA GPU with enough memory. Use short sequences and small batches for the 1B model. Train on a computer or GPU notebook, never on the phone.
4. **Compare:** run the same untouched test questions against the baseline and adapted models. Keep the adapted model only if speaker ratings improve without harming basic chat behavior.
5. **Deliver:** merge the LoRA adapter into the base model, export GGUF, quantize to Q4_K_M, record a SHA-256 hash and license, then measure memory, speed, and offline operation on the 6 GB phone.

The earlier PC check found a GTX 1050 Ti with 4 GB VRAM. The current training laptop has an RTX 4050 with 6 GB VRAM; see [workstation notes](../docs/workstations.md). LoRA on Sailor2-1B-Chat is plausible here but has not yet been validated by a training run.

To check GPU inference without changing model weights, create a local `.venv`, install a CUDA-enabled PyTorch build plus `transformers`, `accelerate`, and `huggingface_hub`, then run `python training/gpu_smoke.py` from the repository root. The script downloads the official 1B chat model into the normal Hugging Face cache and writes its revision, GPU memory use, timing, and sample reply to ignored `outputs/gpu-smoke.json`. Its English sample prompt only verifies that the software path works; it is not the required Waray baseline evaluation.

Sailor2 also publishes [stage-two supervised data](https://huggingface.co/datasets/sailor2/sailor2-sft-stage2). Its dataset viewer reports 1,200 Waray, 1,198 Cebuano, and 1,131 Ilocano examples. This is useful reference material, but published examples are not automatically native-speaker-reviewed or approved for this app. Check source rights, sample quality, and training/test overlap before use. No reviewed training or held-out test rows are in this repository yet.

## Separate speech track

Speech recognition needs recorded Waray audio and matching transcripts. Text chat fine-tuning will not make a microphone understand Waray.
