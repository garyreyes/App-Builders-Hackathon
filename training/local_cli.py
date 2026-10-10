"""Local, unattended Sailor2 baseline and LoRA training commands.

No model API or Codex calls are made. Data and model artifacts stay on this PC.
Run ``python training/local_cli.py --help`` from the repository root.
"""

import argparse
import csv
import hashlib
import json
import re
import sys
import time
from datetime import datetime, timezone
from importlib.metadata import version
from pathlib import Path


DATA_COLUMNS = ("id", "split", "user_message", "assistant_reply", "reviewed_by", "source", "rights")
RATING_COLUMNS = ("correctness", "naturalness", "language_choice", "no_invented_facts")
BASELINE_COLUMNS = (
    "id", "user_message", "ideal_reply", "baseline_reply", *RATING_COLUMNS, "reviewed_by"
)


def read_csv(path: Path, columns: tuple[str, ...]) -> list[dict[str, str]]:
    with path.open("r", encoding="utf-8-sig", newline="") as stream:
        reader = csv.DictReader(stream)
        missing = set(columns) - set(reader.fieldnames or ())
        if missing:
            raise ValueError(f"{path}: missing columns: {', '.join(sorted(missing))}")
        return [
            {column: (row.get(column) or "").strip() for column in columns}
            for row in reader
        ]


def reviewed_data(path: Path, minimum_test: int = 20) -> tuple[list[dict], list[dict]]:
    rows = read_csv(path, DATA_COLUMNS)
    if not rows:
        raise ValueError(f"{path}: no reviewed examples")
    ids: set[str] = set()
    prompts: set[str] = set()
    groups: dict[str, list[dict]] = {"train": [], "test": []}
    for line, row in enumerate(rows, 2):
        if row["split"] not in groups:
            raise ValueError(f"line {line}: split must be train or test")
        for field in DATA_COLUMNS:
            if not row[field]:
                raise ValueError(f"line {line}: {field} is empty")
        if row["id"] in ids:
            raise ValueError(f"line {line}: duplicate id {row['id']}")
        ids.add(row["id"])
        prompt = re.sub(r"\s+", " ", row["user_message"].casefold()).strip()
        if prompt in prompts:
            raise ValueError(f"line {line}: duplicate prompt across train/test data")
        prompts.add(prompt)
        groups[row["split"]].append(row)
    if not groups["train"]:
        raise ValueError("at least one reviewed training example is required")
    if len(groups["test"]) < minimum_test:
        raise ValueError(f"at least {minimum_test} held-out test examples are required")
    return groups["train"], groups["test"]


def checked_baseline(path: Path, test: list[dict]) -> list[dict]:
    rows = read_csv(path, BASELINE_COLUMNS)
    by_id = {row["id"]: row for row in rows}
    if len(by_id) != len(test) or set(by_id) != {row["id"] for row in test}:
        raise ValueError("baseline review must contain exactly the held-out test IDs")
    failures = 0
    for example in test:
        row = by_id[example["id"]]
        if row["user_message"] != example["user_message"] or row["ideal_reply"] != example["assistant_reply"]:
            raise ValueError(f"baseline review differs from test example {example['id']}")
        if not row["baseline_reply"] or not row["reviewed_by"]:
            raise ValueError(f"baseline reply or reviewer is missing for {example['id']}")
        for rating in RATING_COLUMNS:
            if row[rating].lower() not in {"yes", "no"}:
                raise ValueError(f"{example['id']}: {rating} must be yes or no")
            failures += row[rating].lower() == "no"
    if not failures:
        raise ValueError("baseline has no recorded failure; review whether fine-tuning is needed")
    return rows


