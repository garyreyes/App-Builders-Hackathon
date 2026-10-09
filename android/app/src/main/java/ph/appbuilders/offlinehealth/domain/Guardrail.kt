package ph.appbuilders.offlinehealth.domain

import ph.appbuilders.offlinehealth.domain.model.TopicId

/**
 * Pure filter run on every model reply before any of it reaches the screen (ARCHITECTURE "Send pipeline" step 4).
 * Strict mode (owner, Oct 10): no medicine names, no medicine forms, no doses, no diagnoses, no phone numbers,
 * no emergency number but 911, no known first-aid myths (ice/butter/toothpaste on burns, "no water"), and no
 * downplaying while a danger warning is shown. A rejected reply is hidden; the card and dangers stay.
 */
object Guardrail {
    const val MAX_CHARS = 1200

    fun allows(reply: String, dangerShown: Boolean, topic: TopicId? = null): Boolean {
        val text = reply.lowercase()
        return when {
            text.isBlank() || text.length > MAX_CHARS -> false
            topic == TopicId.BURN && GuardrailTerms.unsafeBurnClaim.containsMatchIn(text) -> false
            GuardrailTerms.dose.containsMatchIn(text) -> false
            GuardrailTerms.drug.containsMatchIn(text) -> false
            GuardrailTerms.diagnosis.containsMatchIn(text) -> false
            GuardrailTerms.phone.containsMatchIn(text) -> false
            GuardrailTerms.otherEmergencyNumber.containsMatchIn(text) -> false
            GuardrailTerms.noWater.containsMatchIn(text) -> false
            GuardrailTerms.myth.findAll(text).any { !negated(text, it.range.first) } -> false
            dangerShown && GuardrailTerms.downplay.containsMatchIn(text) -> false
            else -> true
        }
    }

    /** "Don't put ice on it" is the right advice: a myth only counts when no negation comes just before it. */
    private fun negated(text: String, at: Int): Boolean {
        val sentenceStart = text.lastIndexOfAny(charArrayOf('.', '!', '?', '\n'), startIndex = at - 1) + 1
        val window = text.substring(maxOf(sentenceStart, at - NEGATION_WINDOW), at)
        return GuardrailTerms.negation.containsMatchIn(window)
    }

    private const val NEGATION_WINDOW = 40
}

/**
 * Term lists for [Guardrail], all lowercase, English + Waray + Bisaya + Tagalog. Temporary home: ARCHITECTURE puts
 * these in `guardrail_terms.json` behind the HealthLibrary, which doesn't exist yet (DECISIONS, Oct 10).
 * ORS is deliberately not a drug term: it is the diarrhea card's own first step.
 */
internal object GuardrailTerms {

    private fun words(vararg terms: String) = Regex("""\b(?:${terms.joinToString("|")})\b""")

    val drug = words(
        // Brand and generic names the base model was seen to suggest, plus the common OTC set.
        "paracetamol", "acetaminophen", "biogesic", "tempra", "calpol", "ibuprofen", "advil", "medicol",
        "mefenamic", "ponstan", "aspirin", "amoxicill?in", "antibioti\\w*", "antibiyotik\\w*", "cetirizine",
        "loratadine", "antihistamin\\w*", "antacid\\w*", "kremil\\w*", "omeprazole", "loperamide", "imodium",
        "diatabs", "zinc", "metronidazole", "cotrimoxazole", "salbutamol", "ventolin", "cefalexin",
        "azithromycin", "ciprofloxacin", "prednison\\w*", "dexamethasone", "hydrocortisone", "betadine",
        "povidone", "iodine", "calamine", "mupirocin", "sulfadiazine", "losartan", "amlodipine", "metformin",
        "insulin",
        // Medicine forms: any of these means the reply is giving medicine advice.
        "tablet\\w*", "tableta\\w*", "pills?", "capsules?", "kapsula", "syrup\\w*", "syrop", "suppositor\\w*",
        "injection\\w*", "iniksyon", "ineksyon", "ointment\\w*",
        // Dose talk without a number yet, e.g. a "Dosage" heading before invented mg/kg figures.
        "dosage\\w*", "dosis", "dosing",
    )

    /** A number followed by a dose unit, with the optional Visayan linker: "5 ml", "10mg", "2 ka kutsara". */
    val dose = Regex(
        """\d+(?:[.,/]\d+)?\s*(?:ka\s+|nga\s+)?""" +
            """(?:mg|mcg|g|grams?|ml|cc|kutsara\w*|kutsarita\w*|teaspoons?|tablespoons?|tsp|tbsp|""" +
            """beses|times|drops?|patak|doses?|dosis|tablets?|tableta\w*)\b""",
    )

    val diagnosis = words(
        "diagnos\\w*", "pneumonia", "pulmonya", "typhoid", "tipus", "cholera", "kolera", "tuberculosis", "tb",
        "appendicitis", "meningitis", "leptospirosis", "malaria", "sepsis",
        // Dengue is a topic name, so only "has dengue"-style phrases count as a diagnosis.
        "(?:has|have|had|is|it's|be|may|adunay|mayda|may-ada)\\s+(?:a\\s+)?dengue",
    )

    /** Seven or more digits, optionally split by single spaces or hyphens. 911 stays allowed. */
    val phone = Regex("""\+?\d(?:[\s-]?\d){6,}""")

    /** First-aid myths the stock model was seen to recommend. Allowed when negated ("ayaw butangi hin yelo"). */
    val myth = words(
        "ice", "yelo", "ice packs?", "butter", "mantekilya", "toothpaste", "egg whites?",
        "(?:pop|burst|break|prick)\\s+(?:the\\s+|a\\s+)?blisters?",
    )

    /** Negations in the four languages, looked for just before a myth. */
    val negation = words("don'?t", "do not", "never", "avoid", "not", "no", "ayaw", "dili", "diri", "huwag", "wag", "hindi")

    /** Telling people not to use water on a wound or burn: the opposite of the first step. Spans "e.g." periods. */
    val noWater = Regex("""(?:do not|don'?t|avoid|never)\s+(?:apply|use|put|pour|run|rinse)\b[^!?\n]{0,40}\bwater\b""")

    /** The checked burn card says clean running water for 20 minutes; reject other durations and additives. */
    val unsafeBurnClaim = Regex(
        """\b(?!20\b)\d+\s*(?:ka\s+|nga\s+)?(?:minutes?|minutos?)\b|\b(?:salt|asin|gatas|milk)\b|\b(?:hot water|mainit nga tubig)\b""",
    )

    /** Emergency numbers from other countries, or the old 117 hotline. 911 is the Philippine number. */
    val otherEmergencyNumber = Regex("""\b(?:999|112|111|000|117|118|119)\b""")

    val downplay = Regex(
        listOf(
            "no need to go", "not serious", "nothing to worry", "don'?t worry", "it's normal", "okay ra", "ok ra",
            "normal ra", "normal la", "normal lang", "ayaw kabalaka", "dili seryoso", "diri seryoso",
            "dili kinahanglan", "diri kinahanglan", "hindi kailangan", "hindi seryoso",
        ).joinToString("|"),
    )
}
