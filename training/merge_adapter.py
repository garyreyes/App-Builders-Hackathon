"""Merge a trained LoRA adapter into the base Sailor2 model.

Produces a standard Hugging Face model folder with merged weights in float16,
ready for conversion to GGUF format via llama.cpp.
"""

import argparse
import time
from pathlib import Path

import torch
from peft import PeftModel
from transformers import AutoModelForCausalLM, AutoTokenizer


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base-model", default="sail/Sailor2-1B-Chat", help="Hugging Face repo or local path")
    parser.add_argument("--adapter", type=Path, required=True, help="Path to LoRA adapter folder")
    parser.add_argument("--output", type=Path, required=True, help="Destination folder for merged model")
    args = parser.parse_args()

    if not args.adapter.exists():
        raise SystemExit(f"Adapter not found: {args.adapter}")

    print(f"Loading tokenizer from {args.adapter}...", flush=True)
    tokenizer = AutoTokenizer.from_pretrained(args.adapter)

    device = "cuda" if torch.cuda.is_available() else "cpu"
    dtype = torch.float16
    print(f"Loading base model '{args.base_model}' on {device} ({dtype})...", flush=True)
    started = time.perf_counter()
    base_model = AutoModelForCausalLM.from_pretrained(
        args.base_model,
        torch_dtype=dtype,
        device_map=device,
    )
    print(f"Base model loaded in {time.perf_counter() - started:.1f}s", flush=True)

    print(f"Loading adapter from {args.adapter}...", flush=True)
    model = PeftModel.from_pretrained(base_model, str(args.adapter))

    print("Merging LoRA weights into base model...", flush=True)
    merge_start = time.perf_counter()
    merged_model = model.merge_and_unload()
    print(f"Merged in {time.perf_counter() - merge_start:.1f}s", flush=True)

    args.output.mkdir(parents=True, exist_ok=True)
    print(f"Saving merged model to {args.output}...", flush=True)
    save_start = time.perf_counter()
    merged_model.save_pretrained(args.output, safe_serialization=True)
    tokenizer.save_pretrained(args.output)
    print(f"Saved merged model in {time.perf_counter() - save_start:.1f}s", flush=True)

    # Verification prompt
    test_prompt = "Maupay nga adlaw ha imo!"
    inputs = tokenizer.apply_chat_template(
        [{"role": "user", "content": test_prompt}],
        add_generation_prompt=True,
        return_tensors="pt",
        return_dict=True,
    ).to(device)
    with torch.inference_mode():
        out = merged_model.generate(**inputs, max_new_tokens=48, do_sample=False)
    reply = tokenizer.decode(out[0][inputs["input_ids"].shape[-1]:], skip_special_tokens=True).strip()
    print(f"\nVerification test prompt: '{test_prompt}'")
    print(f"Verification reply: '{reply}'\n")
    print(f"Successfully created merged model at: {args.output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
