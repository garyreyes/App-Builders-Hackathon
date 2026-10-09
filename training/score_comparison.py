"""Summarize independently rated adapted replies in a comparison CSV."""

import argparse
import csv
import json
from pathlib import Path


CRITERIA = ("correctness", "naturalness", "language_choice", "no_invented_facts")


def score(path: Path) -> dict:
    with path.open(encoding="utf-8-sig", newline="") as stream:
        rows = list(csv.DictReader(stream))
    if not rows:
        raise ValueError("comparison CSV is empty")
    required = {"id", "adapted_reply", "adapted_reviewed_by", *(f"adapted_{name}" for name in CRITERIA)}
    missing = required - set(rows[0])
    if missing:
        raise ValueError(f"missing columns: {', '.join(sorted(missing))}")
    counts = {name: 0 for name in CRITERIA}
    passed = []
    failed = []
    for row in rows:
        ratings = {name: row[f"adapted_{name}"].strip().lower() for name in CRITERIA}
        if not row["adapted_reply"].strip() or not row["adapted_reviewed_by"].strip() or any(value not in {"yes", "no"} for value in ratings.values()):
            raise ValueError(f"{row['id']}: adapted reply or rating is missing")
        for name, value in ratings.items():
            counts[name] += value == "yes"
        (passed if all(value == "yes" for value in ratings.values()) else failed).append(row["id"])
    return {"total": len(rows), "all_four_passed": len(passed), "pass_rate": round(len(passed) / len(rows), 4), "criterion_passes": counts, "passed_ids": passed, "failed_ids": failed}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("comparison", type=Path)
    args = parser.parse_args()
    print(json.dumps(score(args.comparison), indent=2))


if __name__ == "__main__":
    main()
