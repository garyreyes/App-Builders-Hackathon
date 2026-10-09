package ph.appbuilders.offlinehealth.app

/**
 * Every UI string key the app reads. `ui_strings.json` needs each one in all 4 languages.
 * `{name}` marks a value the app fills in. Keys under `language.` describe a language in that language itself
 * (looked up with `UiText.inLanguage`), so the picker can show each language by its own name.
 */
object UiKey {
    // A language describing itself
    const val LANGUAGE_NAME = "language.name"         // Binisaya · Winaray · Tagalog / Taglish · English
    const val LANGUAGE_ALIAS = "language.alias"       // Cebuano · Waray · Filipino · (empty)
    const val LANGUAGE_CODE = "language.code"         // BIS · WAR · TL · EN
    const val LANGUAGE_WORD = "language.word"         // Pinulongan · Yinaknan · Wika · Language
    const val LANGUAGE_CHOOSE = "language.choose"     // "Choose your language", for the first-launch picker

    // Language picker (first launch)
    const val LANGUAGE_CHANGE_LATER = "languagePicker.changeLater"

    // Shared navigation
    const val NAV_BACK = "nav.back"
    const val NAV_BACK_TO_TOPICS = "nav.backToTopics"
    const val NAV_TOPICS = "nav.topics"

    // Top bar
    const val TOPBAR_STATUS_READY = "topbar.status.ready"
    const val TOPBAR_STATUS_STARTING = "topbar.status.starting"
    const val TOPBAR_STATUS_BASIC = "topbar.status.basic"
    const val TOPBAR_STATUS_READY_DESC = "topbar.status.ready.desc"
    const val TOPBAR_STATUS_STARTING_DESC = "topbar.status.starting.desc"
    const val TOPBAR_STATUS_BASIC_DESC = "topbar.status.basic.desc"
    const val TOPBAR_LANGUAGE_DESC = "topbar.language.desc" // {language}
    const val TOPBAR_MENU_DESC = "topbar.menu.desc"
    const val MENU_TOPICS = "menu.topics"
    const val MENU_SETTINGS = "menu.settings"

    // Composer
    const val COMPOSER_DISCLAIMER = "composer.disclaimer"
    const val COMPOSER_PLACEHOLDER = "composer.placeholder"
    const val COMPOSER_SEND = "composer.send"

    // Danger banner
    const val DANGER_HEADLINE = "danger.headline"
    const val DANGER_CALL = "danger.call"

    // First-aid card
    const val CARD_KICKER = "card.kicker"
    const val CARD_HOME = "card.home"
    const val CARD_GO_NOW = "card.goNow"
    const val CARD_YOU_SAID = "card.youSaid"

    // AI reply
    const val AI_LABEL = "ai.label"
    const val AI_THINKING = "ai.thinking"
    const val AI_WARMING = "ai.warming"
    const val AI_WITHHELD = "ai.withheld"
    const val AI_BASIC = "ai.basic"
    const val AI_BASIC_CTA = "ai.basic.cta"

    // Chat screen
    const val CHAT_PROMPT = "chat.prompt"
    const val CHAT_SUBTITLE = "chat.subtitle"
    const val CHAT_EXAMPLES = "chat.examples"
    const val CHAT_EXAMPLE_1 = "chat.example.1"
    const val CHAT_EXAMPLE_2 = "chat.example.2"
    const val CHAT_EXAMPLE_3 = "chat.example.3"
    const val CHAT_TOPICS = "chat.topics"
    const val CHAT_ALSO_ABOUT = "chat.alsoAbout"
    const val CHAT_REPLY_BELOW = "chat.replyBelow"
    const val CHAT_CHECKED_STEPS = "chat.checkedSteps" // label on the compact card under an AI answer
    const val NOT_COVERED_TITLE = "notCovered.title"
    const val NOT_COVERED_BODY = "notCovered.body"
    const val NOT_COVERED_TOPICS = "notCovered.topics"

    // AI setup (B1–B8)
    const val SETUP_INTRO_TITLE = "setup.intro.title"
    const val SETUP_INTRO_BODY = "setup.intro.body"
    const val SETUP_INTRO_SIZE = "setup.intro.size"           // {size} is shown bold
    const val SETUP_INTRO_OFFLINE = "setup.intro.offline"
    const val SETUP_INTRO_PRIVATE = "setup.intro.private"
    const val SETUP_DOWNLOAD = "setup.download"               // {size}
    const val SETUP_BASIC = "setup.basic"
    const val SETUP_BASIC_NOTE = "setup.basic.note"
    const val SETUP_MB = "setup.mb"                           // {mb}
    const val SETUP_DOWNLOADING_TITLE = "setup.downloading.title"
    const val SETUP_DOWNLOADING_BODY = "setup.downloading.body"
    const val SETUP_PROGRESS = "setup.progress"               // {done} {total}
    const val SETUP_PROGRESS_DESC = "setup.progress.desc"
    const val SETUP_KEEP_WIFI = "setup.keepWifi"
    const val SETUP_CANCEL = "setup.cancel"
    const val SETUP_VERIFYING_TITLE = "setup.verifying.title"
    const val SETUP_VERIFYING_BODY = "setup.verifying.body"
    const val SETUP_DONE_TITLE = "setup.done.title"
    const val SETUP_DONE_BODY = "setup.done.body"
    const val SETUP_START = "setup.start"
    const val SETUP_NO_INTERNET_TITLE = "setup.noInternet.title"
    const val SETUP_NO_INTERNET_BODY = "setup.noInternet.body"
    const val SETUP_TRY_AGAIN = "setup.tryAgain"
    const val SETUP_STORAGE_TITLE = "setup.storage.title"
    const val SETUP_STORAGE_BODY = "setup.storage.body"
    const val SETUP_STORAGE_NEEDED = "setup.storage.needed"
    const val SETUP_STORAGE_FREE = "setup.storage.free"
    const val SETUP_STORAGE_MORE = "setup.storage.more"       // {mb}
    const val SETUP_FAILED_TITLE = "setup.failed.title"
    const val SETUP_FAILED_BODY = "setup.failed.body"         // {done} {total}
    const val SETUP_RETRY = "setup.retry"
    const val SETUP_CHECK_FAILED_TITLE = "setup.checkFailed.title"
    const val SETUP_CHECK_FAILED_BODY = "setup.checkFailed.body"
    const val SETUP_DOWNLOAD_AGAIN = "setup.downloadAgain"

    // Topics (D1)
    const val TOPICS_SUBTITLE = "topics.subtitle"

    // Settings (E) and the two pages it opens
    const val SETTINGS_AI = "settings.ai"
    const val SETTINGS_AI_READY = "settings.ai.ready"
    const val SETTINGS_ABOUT = "settings.about"
    const val SETTINGS_ABOUT_DESC = "settings.about.desc"
    const val SETTINGS_SOURCES = "settings.sources"
    const val SETTINGS_SOURCES_DESC = "settings.sources.desc"
    const val SETTINGS_VERSION = "settings.version"           // {version}
}
