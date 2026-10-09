package ph.appbuilders.offlinehealth.content

import ph.appbuilders.offlinehealth.domain.model.DangerSignId
import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.domain.model.TopicId

/**
 * The demo's Health Library: the three topics with written cards (diarrhea, burn, fever) until the content JSON
 * exists. No medicine names or doses, by the same rule as the guardrail.
 * English: from DOH/WHO first-aid guidance. Waray and Bisaya for burn/fever, and Waray for diarrhea, are
 * AI-drafted and NOT native-speaker reviewed (the source line says so). Tagalog falls back to English.
 */
internal class CardText(val atHome: List<String>, val goNowIf: List<String>, val source: String)

internal object DemoCards {

    val topics = listOf(TopicId.CHILD_DIARRHEA, TopicId.FEVER, TopicId.BURN)

    fun text(topic: TopicId, language: Language): CardText? = cards[topic]?.let { it[language] ?: it[Language.ENG] }

    /** Where each danger sign sits in a card's "Go now if" list (same order in every language), to highlight it. */
    fun signIndex(topic: TopicId, sign: DangerSignId): Int? = signOrder[topic]?.indexOf(sign)?.takeIf { it >= 0 }

    val warayTitles = mapOf(
        TopicId.CHILD_DIARRHEA to "Pagkalibang han bata",
        TopicId.FEVER to "Hilanat",
        TopicId.BURN to "Paso",
    )

    fun dangerMessage(sign: DangerSignId, language: Language): String =
        dangerMessages[sign]?.let { it[language] ?: it[Language.ENG] } ?: sign.name

    private const val UNREVIEWED_WAR = " Waray text not yet reviewed by a native speaker."
    private const val UNREVIEWED_CEB = " Binisaya text not yet reviewed by a native speaker."

    private val signOrder = mapOf(
        TopicId.CHILD_DIARRHEA to listOf(
            DangerSignId.CANNOT_DRINK, DangerSignId.VOMITS_EVERYTHING, DangerSignId.BLOOD_IN_STOOL,
            DangerSignId.VERY_SLEEPY, null,
        ),
        TopicId.FEVER to listOf(
            DangerSignId.SEIZURE, DangerSignId.VERY_SLEEPY, null, DangerSignId.DIFFICULTY_BREATHING, null, null, null,
        ),
        TopicId.BURN to listOf(null, null, null, null, DangerSignId.DIFFICULTY_BREATHING, null),
    )