def write_csv(path: Path, columns: tuple[str, ...], rows: list[dict]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8", newline="") as stream:
        writer = csv.DictWriter(stream, fieldnames=columns)
        writer.writeheader()
        writer.writerows(rows)


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def gpu() -> None:
    import torch

    if not torch.cuda.is_available():
        raise RuntimeError("CUDA is unavailable; install a CUDA PyTorch build and check the NVIDIA driver")
    print(f"GPU: {torch.cuda.get_device_name(0)} ({torch.cuda.get_device_properties(0).total_memory // 2**20} MiB)", flush=True)


def load_model(model_id: str, quantized: bool = False, tokenizer=None):
    import torch
    from transformers import AutoModelForCausalLM, AutoTokenizer, BitsAndBytesConfig

    if tokenizer is None:
        tokenizer = AutoTokenizer.from_pretrained(model_id)
    options = {"dtype": torch.bfloat16 if torch.cuda.is_bf16_supported() else torch.float16}
    if quantized:
        options["quantization_config"] = BitsAndBytesConfig(
            load_in_4bit=True,
            bnb_4bit_quant_type="nf4",
            bnb_4bit_compute_dtype=options["dtype"],
            bnb_4bit_use_double_quant=True,
        )
        options["device_map"] = {"": 0}
    model = AutoModelForCausalLM.from_pretrained(model_id, **options)
    if not quantized:
        model.to("cuda")
    model.eval()
    return tokenizer, model


def answer(tokenizer, model, prompt: str, max_new_tokens: int) -> tuple[str, float]:
    import torch

    inputs = tokenizer.apply_chat_template(
        [{"role": "user", "content": prompt}],
        add_generation_prompt=True,
        return_tensors="pt",
        return_dict=True,
    ).to("cuda")
    torch.cuda.synchronize()
    started = time.perf_counter()
    with torch.inference_mode():
        result = model.generate(
            **inputs,
            max_new_tokens=max_new_tokens,
            do_sample=False,
            pad_token_id=tokenizer.eos_token_id,
        )
    torch.cuda.synchronize()
    reply = tokenizer.decode(result[0][inputs["input_ids"].shape[-1] :], skip_special_tokens=True).strip()
    return reply, round(time.perf_counter() - started, 2)


def baseline(args, test: list[dict]) -> None:
    if args.output.exists() or args.output.with_suffix(".json").exists():
        raise ValueError(f"baseline output already exists: {args.output}")
    gpu()
    tokenizer, model = load_model(args.model)
    rows = []
    for position, example in enumerate(test, 1):
        reply, seconds = answer(tokenizer, model, example["user_message"], args.max_new_tokens)
        rows.append({
            "id": example["id"],
            "user_message": example["user_message"],
            "ideal_reply": example["assistant_reply"],
            "baseline_reply": reply,
            **{rating: "" for rating in RATING_COLUMNS},
            "reviewed_by": "",
        })
        print(f"Baseline {position}/{len(test)}: {seconds}s", flush=True)
    write_csv(args.output, BASELINE_COLUMNS, rows)
    manifest = {
        "created_at_utc": datetime.now(timezone.utc).isoformat(),
        "model": args.model,
        "model_revision": getattr(model.config, "_commit_hash", None) or "local-or-unresolved",
        "data_sha256": sha256(args.data),
        "test_count": len(test),
        "max_new_tokens": args.max_new_tokens,
        "note": "Desktop language baseline; independent ratings and Android phone test still required",
    }
    args.output.with_suffix(".json").write_text(json.dumps(manifest, indent=2), encoding="utf-8")
    print(f"Review the replies and fill yes/no ratings in {args.output}")


def train(args, train_rows: list[dict], test: list[dict], reviewed: list[dict]) -> None:
    import torch
    from datasets import Dataset
    from peft import LoraConfig, prepare_model_for_kbit_training
    from trl import SFTConfig, SFTTrainer
    from transformers import AutoTokenizer, TrainerCallback

    gpu()
    if args.output.exists():
        raise ValueError(f"training output already exists: {args.output}")
    tokenizer = AutoTokenizer.from_pretrained(args.model)
    too_long = []
    for row in train_rows:
        tokens = tokenizer.apply_chat_template([
            {"role": "user", "content": row["user_message"]},
            {"role": "assistant", "content": row["assistant_reply"]},
        ])
        if len(tokens) > args.max_length:
            too_long.append(row["id"])
    if too_long:
        raise ValueError(f"shorten {len(too_long)} training rows beyond {args.max_length} tokens: {', '.join(too_long[:10])}")
    tokenizer, model = load_model(args.model, quantized=True, tokenizer=tokenizer)
    model_revision = getattr(model.config, "_commit_hash", None) or "local-or-unresolved"
    software = {name: version(name) for name in ("torch", "transformers", "trl", "peft", "bitsandbytes", "datasets")}
    args.output.mkdir(parents=True, exist_ok=False)
    run_file = args.output / "run.json"
    run_file.write_text(json.dumps({
        "status": "running",
        "started_at_utc": datetime.now(timezone.utc).isoformat(),
        "model": args.model,
        "model_revision": model_revision,
        "data_sha256": sha256(args.data),
        "baseline_review_sha256": sha256(args.baseline_review),
        "train_count": len(train_rows),
        "test_count": len(test),
        "gpu": torch.cuda.get_device_name(0),
        "software": software,
        "max_length": args.max_length,
        "max_steps": args.max_steps,
        "epochs": args.epochs,
    }, indent=2), encoding="utf-8")

    class Progress(TrainerCallback):
        def on_log(self, training_args, state, control, logs=None, **kwargs):
            with (args.output / "progress.jsonl").open("a", encoding="utf-8") as stream:
                stream.write(json.dumps({"step": state.global_step, **(logs or {})}, default=str) + "\n")

    model.config.use_cache = False
    model = prepare_model_for_kbit_training(
        model,
        use_gradient_checkpointing=True,
        gradient_checkpointing_kwargs={"use_reentrant": False},
    )
    dataset = Dataset.from_list([
        {
            "prompt": [{"role": "user", "content": row["user_message"]}],
            "completion": [{"role": "assistant", "content": row["assistant_reply"]}],
        }
        for row in train_rows
    ])
    settings = SFTConfig(
        output_dir=str(args.output / "checkpoints"),
        max_length=args.max_length,
        completion_only_loss=True,
        per_device_train_batch_size=1,
        gradient_accumulation_steps=8,
        gradient_checkpointing=True,
        gradient_checkpointing_kwargs={"use_reentrant": False},
        learning_rate=args.learning_rate,
        lr_scheduler_type="cosine",
        warmup_ratio=0.05,
        num_train_epochs=args.epochs,
        max_steps=args.max_steps,
        bf16=torch.cuda.is_bf16_supported(),
        fp16=not torch.cuda.is_bf16_supported(),
        optim="adamw_torch",
        logging_steps=10,
        logging_first_step=True,
        save_strategy="no",
        report_to="none",
        seed=42,
    )
    target_modules = (
        ["q_proj", "k_proj", "v_proj", "o_proj", "gate_proj", "up_proj", "down_proj"]
        if getattr(args, "all_linear", False)
        else ["q_proj", "v_proj"]
    )
    trainer = SFTTrainer(
        model=model,
        args=settings,
        train_dataset=dataset,
        peft_config=LoraConfig(
            r=args.lora_rank, lora_alpha=args.lora_alpha, lora_dropout=0.05,
            target_modules=target_modules,
            bias="none", task_type="CAUSAL_LM",
        ),
        processing_class=tokenizer,
        callbacks=[Progress()],
    )
    started = time.perf_counter()
    result = trainer.train()
    adapter = args.output / "adapter"
    trainer.model.save_pretrained(adapter)
    tokenizer.save_pretrained(adapter)
    trainer.model.eval()
    by_id = {row["id"]: row for row in reviewed}
    comparison = []
    for position, example in enumerate(test, 1):
        reply, seconds = answer(tokenizer, trainer.model, example["user_message"], args.max_new_tokens)
        comparison.append({
            **by_id[example["id"]],
            "adapted_reply": reply,
            "adapted_seconds": seconds,
            "adapted_correctness": "",
            "adapted_naturalness": "",
            "adapted_language_choice": "",
            "adapted_no_invented_facts": "",
            "adapted_reviewed_by": "",
        })
        print(f"Adapted {position}/{len(test)}: {seconds}s", flush=True)
    columns = (*BASELINE_COLUMNS, "adapted_reply", "adapted_seconds", *[f"adapted_{x}" for x in RATING_COLUMNS], "adapted_reviewed_by")
    write_csv(args.output / "comparison.csv", columns, comparison)
    manifest = {
        "status": "completed; awaiting human comparison review",
        "created_at_utc": datetime.now(timezone.utc).isoformat(),
        "model": args.model,
        "model_revision": model_revision,
        "data_sha256": sha256(args.data),
        "baseline_review_sha256": sha256(args.baseline_review),
        "train_count": len(train_rows),
        "test_count": len(test),
        "training_seconds": round(time.perf_counter() - started, 1),
        "train_loss": result.training_loss,
        "gpu": torch.cuda.get_device_name(0),
        "gpu_peak_allocated_mib": round(torch.cuda.max_memory_allocated() / 2**20, 1),
        "torch_version": torch.__version__,
        "software": software,
        "max_length": args.max_length,
        "max_steps": args.max_steps,
        "epochs": args.epochs,
        "learning_rate": args.learning_rate,
        "lr_scheduler_type": "cosine",
        "warmup_ratio": 0.05,
        "lora_rank": args.lora_rank,
        "lora_alpha": args.lora_alpha,
        "target_modules": target_modules,
        "note": "Adapter and comparison need independent review and Android phone testing; AI review does not establish native-speaker quality",
    }
    run_file.write_text(json.dumps(manifest, indent=2), encoding="utf-8")
    print(f"Adapter: {adapter}\nComparison for independent review: {args.output / 'comparison.csv'}")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    commands = parser.add_subparsers(dest="command", required=True)
    for name in ("check", "baseline", "train"):
        command = commands.add_parser(name)
        command.add_argument("--data", type=Path, required=True, help="reviewed chat CSV outside public Git when private")
        if name != "check":
            command.add_argument("--model", default="sail/Sailor2-1B-Chat")
            command.add_argument("--max-new-tokens", type=int, default=128)
            command.add_argument("--output", type=Path, required=True)
        if name == "train":
            command.add_argument("--baseline-review", type=Path, required=True)
            command.add_argument("--max-length", type=int, default=256)
            command.add_argument("--max-steps", type=int, default=-1, help="-1 trains for the configured number of epochs")
            command.add_argument("--epochs", type=int, default=4, help="training epochs when --max-steps is -1")
            command.add_argument("--learning-rate", type=float, default=2e-4)
            command.add_argument("--lora-rank", type=int, default=16)
            command.add_argument("--lora-alpha", type=int, default=32)
            command.add_argument("--all-linear", action="store_true", help="target all linear projection layers")
    args = parser.parse_args()
    try:
        train_rows, test = reviewed_data(args.data)
        print(f"Validated {len(train_rows)} reviewed train and {len(test)} held-out test examples")
        if args.command == "baseline":
            baseline(args, test)
        elif args.command == "train":
            reviewed = checked_baseline(args.baseline_review, test)
            train(args, train_rows, test, reviewed)
    except (OSError, ValueError, RuntimeError) as error:
        print(f"Error: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
