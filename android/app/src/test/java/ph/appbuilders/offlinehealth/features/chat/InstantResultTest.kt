package ph.appbuilders.offlinehealth.features.chat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ph.appbuilders.offlinehealth.content.DemoContentSource
import ph.appbuilders.offlinehealth.domain.model.DangerSignId
import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.domain.model.TopicId

class InstantResultTest {

    private val content = DemoContentSource()
    private val instant = InstantResult(content)

    @Test fun warayDiarrheaWithBloodHighlightsTheRightSign() {
        val result = instant.of("Nagkalibang an akon anak, may dugo na", Language.WAR)
        assertEquals(TopicId.CHILD_DIARRHEA, result.card?.topicId)
        assertEquals(listOf(DangerSignId.BLOOD_IN_STOOL), result.dangers.map { it.id })
        assertEquals("May-ada dugo an tae.", result.dangers.single().text)
        assertEquals(setOf(2), result.matchedSigns) // "may-ada dugo an tae" in the card
        assertEquals("may-ada dugo an tae", result.card!!.goNowIf[2])
        assertNull(result.ai)
    }

    @Test fun burnGetsTheBurnCardInWaray() {
        val result = instant.of("Napaso an akon kamot", Language.WAR)
        assertEquals(TopicId.BURN, result.card?.topicId)
        assertEquals("Paso", result.card?.title)
        assertTrue(result.card!!.source.contains("Prototype card"))
        assertFalse(result.otherTopics.any { it.topicId == TopicId.BURN })
    }

    @Test fun nothingMatchedIsNotCovered() {
        val result = instant.of("I have a toothache", Language.ENG)
        assertNull(result.card)
        assertTrue(result.dangers.isEmpty())
    }

    @Test fun dangerWithoutTopicHasNoCard() {
        val result = instant.of("He had a seizure", Language.ENG)
        assertNull(result.card)
        assertEquals("Seizure (the body is shaking).", result.dangers.single().text)
    }

    @Test fun onlyTopicsWithWrittenCardsAreListed() {
        assertEquals(
            setOf(TopicId.CHILD_DIARRHEA, TopicId.FEVER, TopicId.BURN),
            content.topics(Language.WAR).map { it.topicId }.toSet(),
        )
        for (language in Language.entries) {
            for (topic in DemoContentSourceTopics) {
                val card = content.card(topic, language)
                assertFalse("$topic/$language", card.atHome.any { "Placeholder" in it })
                assertFalse("$topic/$language", card.goNowIf.any { "Placeholder" in it })
            }
        }
    }

    private val DemoContentSourceTopics = listOf(TopicId.CHILD_DIARRHEA, TopicId.FEVER, TopicId.BURN)
}
