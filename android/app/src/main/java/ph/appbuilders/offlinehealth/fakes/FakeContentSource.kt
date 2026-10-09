package ph.appbuilders.offlinehealth.fakes

import ph.appbuilders.offlinehealth.content.ContentSource
import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.domain.model.TopicCard
import ph.appbuilders.offlinehealth.domain.model.TopicId
import ph.appbuilders.offlinehealth.domain.model.TopicSummary

/**
 * FAKE: placeholder content (see FakeCopy.kt) until the core's `HealthLibrary` is swapped in via `AppContainer`.
 * English plus Bisaya samples. Waray and Tagalog fall back to English. A missing key shows as `[key]`.
 */
class FakeContentSource : ContentSource {

    override fun topics(language: Language): List<TopicSummary> =
        TopicId.entries.filter { it != TopicId.NONE }.map { TopicSummary(it, title(it, language)) }

    override fun card(topicId: TopicId, language: Language): TopicCard {
        val text = when {
            topicId != TopicId.CHILD_DIARRHEA -> PlaceholderCard
            language == Language.CEB -> BisayaDiarrhea
            else -> EnglishDiarrhea
        }
        return TopicCard(topicId, title(topicId, language), text.atHome, text.goNowIf, text.source)
    }

    override fun uiString(key: String, language: Language): String =
        LanguageSelf[language]?.get(key)
            ?: (if (language == Language.CEB) BisayaUi[key] else null)
            ?: EnglishUi[key]
            ?: "[$key]"

    private fun title(topicId: TopicId, language: Language): String =
        (if (language == Language.CEB) BisayaTitles[topicId] else null)
            ?: EnglishTitles[topicId]
            ?: "[${topicId.name}]"
}
