package ph.appbuilders.offlinehealth.lib.llm

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.domain.model.TopicCard
import ph.appbuilders.offlinehealth.domain.model.TopicId

class HealthPromptTest {

    private val burn = TopicCard(
        TopicId.BURN, "Burns",
        atHome = listOf("Cool the burn under clean running water for 20 minutes."),
        goNowIf = listOf("bigger than the person’s palm"),
        source = "WHO",
    )

    @Test fun alwaysStatesTheSafetyRulesAnd911() {
        val prompt = HealthPrompt.build(card = null, Language.ENG)
        assertTrue("Never name a medicine" in prompt)
        assertTrue("911" in prompt)
    }

    @Test fun groundsTheModelOnTheCheckedCard() {
        val prompt = HealthPrompt.build(burn, Language.ENG)
        assertTrue("Cool the burn under clean running water for 20 minutes." in prompt)
        assertTrue("bigger than the person’s palm" in prompt)
        assertTrue("do not contradict" in prompt)
    }

    @Test fun noCardMeansNoCheckedSteps() {
        assertFalse("Checked first-aid steps" in HealthPrompt.build(card = null, Language.ENG))
    }

    @Test fun namesTheUsersLanguage() {
        assertTrue("Reply only in Waray" in HealthPrompt.build(burn, Language.WAR))
        assertTrue("Reply only in Bisaya" in HealthPrompt.build(burn, Language.CEB))
        assertTrue("Reply only in Tagalog" in HealthPrompt.build(burn, Language.TGL))
        assertTrue("Reply only in English" in HealthPrompt.build(burn, Language.ENG))
    }
}
