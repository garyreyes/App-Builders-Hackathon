package ph.appbuilders.offlinehealth.fakes

import ph.appbuilders.offlinehealth.app.UiKey
import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.domain.model.TopicId

// PLACEHOLDER COPY. Every string here comes from the design canvas and handover §8.
// The teammate's ui_strings.json and topic JSON replace all of it.
// Bisaya appears only where the canvas wrote it. Any other key falls back to English (no invented translations).

/** Each language describing itself (endonyms). Real for all 4 languages, since the picker needs them. */
internal val LanguageSelf: Map<Language, Map<String, String>> = mapOf(
    Language.CEB to selfOf("Binisaya", "Cebuano", "BIS", "Pinulongan", "Pili-a ang imong pinulongan"),
    Language.WAR to selfOf("Winaray", "Waray", "WAR", "Yinaknan", "Pilia an imo yinaknan"),
    Language.TGL to selfOf("Tagalog / Taglish", "Filipino", "TL", "Wika", "Piliin ang iyong wika"),
    Language.ENG to selfOf("English", "", "EN", "Language", "Choose your language"),
)

private fun selfOf(name: String, alias: String, code: String, word: String, choose: String) = mapOf(
    UiKey.LANGUAGE_NAME to name, UiKey.LANGUAGE_ALIAS to alias,
    UiKey.LANGUAGE_CODE to code, UiKey.LANGUAGE_WORD to word, UiKey.LANGUAGE_CHOOSE to choose,
)

/** Language picker, setup, topics, and settings. The canvas wrote these in English only. */
private val EnglishScreensUi: Map<String, String> = mapOf(
    UiKey.LANGUAGE_CHANGE_LATER to "You can change this anytime.",
    UiKey.NAV_BACK to "Back",
    UiKey.NAV_BACK_TO_TOPICS to "Back to topics",
    UiKey.NAV_TOPICS to "Topics",
    UiKey.SETUP_INTRO_TITLE to "Get the AI helper",
    UiKey.SETUP_INTRO_BODY to "Download once on Wi-Fi. After that, it works with no signal.",
    UiKey.SETUP_INTRO_SIZE to "{size}, one time only",
    UiKey.SETUP_INTRO_OFFLINE to "Works offline after setup",
    UiKey.SETUP_INTRO_PRIVATE to "Everything stays on your phone",
    UiKey.SETUP_DOWNLOAD to "Download AI · {size}",
    UiKey.SETUP_BASIC to "Use basic mode for now",
    UiKey.SETUP_BASIC_NOTE to "Danger checks and first-aid cards work right away.",
    UiKey.SETUP_MB to "{mb} MB",
    UiKey.SETUP_DOWNLOADING_TITLE to "Downloading the AI helper",
    UiKey.SETUP_DOWNLOADING_BODY to "You can leave the app. The download keeps going.",
    UiKey.SETUP_PROGRESS to "{done} of {total} MB",
    UiKey.SETUP_PROGRESS_DESC to "Download progress",
    UiKey.SETUP_KEEP_WIFI to "Keep Wi-Fi on until it finishes.",
    UiKey.SETUP_CANCEL to "Cancel download",
    UiKey.SETUP_VERIFYING_TITLE to "Checking the file…",
    UiKey.SETUP_VERIFYING_BODY to "This takes a few seconds.",
    UiKey.SETUP_DONE_TITLE to "Ready.",
    UiKey.SETUP_DONE_BODY to "Works without internet now.",
    UiKey.SETUP_START to "Start",
    UiKey.SETUP_NO_INTERNET_TITLE to "No internet right now",
    UiKey.SETUP_NO_INTERNET_BODY to
        "Connect to Wi-Fi or mobile data once to download the AI. Basic mode works without it.",
    UiKey.SETUP_TRY_AGAIN to "Try again",
    UiKey.SETUP_STORAGE_TITLE to "Not enough space",
    UiKey.SETUP_STORAGE_BODY to "Remove some videos, photos, or apps, then try again.",
    UiKey.SETUP_STORAGE_NEEDED to "Needed",
    UiKey.SETUP_STORAGE_FREE to "Free on this phone",
    UiKey.SETUP_STORAGE_MORE to "Free up {mb} MB more.",
    UiKey.SETUP_FAILED_TITLE to "Download stopped",
    UiKey.SETUP_FAILED_BODY to "The connection dropped at {done} of {total} MB. Try again when the signal is better.",
    UiKey.SETUP_RETRY to "Retry",
    UiKey.SETUP_CHECK_FAILED_TITLE to "The file didn’t pass the check",
    UiKey.SETUP_CHECK_FAILED_BODY to "It may be damaged. Download it again to fix it.",
    UiKey.SETUP_DOWNLOAD_AGAIN to "Download again",
    UiKey.TOPICS_SUBTITLE to "Open a topic to see what to do. No typing needed.",
    UiKey.SETTINGS_AI to "AI helper",
    UiKey.SETTINGS_AI_READY to "Ready · works offline",
    UiKey.SETTINGS_ABOUT to "About & disclaimer",
    UiKey.SETTINGS_ABOUT_DESC to "Not a doctor. What this app can do.",
    UiKey.SETTINGS_SOURCES to "Sources",
    UiKey.SETTINGS_SOURCES_DESC to "DOH and WHO guidance",
    UiKey.SETTINGS_VERSION to "Buh.ai · Version {version}",
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
    UiKey.COMPOSER_SEND to "Send",
    UiKey.DANGER_HEADLINE to "Go to the health center NOW.",
    UiKey.DANGER_CALL to "Call 911 if you can",
    UiKey.CARD_KICKER to "First aid",
    UiKey.CARD_HOME to "What to do at home",
    UiKey.CARD_GO_NOW to "Go now if…",
    UiKey.CARD_YOU_SAID to "you said this",
    UiKey.AI_LABEL to "AI helper · not a doctor",
    UiKey.AI_THINKING to "AI is thinking…",
    UiKey.AI_WARMING to "The AI is still starting. Its reply will follow.",
    UiKey.AI_WITHHELD to "I can’t answer that safely. Please ask a health worker.",
    UiKey.AI_BASIC to "Basic mode: no AI reply. Danger checks and first-aid cards still work.",
    UiKey.AI_BASIC_CTA to "Download the AI",
    UiKey.CHAT_PROMPT to "What are you feeling?",
    UiKey.CHAT_SUBTITLE to "Write in your own language. Works without signal.",
    UiKey.CHAT_EXAMPLES to "Examples",
    UiKey.CHAT_EXAMPLE_1 to "My child has had diarrhea for three days.",
    UiKey.CHAT_EXAMPLE_2 to "My child has had a high fever since yesterday.",
    UiKey.CHAT_EXAMPLE_3 to "My father burned his hand on cooking oil.",
    UiKey.CHAT_TOPICS to "Or pick a topic",
    UiKey.CHAT_ALSO_ABOUT to "Also about:",
    UiKey.CHAT_REPLY_BELOW to "AI reply below",
    UiKey.CHAT_CHECKED_STEPS to "Checked first-aid steps",
    UiKey.NOT_COVERED_TITLE to "I can’t help with this one.",
    UiKey.NOT_COVERED_BODY to "Please go to the nearest health center.",
    UiKey.NOT_COVERED_TOPICS to "Topics I can help with",
) + EnglishScreensUi