    private val cards: Map<TopicId, Map<Language, CardText>> = mapOf(
        TopicId.CHILD_DIARRHEA to mapOf(
            Language.ENG to CardText(
                atHome = listOf("Give more fluids than usual.", "Give ORS after each loose stool.", "Keep breastfeeding and feeding."),
                goNowIf = listOf(
                    "can’t drink or breastfeed", "vomits everything", "blood in the stool",
                    "very sleepy or hard to wake", "sunken eyes",
                ),
                source = "Based on DOH/WHO guidance on childhood diarrhea.",
            ),
            Language.CEB to CardText(
                atHome = listOf(
                    "Hatagi og mas daghang ilimnon kaysa kasagaran.",
                    "Hatagi og ORS matag human sa iyang pagkalibang.",
                    "Padayona ang pagpasuso ug pagpakaon.",
                ),
                goNowIf = listOf(
                    "dili makainom o makasuso", "nagsuka sa tanan", "adunay dugo sa tae",
                    "hilabihan ka katulgon o lisod pukawon", "nalubong ang mga mata",
                ),
                source = "Gibase sa giya sa DOH/WHO bahin sa kalibanga sa bata.",
            ),
            Language.WAR to CardText(
                atHome = listOf(
                    "Hatagi hin mas damo nga iinumon kaysa han naaanad.",
                    "Hatagi hin ORS kada kalibang.",
                    "Padayona an pagpasuso ngan pagpakaon.",
                ),
                goNowIf = listOf(
                    "diri makainom o makasuso", "isinusuka an ngatanan", "may-ada dugo an tae",
                    "maturog-turogon hin duro o makuri pukawon", "lubong an mga mata",
                ),
                source = "Based on DOH/WHO guidance on childhood diarrhea.$UNREVIEWED_WAR",
            ),
        ),
        TopicId.BURN to mapOf(
            Language.ENG to CardText(
                atHome = listOf(
                    "Cool the burn under clean running water for 20 minutes.",
                    "Remove rings, watches, and tight clothing near the burn.",
                    "Cover it loosely with a clean cloth or plastic wrap.",
                    "Don’t put ice, toothpaste, oil, or butter on it.",
                ),
                goNowIf = listOf(
                    "bigger than the person’s palm", "on the face, hands, feet, private parts, or a joint",
                    "from electricity or a chemical", "a baby, an older person, or a pregnant woman",
                    "trouble breathing after smoke or fire", "deep: white, brown, or black skin, or no pain",
                ),
                source = "Based on WHO first-aid guidance for burns.",
            ),
            Language.CEB to CardText(
                atHome = listOf(
                    "Paagasi og limpyo nga tubig ang paso sulod sa 20 ka minuto.",
                    "Kuhaa ang singsing, relo, ug hugot nga sinina duol sa paso.",
                    "Taboni og limpyo nga panapton o plastik.",
                    "Ayaw butangi og yelo, toothpaste, mantika, o mantekilya.",
                ),
                goNowIf = listOf(
                    "mas dako pa sa iyang palad", "sa nawong, kamot, tiil, kinatawo, o lutahan",
                    "gikan sa kuryente o kemikal", "bata, tigulang, o mabdos",
                    "lisod moginhawa human sa aso o sunog", "lawom: puti, brown, o itom nga panit, o walay sakit",
                ),
                source = "Gibase sa giya sa WHO bahin sa first aid sa paso.$UNREVIEWED_CEB",
            ),
            Language.WAR to CardText(
                atHome = listOf(
                    "Buhusi hin malinis nga nagdadalagan nga tubig an paso ha sulod hin 20 ka minuto.",
                    "Kuhaa an singsing, relo, ngan hugot nga bado harani ha paso.",
                    "Tabuni hin malinis nga panapton o plastik.",
                    "Ayaw butangi hin yelo, toothpaste, mantika, o mantekilya.",
                ),
                goNowIf = listOf(
                    "mas dako pa kaysa han iya palad", "ha bayhon, kamot, tiil, kinatawo, o lutahan",
                    "tikang ha kuryente o kemikal", "bata, lagas, o nagbuburod",
                    "makuri gumhawa kahuman han aso o sunog", "hilarom: busag, brown, o itom nga panit, o waray kasakit",
                ),
                source = "Based on WHO first-aid guidance for burns.$UNREVIEWED_WAR",
            ),
        ),
        TopicId.FEVER to mapOf(
            Language.ENG to CardText(
                atHome = listOf(
                    "Give plenty of fluids, a little at a time and often.",
                    "Let them rest in light clothing.",
                    "Wipe the skin with a cloth dipped in lukewarm water, not cold.",
                    "Keep breastfeeding a baby with fever.",
                ),
                goNowIf = listOf(
                    "seizure (the body is shaking)", "very sleepy or hard to wake", "stiff neck", "trouble breathing",
                    "bleeding gums or nose, or red spots on the skin", "fever for more than 2 days",
                    "a baby under 2 months with any fever",
                ),
                source = "Based on DOH/WHO guidance on fever and dengue warning signs.",
            ),
            Language.CEB to CardText(
                atHome = listOf(
                    "Hatagi og daghang ilimnon, gamay pero kanunay.",
                    "Papahuwaya siya ug pasul-uba og nipis nga sinina.",
                    "Punasi ang panit og panapton nga gituslob sa dili bugnaw nga tubig.",
                    "Padayona ang pagpasuso sa bata nga gihilantan.",
                ),
                goNowIf = listOf(
                    "kumbulsyon (nagkurog ang lawas)", "hilabihan ka katulgon o lisod pukawon", "gahi ang liog",
                    "lisod moginhawa", "nagdugo ang lagos o ilong, o pula nga mga tuldok sa panit",
                    "hilanat nga molapas sa 2 ka adlaw", "bata nga ubos sa 2 ka bulan nga adunay hilanat",
                ),
                source = "Gibase sa giya sa DOH/WHO bahin sa hilanat ug dengue.$UNREVIEWED_CEB",
            ),
            Language.WAR to CardText(
                atHome = listOf(
                    "Hatagi hin damo nga iinumon, gutiay pero pirme.",
                    "Papahuwaya hiya ngan pagsul-ota hin manipis nga bado.",
                    "Pahiri an panit hin panapton nga gin-unlop ha diri matugnaw nga tubig.",
                    "Padayona an pagpasuso han bata nga may hilanat.",
                ),
                goNowIf = listOf(
                    "kumbulsyon (nangangurog an lawas)", "maturog-turogon hin duro o makuri pukawon", "matig-a an liog",
                    "makuri gumhawa", "nagdudugo an lagos o irong, o may pula nga mga tuldok ha panit",
                    "hilanat nga sobra 2 ka adlaw", "bata nga ubos 2 ka bulan nga may hilanat",
                ),
                source = "Based on DOH/WHO guidance on fever and dengue warning signs.$UNREVIEWED_WAR",
            ),
        ),
    )

    private val dangerMessages: Map<DangerSignId, Map<Language, String>> = mapOf(
        DangerSignId.CANNOT_DRINK to mapOf(
            Language.ENG to "Can’t drink or breastfeed.",
            Language.CEB to "Dili makainom o makasuso.",
            Language.WAR to "Diri makainom o makasuso.",
        ),
        DangerSignId.VOMITS_EVERYTHING to mapOf(
            Language.ENG to "Vomits everything.",
            Language.CEB to "Nagsuka sa tanan.",
            Language.WAR to "Isinusuka an ngatanan.",
        ),
        DangerSignId.BLOOD_IN_STOOL to mapOf(
            Language.ENG to "Blood in the stool.",
            Language.CEB to "Adunay dugo sa tae.",
            Language.WAR to "May-ada dugo an tae.",
        ),
        DangerSignId.VERY_SLEEPY to mapOf(
            Language.ENG to "Very sleepy or hard to wake.",
            Language.CEB to "Hilabihan ka katulgon o lisod pukawon.",
            Language.WAR to "Maturog-turogon hin duro o makuri pukawon.",
        ),
        DangerSignId.SEIZURE to mapOf(
            Language.ENG to "Seizure (the body is shaking).",
            Language.CEB to "Kumbulsyon (pagkurog sa lawas).",
            Language.WAR to "Kumbulsyon (nangangurog an lawas).",
        ),
        DangerSignId.DIFFICULTY_BREATHING to mapOf(
            Language.ENG to "Trouble breathing.",
            Language.CEB to "Lisod moginhawa.",
            Language.WAR to "Makuri gumhawa.",
        ),
        DangerSignId.UNCONSCIOUS to mapOf(
            Language.ENG to "Unconscious or fainted.",
            Language.CEB to "Nawad-an og panimuot.",
            Language.WAR to "Nawara an panimuot.",
        ),
    )
}
