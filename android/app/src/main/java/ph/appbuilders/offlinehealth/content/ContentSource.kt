package ph.appbuilders.offlinehealth.content

import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.domain.model.TopicCard
import ph.appbuilders.offlinehealth.domain.model.TopicId
import ph.appbuilders.offlinehealth.domain.model.TopicSummary

/**
 * Read-only access to the Health Library. The core session's `HealthLibrary` implements this.
 * All UI copy comes from [uiString], so no composable hard-codes text.
 */
interface ContentSource {
    fun topics(language: Language): List<TopicSummary>
    fun card(topicId: TopicId, language: Language): TopicCard
    fun uiString(key: String, language: Language): String
}