internal val BisayaUi: Map<String, String> = mapOf(
    UiKey.COMPOSER_DISCLAIMER to "Dili kini doktor. Kung emerhensya, adto sa health center.",
    UiKey.COMPOSER_PLACEHOLDER to "Isulat dinhi…",
    UiKey.COMPOSER_SEND to "Ipadala",
    UiKey.DANGER_HEADLINE to "Adto dayon sa health center KARON.",
    UiKey.DANGER_CALL to "Tawag sa 911 kung mahimo",
    UiKey.CARD_KICKER to "Unang tabang",
    UiKey.CARD_HOME to "Unsa ang buhaton sa balay",
    UiKey.CARD_GO_NOW to "Adto dayon kung…",
    UiKey.CARD_YOU_SAID to "imong gisulti",
    UiKey.AI_LABEL to "AI helper · dili doktor",
    UiKey.AI_THINKING to "Naghunahuna ang AI…",
    UiKey.AI_WARMING to "Nagsugod pa ang AI. Moabot ang tubag human niini.",
    UiKey.AI_WITHHELD to "Dili ko makatubag ani nga luwas. Palihug pangutana sa health worker.",
    UiKey.AI_BASIC to "Basic mode: walay tubag gikan sa AI. Mogana gihapon ang mga pasidaan ug first-aid card.",
    UiKey.AI_BASIC_CTA to "I-download ang AI",
    UiKey.CHAT_PROMPT to "Unsa imong gibati?",
    UiKey.CHAT_SUBTITLE to "Isulat sa imong pinulongan. Mogana bisan walay signal.",
    UiKey.CHAT_EXAMPLES to "Pananglitan",
    UiKey.CHAT_EXAMPLE_1 to "Tulo na ka adlaw nga nagkalibang ang akong anak.",
    UiKey.CHAT_EXAMPLE_2 to "Hilanat kaayo ang akong anak sukad gahapon.",
    UiKey.CHAT_EXAMPLE_3 to "Napaso sa mantika ang kamot sa akong amahan.",
    UiKey.CHAT_TOPICS to "O pili og topiko",
    UiKey.CHAT_ALSO_ABOUT to "Bahin usab sa:",
    UiKey.CHAT_REPLY_BELOW to "Tubag sa AI sa ubos",
    UiKey.CHAT_CHECKED_STEPS to "Gisusi nga first-aid nga mga lakang",
    UiKey.NOT_COVERED_TITLE to "Dili ko makatabang ani.",
    UiKey.NOT_COVERED_BODY to "Palihug adto sa pinakaduol nga health center.",
    UiKey.NOT_COVERED_TOPICS to "Mga topiko nga makatabang ko",
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
