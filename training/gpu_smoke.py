"""Check that the untouched Sailor2 chat model runs on this computer's CUDA GPU.

This is an inference smoke check, not a Waray quality evaluation or training run.
The model stays in the Hugging Face cache; the report goes under ignored outputs/.
"""

import argparse
import json
import time
from datetime import datetime, timezone
from pathlib import Path

import torch
from huggingface_hub import model_info
from transformers import AutoModelForCausalLM, AutoTokenizer


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--model", default="sail/Sailor2-1B-Chat")
    parser.add_argument("--prompt", default="Hello. Please reply in one sentence.")
    parser.add_argument("--max-new-tokens", type=int, default=48)
    parser.add_argument("--output", type=Path, default=Path("outputs/gpu-smoke.json"))
    args = parser.parse_args()

    if not torch.cuda.is_available():
        raise SystemExit("CUDA is unavailable in this Python environment")

    torch.cuda.reset_peak_memory_stats()
    started = time.perf_counter()
    tokenizer = AutoTokenizer.from_pretrained(args.model)
    model = AutoModelForCausalLM.from_pretrained(args.model, dtype=torch.float16)
    model.to("cuda").eval()
    loaded_seconds = time.perf_counter() - started

    messages = [{"role": "user", "content": args.prompt}]
    inputs = tokenizer.apply_chat_template(
        messages, add_generation_prompt=True, return_tensors="pt"
    ).to("cuda")
    torch.cuda.synchronize()
    generation_started = time.perf_counter()
    with torch.inference_mode():
        tokens = model.generate(
            inputs,
            max_new_tokens=args.max_new_tokens,
            do_sample=False,
            pad_token_id=tokenizer.eos_token_id,
        )
    torch.cuda.synchronize()
    generation_seconds = time.perf_counter() - generation_started
    new_tokens = tokens[0][inputs.shape[-1] :]

    result = {
        "checked_at_utc": datetime.now(timezone.utc).isoformat(),
        "model": args.model,
        "model_revision": model_info(args.model).sha,
        "torch_version": torch.__version__,
        "gpu": torch.cuda.get_device_name(0),
        "gpu_peak_allocated_mib": round(torch.cuda.max_memory_allocated() / 2**20, 1),
        "load_seconds": round(loaded_seconds, 2),
        "generation_seconds": round(generation_seconds, 2),
        "generated_tokens": int(new_tokens.numel()),
        "prompt": args.prompt,
        "reply": tokenizer.decode(new_tokens, skip_special_tokens=True),
        "purpose": "GPU inference smoke check; not a Waray evaluation",
    }
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps(result, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
