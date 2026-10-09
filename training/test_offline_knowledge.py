"""Check generality and avoid triggering a fact card for unrelated questions."""

import unittest

from offline_knowledge import answer_known


class OfflineKnowledgeTests(unittest.TestCase):
    def test_capital_question_uses_province(self):
        reply, rule = answer_known("Which town serves as the capital of Biliran province?")
        self.assertEqual(rule, "capital")
        self.assertIn("Naval", reply)

    def test_current_fare_has_no_invented_price(self):
        reply, rule = answer_known("Tagpira an plete han ferry yana?")
        self.assertEqual(rule, "live_fare")
        self.assertIn("offline", reply)
        self.assertNotRegex(reply, r"\d")

    def test_recipe_uses_coconut_milk(self):
        reply, rule = answer_known("How do I cook natong for supper?")
        self.assertEqual(rule, "natong")
        self.assertIn("gata", reply)

    def test_unrelated_water_question_falls_through(self):
        self.assertIsNone(answer_known("Is this river water safe to drink?"))

    def test_sentence_translation_precedes_single_word_lexicon(self):
        reply, rule = answer_known("Translate 'We leave tomorrow' into Waray.")
        self.assertEqual(rule, "leave_tomorrow")
        self.assertIn("Malakat kami buwas", reply)

    def test_noncurrent_weather_question_falls_through(self):
        self.assertIsNone(answer_known("What is a weather forecast?"))

    def test_non_result_lottery_question_falls_through(self):
        self.assertIsNone(answer_known("Ano an lotto?"))

    def test_non_food_moron_question_falls_through(self):
        self.assertIsNone(answer_known("What does the English insult moron mean?"))


if __name__ == "__main__":
    unittest.main()
