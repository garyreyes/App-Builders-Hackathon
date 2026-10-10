package ph.appbuilders.offlinehealth.content

import ph.appbuilders.offlinehealth.app.UiKey
import ph.appbuilders.offlinehealth.domain.model.TopicId

// Chat-screen copy in Waray and Tagalog, so the demo screen speaks the user's language (Bisaya already exists in
// FakeCopy.kt). Prototype text. Screens not listed here (setup,
// settings) still fall back to English. ui_strings.json replaces all of this.

internal val WarayUi: Map<String, String> = mapOf(
    UiKey.TOPBAR_STATUS_STARTING to "Nagsisinugod an AI…",
    UiKey.MENU_TOPICS to "Mga topiko han first aid",
    UiKey.COMPOSER_DISCLAIMER to "Diri ini doktor. Kun emerhensya, kadto ha health center.",
    UiKey.COMPOSER_PLACEHOLDER to "Pagsurat dinhi…",
    UiKey.COMPOSER_SEND to "Ipadara",
    UiKey.DANGER_HEADLINE to "Kadto dayon ha health center YANA.",
    UiKey.DANGER_CALL to "Tawag ha 911 kun mahimo",
    UiKey.CARD_KICKER to "Siyahan nga bulig",
    UiKey.CARD_HOME to "Ano an bubuhaton ha balay",
    UiKey.CARD_GO_NOW to "Kadto dayon kun…",
    UiKey.CARD_YOU_SAID to "imo ginsiring",
    UiKey.AI_LABEL to "AI helper · diri doktor",
    UiKey.AI_THINKING to "Naghuhunahuna an AI…",
    UiKey.AI_WARMING to "Nagsisinugod pa an AI. Maabot an iya baton kahuman.",
    UiKey.AI_WITHHELD to "Diri ako makabaton hini nga luwas. Alayon pakiana ha health worker.",
    UiKey.AI_BASIC to "Basic mode: waray baton tikang ha AI. Nagana gihapon an mga pahimangno ngan first-aid card.",
    UiKey.AI_BASIC_CTA to "I-download an AI",
    UiKey.CHAT_PROMPT to "Ano an imo ginbabati?",
    UiKey.CHAT_SUBTITLE to "Pagsurat ha imo yinaknan. Nagana bisan waray signal.",
    UiKey.CHAT_EXAMPLES to "Pananglitan",
    UiKey.CHAT_EXAMPLE_1 to "Tulo na ka adlaw nga nagkakalibang an akon anak.",
    UiKey.CHAT_EXAMPLE_2 to "Duro an hilanat han akon anak tikang kahapon.",
    UiKey.CHAT_EXAMPLE_3 to "Napaso ha mantika an kamot han akon tatay.",
    UiKey.CHAT_TOPICS to "O pagpili hin topiko",
    UiKey.CHAT_ALSO_ABOUT to "Mahitungod liwat ha:",
    UiKey.CHAT_REPLY_BELOW to "Baton han AI ha ubos",
    UiKey.CHAT_CHECKED_STEPS to "Ginsusi nga mga lakang han first aid",
    UiKey.NOT_COVERED_TITLE to "Diri ako makabulig hini.",
    UiKey.NOT_COVERED_BODY to "Alayon kadto ha pinakahirani nga health center.",
    UiKey.NOT_COVERED_TOPICS to "Mga topiko nga makakabulig ako",
)

internal val TagalogUi: Map<String, String> = mapOf(
    UiKey.TOPBAR_STATUS_STARTING to "Nagsisimula ang AI…",
    UiKey.MENU_TOPICS to "Mga paksa ng first aid",
    UiKey.COMPOSER_DISCLAIMER to "Hindi ito doktor. Kung emergency, pumunta sa health center.",
    UiKey.COMPOSER_PLACEHOLDER to "Mag-type dito…",
    UiKey.COMPOSER_SEND to "Ipadala",
    UiKey.DANGER_HEADLINE to "Pumunta sa health center NGAYON.",
    UiKey.DANGER_CALL to "Tumawag sa 911 kung kaya",
    UiKey.CARD_KICKER to "Paunang lunas",
    UiKey.CARD_HOME to "Ano ang gagawin sa bahay",
    UiKey.CARD_GO_NOW to "Pumunta agad kung…",
    UiKey.CARD_YOU_SAID to "sinabi mo ito",
    UiKey.AI_LABEL to "AI helper · hindi doktor",
    UiKey.AI_THINKING to "Nag-iisip ang AI…",
    UiKey.AI_WARMING to "Nagsisimula pa ang AI. Susunod ang sagot nito.",
    UiKey.AI_WITHHELD to "Hindi ko ito masasagot nang ligtas. Magtanong sa health worker.",
    UiKey.AI_BASIC to "Basic mode: walang sagot mula sa AI. Gumagana pa rin ang mga babala at first-aid card.",
    UiKey.AI_BASIC_CTA to "I-download ang AI",
    UiKey.CHAT_PROMPT to "Ano ang nararamdaman mo?",
    UiKey.CHAT_SUBTITLE to "Sumulat sa sarili mong wika. Gumagana kahit walang signal.",
    UiKey.CHAT_EXAMPLES to "Halimbawa",
    UiKey.CHAT_EXAMPLE_1 to "Tatlong araw nang nagtatae ang anak ko.",
    UiKey.CHAT_EXAMPLE_2 to "Mataas ang lagnat ng anak ko mula kahapon.",
    UiKey.CHAT_EXAMPLE_3 to "Napaso ng mantika ang kamay ng tatay ko.",
    UiKey.CHAT_TOPICS to "O pumili ng paksa",
    UiKey.CHAT_ALSO_ABOUT to "Tungkol din sa:",
    UiKey.CHAT_REPLY_BELOW to "Sagot ng AI sa ibaba",
    UiKey.CHAT_CHECKED_STEPS to "Sinuring hakbang ng first aid",
    UiKey.NOT_COVERED_TITLE to "Hindi ako makakatulong dito.",
    UiKey.NOT_COVERED_BODY to "Pumunta sa pinakamalapit na health center.",
    UiKey.NOT_COVERED_TOPICS to "Mga paksang matutulungan ko",
)

internal val TagalogTitles: Map<TopicId, String> = mapOf(
    TopicId.CHILD_DIARRHEA to "Pagtatae ng bata",
    TopicId.FEVER to "Lagnat",
    TopicId.BURN to "Paso",
)
