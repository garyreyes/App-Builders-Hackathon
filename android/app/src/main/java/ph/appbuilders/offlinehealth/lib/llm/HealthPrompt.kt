package ph.appbuilders.offlinehealth.lib.llm

import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.domain.model.TopicCard

/**
 * The system prompt sent with every message: safety rules, the matched topic's checked card (so the model
 * answers from it, not from memory), and the user's language. Wording is exactly what was compared on
 * Oct 10 (10 questions, 4 setups; docs/PROJECT_FACTS.md). Change it only with a re-run of that comparison.
 */
object HealthPrompt {

    fun build(card: TopicCard?, language: Language): String = buildString {
        append(RULES)
        if (card != null) {
            append("\n\nChecked first-aid steps for ${card.title} (follow these, do not contradict them):\n- ")
            append(card.atHome.joinToString("\n- "))
            append("\nGo to the health center now if: ${card.goNowIf.joinToString("; ")}.")
        }
        val name = languageName(language)
        append(" The user chose $name. Reply only in $name.")
    }

    private fun languageName(language: Language) = when (language) {
        Language.WAR -> "Waray (Winaray, the language of Samar and Leyte)"
        Language.CEB -> "Bisaya (Cebuano)"
        Language.TGL -> "Tagalog"
        Language.ENG -> "English"
    }

    private const val RULES =
        "You are an offline first-aid helper for families in the Philippines. " +
            "Always reply in the same language the user writes in (Waray, Bisaya/Cebuano, Tagalog, or English). " +
            "Be short: at most 4 simple steps in plain words. No headings, no markdown. " +
            "Give only safe home first-aid steps. Never name a medicine, never give a dose, never diagnose. " +
            "If anything sounds serious, tell them to go to the nearest health center now. " +
            "The emergency number in the Philippines is 911. If you are not sure, say so and tell them to ask a health worker."
}
