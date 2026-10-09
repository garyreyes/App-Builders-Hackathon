package ph.appbuilders.offlinehealth.fakes

import ph.appbuilders.offlinehealth.app.UiKey
import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.domain.model.TopicId

// PLACEHOLDER COPY. Every string here comes from the design canvas and handover §8. None of it has been
// reviewed by a native speaker or a clinician. The teammate's ui_strings.json and topic JSON replace all of it.
// Bisaya appears only where the canvas wrote it. Any other key falls back to English (no invented translations).

/** Each language describing itself (endonyms). Real for all 4 languages, since the picker needs them. */
internal val LanguageSelf: Map<Language, Map<String, String>> = mapOf(
    Language.CEB to selfOf("Binisaya", "Cebuano", "BIS", "Pinulongan"),
    Language.WAR to selfOf("Winaray", "Waray", "WAR", "Yinaknan"),
    Language.TGL to selfOf("Tagalog / Taglish", "Filipino", "TL", "Wika"),
    Language.ENG to selfOf("English", "", "EN", "Language"),
)

private fun selfOf(name: String, alias: String, code: String, word: String) = mapOf(
    UiKey.LANGUAGE_NAME to name, UiKey.LANGUAGE_ALIAS to alias,
    UiKey.LANGUAGE_CODE to code, UiKey.LANGUAGE_WORD to word,
)

internal val EnglishUi: Map<String, String> = mapOf(
    UiKey.TOPBAR_STATUS_READY to "Offline",
    UiKey.TOPBAR_STATUS_STARTING to "AI starting…",
    UiKey.TOPBAR_STATUS_BASIC to "Basic mode",
    UiKey.TOPBAR_STATUS_READY_DESC to "Status: offline, AI ready. Open AI settings",
    UiKey.TOPBAR_STATUS_STARTING_DESC to "Status: AI starting. Open AI settings",
    UiKey.TOPBAR_STATUS_BASIC_DESC to "Status: basic mode, no AI. Open AI settings",
    UiKey.TOPBAR_LANGUAGE_DESC to "Language: {language}. Tap to change",
    UiKey.TOPBAR_MENU_DESC to "Settings and topics",
    UiKey.MENU_TOPICS to "First-aid topics",
    UiKey.MENU_SETTINGS to "Settings",
    UiKey.COMPOSER_DISCLAIMER to "Not a doctor. For emergencies, go to the health center.",
    UiKey.COMPOSER_PLACEHOLDER to "Type here…",
    UiKey.COMPOSER_FIELD_DESC to "Your message",
    UiKey.COMPOSER_SEND to "Send",
    UiKey.DANGER_HEADLINE to "Go to the health center NOW.",
    UiKey.DANGER_CALL to "Call 911 if you can",
    UiKey.CARD_KICKER to "First aid",
    UiKey.CARD_HOME to "What to do at home",
    UiKey.CARD_GO_NOW to "Go now if…",
    UiKey.CARD_YOU_SAID to "you said this",
    UiKey.AI_LABEL to "AI helper · follow the card above",
    UiKey.AI_THINKING to "AI is thinking…",
    UiKey.AI_WARMING to "The AI is still starting. Its reply will follow.",
    UiKey.AI_WITHHELD to "Follow the card above.",
    UiKey.AI_BASIC to "Basic mode: no AI reply. Danger checks and first-aid cards still work.",
    UiKey.AI_BASIC_CTA to "Download the AI",
)

internal val BisayaUi: Map<String, String> = mapOf(
    UiKey.COMPOSER_DISCLAIMER to "Dili kini doktor. Kung emerhensya, adto sa health center.",
    UiKey.COMPOSER_PLACEHOLDER to "Isulat dinhi…",
    UiKey.COMPOSER_FIELD_DESC to "Imong mensahe",
    UiKey.COMPOSER_SEND to "Ipadala",
    UiKey.DANGER_HEADLINE to "Adto dayon sa health center KARON.",
    UiKey.DANGER_CALL to "Tawag sa 911 kung mahimo",
    UiKey.CARD_KICKER to "Unang tabang",
    UiKey.CARD_HOME to "Unsa ang buhaton sa balay",
    UiKey.CARD_GO_NOW to "Adto dayon kung…",
    UiKey.CARD_YOU_SAID to "imong gisulti",
    UiKey.AI_LABEL to "AI helper · sunda ang card sa ibabaw",
    UiKey.AI_THINKING to "Naghunahuna ang AI…",
    UiKey.AI_WARMING to "Nagsugod pa ang AI. Moabot ang tubag human niini.",
    UiKey.AI_WITHHELD to "Sunda ang card sa ibabaw.",
    UiKey.AI_BASIC to "Basic mode: walay tubag gikan sa AI. Mogana gihapon ang mga pasidaan ug first-aid card.",
    UiKey.AI_BASIC_CTA to "I-download ang AI",
)

internal val EnglishTitles: Map<TopicId, String> = mapOf(
    TopicId.CHILD_DIARRHEA to "Child diarrhea",
    TopicId.FEVER to "Fever",
    TopicId.COUGH_BREATHING to "Cough & hard breathing",
    TopicId.WOUND_BLEEDING to "Wounds & bleeding",
    TopicId.BURN to "Burns",
    TopicId.PREGNANCY_WARNING to "Pregnancy warning signs",
    TopicId.DENGUE_WARNING to "Dengue warning signs",
)

internal val BisayaTitles: Map<TopicId, String> = mapOf(
    TopicId.CHILD_DIARRHEA to "Kalibanga sa bata",
    TopicId.FEVER to "Hilanat",
    TopicId.COUGH_BREATHING to "Ubo ug lisod nga pagginhawa",
    TopicId.WOUND_BLEEDING to "Samad ug pagdugo",
    TopicId.BURN to "Paso",
    TopicId.PREGNANCY_WARNING to "Peligro sa pagmabdos",
    TopicId.DENGUE_WARNING to "Peligro sa dengue",
)

/** The one full card (child diarrhea), from FirstAidCard.dc.html. */
internal class FakeCardText(val atHome: List<String>, val goNowIf: List<String>, val source: String)

internal val EnglishDiarrhea = FakeCardText(
    atHome = listOf("Give more fluids than usual.", "Give ORS after each loose stool.", "Keep breastfeeding and feeding."),
    goNowIf = listOf(
        "can’t drink or breastfeed", "vomits everything", "blood in the stool",
        "very sleepy or hard to wake", "sunken eyes",
    ),
    source = "Based on DOH/WHO guidance on childhood diarrhea.",
)

internal val BisayaDiarrhea = FakeCardText(
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
)

/** Every other topic: an obviously unfinished card until the content team writes it. */
internal val PlaceholderCard = FakeCardText(
    atHome = listOf("[Placeholder] The content team writes this card’s steps."),
    goNowIf = listOf("[Placeholder] The content team writes this card’s danger signs."),
    source = "[Placeholder] Source not cited yet.",
)
