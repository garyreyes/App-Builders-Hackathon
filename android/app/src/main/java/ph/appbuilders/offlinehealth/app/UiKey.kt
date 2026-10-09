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
    const val COMPOSER_FIELD_DESC = "composer.field.desc"
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
}
