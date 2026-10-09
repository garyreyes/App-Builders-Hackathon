package ph.appbuilders.offlinehealth.content

import ph.appbuilders.offlinehealth.domain.model.DangerSignId
import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.domain.model.TopicCard
import ph.appbuilders.offlinehealth.domain.model.TopicId
import ph.appbuilders.offlinehealth.domain.model.TopicSummary
import ph.appbuilders.offlinehealth.fakes.BisayaTitles
import ph.appbuilders.offlinehealth.fakes.BisayaUi
import ph.appbuilders.offlinehealth.fakes.EnglishTitles
import ph.appbuilders.offlinehealth.fakes.EnglishUi
import ph.appbuilders.offlinehealth.fakes.LanguageSelf

/**
 * The demo Health Library: only topics with written cards ([DemoCards]) exist, so no placeholder can reach the
 * screen. UI copy still comes from the frontend's copy tables (FakeCopy.kt) until `ui_strings.json` exists.
 */
class DemoContentSource : ContentSource {

    override fun topics(language: Language): List<TopicSummary> =
        DemoCards.topics.map { TopicSummary(it, title(it, language)) }

    override fun card(topicId: TopicId, language: Language): TopicCard {
        val text = DemoCards.text(topicId, language)
            ?: return TopicCard(topicId, title(topicId, language), emptyList(), emptyList(), source = "")
        return TopicCard(topicId, title(topicId, language), text.atHome, text.goNowIf, text.source)
    }

    override fun uiString(key: String, language: Language): String =
        LanguageSelf[language]?.get(key)
            ?: (if (language == Language.CEB) BisayaUi[key] else null)
            ?: EnglishUi[key]
            ?: "[$key]"

    fun dangerMessage(sign: DangerSignId, language: Language): String = DemoCards.dangerMessage(sign, language)

    private fun title(topicId: TopicId, language: Language): String =
        when (language) {
            Language.WAR -> DemoCards.warayTitles[topicId]
            Language.CEB -> BisayaTitles[topicId]
            else -> null
        } ?: EnglishTitles[topicId] ?: "[${topicId.name}]"
}
