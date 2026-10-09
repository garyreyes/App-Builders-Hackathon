"""Check the data separation and human-review gates without loading a model."""

import csv
import tempfile
import unittest
from pathlib import Path

from local_cli import BASELINE_COLUMNS, DATA_COLUMNS, checked_baseline, reviewed_data


class LocalCliGates(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.root = Path(self.directory.name)
        self.examples = [
            {
                "id": f"item-{index}",
                "split": "train" if index == 0 else "test",
                "user_message": f"Question {index}?",
                "assistant_reply": f"Answer {index}.",
                "reviewed_by": "speaker-1",
                "source": "speaker-written",
                "rights": "permission recorded",
            }
            for index in range(21)
        ]

    def save(self, name, columns, rows):
        path = self.root / name
        with path.open("w", encoding="utf-8", newline="") as stream:
            writer = csv.DictWriter(stream, fieldnames=columns)
            writer.writeheader()
            writer.writerows(rows)
        return path

    def baseline_rows(self):
        return [
            {
                "id": row["id"],
                "user_message": row["user_message"],
                "ideal_reply": row["assistant_reply"],
                "baseline_reply": f"Model answer {row['id']}",
                "correctness": "no" if row["id"] == "item-1" else "yes",
                "naturalness": "yes",
                "language_choice": "yes",
                "no_invented_facts": "yes",
                "reviewed_by": "speaker-1",
            }
            for row in self.examples[1:]
        ]

    def test_reviewed_data_keeps_test_rows_separate(self):
        path = self.save("reviewed.csv", DATA_COLUMNS, self.examples)
        training, test = reviewed_data(path)
        self.assertEqual((len(training), len(test)), (1, 20))
        self.assertFalse({row["id"] for row in training} & {row["id"] for row in test})

    def test_duplicate_prompt_is_rejected(self):
        self.examples[1]["user_message"] = self.examples[0]["user_message"]
        path = self.save("reviewed.csv", DATA_COLUMNS, self.examples)
        with self.assertRaisesRegex(ValueError, "duplicate prompt"):
            reviewed_data(path)

    def test_reviewed_baseline_requires_a_recorded_failure(self):
        rows = self.baseline_rows()
        path = self.save("baseline.csv", BASELINE_COLUMNS, rows)
        self.assertEqual(len(checked_baseline(path, self.examples[1:])), 20)
        rows[0]["correctness"] = "yes"
        path = self.save("baseline.csv", BASELINE_COLUMNS, rows)
        with self.assertRaisesRegex(ValueError, "no recorded failure"):
            checked_baseline(path, self.examples[1:])

    def test_changed_test_prompt_is_rejected(self):
        rows = self.baseline_rows()
        rows[0]["user_message"] = "Changed question"
        path = self.save("baseline.csv", BASELINE_COLUMNS, rows)
        with self.assertRaisesRegex(ValueError, "differs from test example"):
            checked_baseline(path, self.examples[1:])


if __name__ == "__main__":
    unittest.main()
