# Sailor2 source notes

Checked 2026-10-09. These are candidate sources, not approved training data or a tested Android build.

## Chat baseline

- Official model: [sail/Sailor2-1B-Chat](https://huggingface.co/sail/Sailor2-1B-Chat). The model card says it includes Waray and uses Apache 2.0.
- A third-party [Q4_K_M GGUF](https://huggingface.co/bartowski/Sailor2-1B-Chat-GGUF) is listed at about 0.74 GB. Verify its exact file, license, and hash when downloaded. A 6 GB RAM phone is a plausible test target, but actual memory and speed are unknown until measured on the device.
- Run a native-speaker evaluation before choosing this as the final chat model. The model authors describe 1B as useful for specialized applications; stronger results may require a larger model that will be harder to run on the phone.

## Waray text data

- [Sailor2 sea-synthetic / waray](https://huggingface.co/datasets/sailor2/sea-synthetic/tree/main/waray) lists eight JSONL chunks, about 3.0 GB total. The dataset page lists Apache 2.0.
- A 16 KB byte-range sample from `waray/chunk_1.jsonl` was inspected. Rows contain `text` and `meta` fields. The text consists of long generated passages, not user/assistant chat pairs. At least one sampled passage mixes Waray and English and discusses an unrelated setting (fall hayrides). Have speakers review quality and relevance before training on it.
- The full [Sailor2 stage 2 pretraining dataset](https://huggingface.co/datasets/sailor2/sailor2-pretrain-data-stage2) is roughly 282 GB across languages. Downloading all of it is unnecessary for this phone prototype.
- Sailor2 publishes separate instruction-tuning datasets. Their contents and usage terms need inspection before using them for a Waray chat fine-tune.

## What this data cannot do

The sources above are **text**. They do not train speech recognition. Waray speech input needs audio files paired with accurate transcripts and a separate offline speech model.

## Next check

1. Test Sailor2-1B-Chat on a small, speaker-written Waray evaluation set.
2. Review a representative Sailor2 Waray sample with native speakers before selecting training data.
3. Measure the GGUF baseline on an API 28+ phone in airplane mode.
