package ph.appbuilders.offlinehealth.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import ph.appbuilders.offlinehealth.domain.model.DangerSignId
import ph.appbuilders.offlinehealth.domain.model.TopicId

/** The glossary is the router (ARCHITECTURE step 1): a missed danger sign is the expensive failure. */
class TriageTest {

    private fun topics(text: String) = Triage.match(text).topics
    private fun dangers(text: String) = Triage.match(text).dangers

    @Test fun diarrheaInEveryLanguage() {
        assertEquals(listOf(TopicId.CHILD_DIARRHEA), topics("Tulo na ka adlaw nga nagkalibang an akon anak."))
        assertEquals(listOf(TopicId.CHILD_DIARRHEA), topics("Nagkalibang ang akong anak."))
        assertEquals(listOf(TopicId.CHILD_DIARRHEA), topics("Nagtatae ang anak ko."))
        assertEquals(listOf(TopicId.CHILD_DIARRHEA), topics("My child has had diarrhea for three days."))
    }

    @Test fun burnInEveryLanguage() {
        assertEquals(listOf(TopicId.BURN), topics("My grandfather burned his arm on hot oil."))
        assertEquals(listOf(TopicId.BURN), topics("Napaso an akon kamot ha mantika."))
        assertEquals(listOf(TopicId.BURN), topics("Nasunog ang kamay ko."))
        assertEquals(listOf(TopicId.BURN), topics("Scalded by boiling water"))
    }

    @Test fun feverInEveryLanguage() {
        assertEquals(listOf(TopicId.FEVER), topics("May hilanat an bata."))
        assertEquals(listOf(TopicId.FEVER), topics("Gihilantan ang akong anak."))
        assertEquals(listOf(TopicId.FEVER), topics("Mataas ang lagnat ng anak ko mula kahapon."))
        assertEquals(listOf(TopicId.FEVER), topics("My child has had a high fever since yesterday."))
    }

    @Test fun similarWordsDoNotMatch() {
        assertEquals(emptyList<TopicId>(), topics("Diri ako makapasok ha eskwelahan.")) // pasok ≠ paso
        assertEquals(emptyList<TopicId>(), topics("I have a toothache."))
    }

    @Test fun moreMentionsRankFirst() {
        assertEquals(
            listOf(TopicId.CHILD_DIARRHEA, TopicId.FEVER),
            topics("Fever since last night, and diarrhea. The diarrhea is watery."),
        )
    }

    @Test fun bloodInStoolNeedsTheDiarrheaContext() {
        assertEquals(
            listOf(DangerSignId.BLOOD_IN_STOOL),
            dangers("Tulo na ka adlaw nga nagkalibang ang akong anak, karon naa nay dugo."),
        )
        assertEquals(emptyList<DangerSignId>(), dangers("Blood from a small cut on his burned hand"))
    }

    @Test fun dangerSignsInEveryLanguage() {
        assertEquals(listOf(DangerSignId.CANNOT_DRINK), dangers("Diarrhea and he can’t drink anything"))
        assertEquals(listOf(DangerSignId.CANNOT_DRINK), dangers("Nagkalibang, diri makainom"))
        assertEquals(listOf(DangerSignId.VOMITS_EVERYTHING), dangers("Nagkalibang ngan nagsusuka"))
        assertEquals(listOf(DangerSignId.VERY_SLEEPY), dangers("Hilanat, lisod pukawon"))
        assertEquals(listOf(DangerSignId.SEIZURE), dangers("Hilanat ngan kumbulsyon"))
        assertEquals(listOf(DangerSignId.DIFFICULTY_BREATHING), dangers("Fever and hard to breathe"))
        assertEquals(listOf(DangerSignId.DIFFICULTY_BREATHING), dangers("Makuri gumhawa"))
        assertEquals(listOf(DangerSignId.UNCONSCIOUS), dangers("Nahimatay siya"))
    }

    @Test fun aDangerAloneHasNoTopic() {
        val match = Triage.match("He had a seizure")
        assertEquals(emptyList<TopicId>(), match.topics)
        assertEquals(listOf(DangerSignId.SEIZURE), match.dangers)
    }

    @Test fun chillsAreNotASeizure() {
        assertEquals(emptyList<DangerSignId>(), dangers("Hilanat ngan nagkukurog ha katugnaw"))
    }
}
