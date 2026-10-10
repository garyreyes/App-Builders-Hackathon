"""Generate a fresh held-out comparison from an existing local LoRA adapter."""

import argparse
from pathlib import Path

from peft import PeftModel

from local_cli import (
    BASELINE_COLUMNS, RATING_COLUMNS, answer, checked_baseline, gpu,
    load_model, reviewed_data, write_csv,
)
from offline_knowledge import answer_known


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--data", type=Path, required=True)
    parser.add_argument("--baseline-review", type=Path, required=True)
    parser.add_argument("--adapter", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--model", default="sail/Sailor2-1B-Chat")
    parser.add_argument("--max-new-tokens", type=int, default=128)
    parser.add_argument("--knowledge", action="store_true", help="use the offline fact and policy layer before model fallback")
    args = parser.parse_args()
    if args.output.exists():
        raise ValueError(f"output already exists: {args.output}")
    _, test = reviewed_data(args.data)
    reviewed = checked_baseline(args.baseline_review, test)
    by_id = {row["id"]: row for row in reviewed}
    gpu()
    tokenizer, base = load_model(args.model, quantized=True)
    model = PeftModel.from_pretrained(base, args.adapter)
    model.eval()
    rows = []
    for position, example in enumerate(test, 1):
        known = answer_known(example["user_message"]) if args.knowledge else None
        if known:
            reply, rule = known
            seconds = 0.0
            source = f"offline_knowledge:{rule}"
        else:
            reply, seconds = answer(tokenizer, model, example["user_message"], args.max_new_tokens)
            source = "sailor2_lora"
        rows.append({
            **by_id[example["id"]],
            "adapted_reply": reply,
            "adapted_seconds": seconds,
            **{f"adapted_{name}": "" for name in RATING_COLUMNS},
            "adapted_reviewed_by": "",
            "reply_source": source,
        })
        print(f"Adapted {position}/{len(test)}: {seconds}s", flush=True)
    columns = (*BASELINE_COLUMNS, "adapted_reply", "adapted_seconds", *[f"adapted_{name}" for name in RATING_COLUMNS], "adapted_reviewed_by", "reply_source")
    write_csv(args.output, columns, rows)
    print(f"Comparison for independent review: {args.output}")


if __name__ == "__main__":
    main()
