package ph.appbuilders.offlinehealth.domain

import ph.appbuilders.offlinehealth.domain.model.DangerSignId
import ph.appbuilders.offlinehealth.domain.model.TopicId

/** Ranked topics (most mentions first) and the danger signs found in one message. */
data class TriageMatch(val topics: List<TopicId>, val dangers: List<DangerSignId>)

/**
 * Keyword triage (ARCHITECTURE "Send pipeline" step 1): the glossary is the router, the model never classifies.
 * Pure and synchronous. Every language's words are checked whatever the app language, since people code-switch.
 */
object Triage {

    fun match(text: String): TriageMatch {
        val normalized = text.lowercase().replace('’', '\'').replace('‘', '\'')
        val topics = Glossary.topics
            .map { (topic, words) -> topic to words.findAll(normalized).count() }
            .filter { (_, hits) -> hits > 0 }
            .sortedByDescending { (_, hits) -> hits } // stable: ties keep the glossary order
            .map { (topic, _) -> topic }
        val dangers = Glossary.dangers
            .filter { (sign, words) -> words.containsMatchIn(normalized) && inContext(sign, normalized, topics) }
            .map { (sign, _) -> sign }
        return TriageMatch(topics, dangers)
    }

    /** "Dugo"/"blood" alone is too broad (a cut, a nosebleed): it is a stool danger only around diarrhea. */
    private fun inContext(sign: DangerSignId, text: String, topics: List<TopicId>): Boolean =
        sign != DangerSignId.BLOOD_IN_STOOL || TopicId.CHILD_DIARRHEA in topics || Glossary.stool.containsMatchIn(text)
}

/**
 * Temporary home for the glossary until `glossary.json` exists (ARCHITECTURE "Health Library"). Only the three
 * topics with written cards are routed; everything else gets the "not covered" answer.
 * Words are lowercase regexes. Waray/Bisaya/Tagalog entries are AI-drafted and not native-reviewed.
 */
internal object Glossary {

    private fun words(vararg terms: String) = Regex("""\b(?:${terms.joinToString("|")})""")

    val topics: List<Pair<TopicId, Regex>> = listOf(
        TopicId.CHILD_DIARRHEA to words(
            "diarr?h?o?ea\\w*", "loose (?:stool|bowel)s?", "watery stools?", "lbm\\b",
            "\\w*kalibang\\w*", "\\w*tatae\\w*",
        ),
        TopicId.BURN to words(
            "burn\\w*", "scald\\w*", "\\w*paso\\b", "\\w*pasuan\\b", "\\w*sunog\\b", "nasusunog",
        ),
        TopicId.FEVER to words(
            "fever\\w*", "high temperature", "\\w*hilanat\\w*", "\\w*hilantan\\w*", "\\w*lagnat\\w*",
        ),
    )

    val dangers: List<Pair<DangerSignId, Regex>> = listOf(
        DangerSignId.CANNOT_DRINK to words(
            "(?:can't|cannot|can not|won't|unable to) (?:drink|breastfeed|feed|suck)",
            "(?:dili|diri|di|hindi) (?:maka|mo|ma)?(?:inom|suso)\\w*", "ayaw (?:mo)?inom",
        ),
        DangerSignId.VOMITS_EVERYTHING to words(
            "vomit\\w*", "throwing up", "threw up", "nagsuka\\w*", "nasuka\\w*", "nagsusuka\\w*", "isinusuka\\w*",
            "gisuka\\w*", "sumusuka\\w*", "nagasuka\\w*",
        ),
        DangerSignId.BLOOD_IN_STOOL to words("blood\\w*", "dugo\\w*"),
        DangerSignId.VERY_SLEEPY to words(
            "sleepy", "drowsy", "lethargic", "hard to wake", "won't wake", "can't wake", "katulgon",
            "lisod (?:pukawon|mamata)", "makuri (?:pukawon|pagmata)", "antok na antok", "hirap gisingin",
        ),
        DangerSignId.SEIZURE to words(
            "seizure\\w*", "convuls\\w*", "k[ou]mbulsi?yon\\w*", "kinombulsyon", "nangisay",
        ),
        DangerSignId.DIFFICULTY_BREATHING to words(
            "(?:can't|cannot|hard to|trouble|difficulty|struggling to) breath\\w*", "short(?:ness)? of breath",
            "lisod (?:mo)?ginhawa", "makuri (?:gumhawa|ginhawa)", "hirap (?:huminga|sa paghinga)", "hingal\\w*",
        ),
        DangerSignId.UNCONSCIOUS to words(
            "unconscious", "fainted", "passed out", "nahimatay", "\\w*himatay\\w*", "nawad-an (?:og )?panimuot",
            "walang malay", "nawara an panimuot",
        ),
    )

    val stool = words("stool\\w*", "poop\\w*", "tae\\b", "tai\\b")
}
