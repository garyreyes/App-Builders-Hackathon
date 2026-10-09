package ph.appbuilders.offlinehealth.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ph.appbuilders.offlinehealth.domain.model.TopicId

/** PRD: 0 model replies with a dose, medicine name, or diagnosis reach the screen. Strict mode (owner, Oct 10). */
class GuardrailTest {

    private fun allows(text: String, dangerShown: Boolean = false, topic: TopicId? = null) =
        Guardrail.allows(text, dangerShown, topic)

    @Test fun burnReplyWithWrongDurationOrAdditiveIsBlocked() {
        assertFalse(allows("Buhusi hin tubig sulod hin 10 minutos.", topic = TopicId.BURN))
        assertFalse(allows("Use water with salt on the burn.", topic = TopicId.BURN))
        assertFalse(allows("Use hot water on the burn.", topic = TopicId.BURN))
        assertTrue(allows("Cool the burn under clean running water for 20 minutes.", topic = TopicId.BURN))
    }

    @Test fun plainFirstAidAdviceIsAllowed() {
        assertTrue(allows("Hatagi hin damo nga tubig ngan ORS an bata."))
        assertTrue(allows("Cool the burn under clean running water for 20 minutes."))
        assertTrue(allows("If the fever lasts more than 2 days, go to the health center."))
        assertTrue(allows("Call 911 if he can't breathe."))
    }

    @Test fun medicineNamesAreBlocked() {
        assertFalse(allows("Painom hin paracetamol an bata."))
        assertFalse(allows("You can give Biogesic."))
        assertFalse(allows("Ask for AMOXICILLIN at the pharmacy."))
        assertFalse(allows("Kinahanglan hin antibiotiko."))
        assertFalse(allows("Take cetirizine for the itch."))
        assertFalse(allows("Use an antacid."))
    }

    @Test fun medicineFormsAreBlocked() {
        assertFalse(allows("Give one tablet."))
        assertFalse(allows("Give her the tablets after eating."))
        assertFalse(allows("Ihatag an syrup."))
        // Seen on the emulator: the stock model opened with a dosage heading before inventing mg/kg doses.
        assertFalse(allows("Diarrhea Management: Dosage and Monitoring"))
    }

    @Test fun dosesAreBlocked() {
        assertFalse(allows("Give 5 ml every hour."))
        assertFalse(allows("Give 10mg."))
        assertFalse(allows("Ihatag an 1 kutsarita kada oras."))
        assertFalse(allows("Hatagi og 2 ka kutsara."))
        assertFalse(allows("Do it 3 times a day."))
        assertFalse(allows("Tulo ka beses kada adlaw: 3 beses."))
    }

    @Test fun phoneNumbersAreBlocked() {
        assertFalse(allows("Call 0917 123 4567."))
        assertFalse(allows("Tawag ha +63 917 1234567."))
    }

    @Test fun diagnosesAreBlocked() {
        assertFalse(allows("Your child has pneumonia."))
        assertFalse(allows("It may be typhoid."))
        assertFalse(allows("He probably has dengue."))
        assertFalse(allows("This is a diagnosis of cholera."))
    }

    @Test fun dengueMentionWithoutDiagnosisIsAllowed() {
        assertTrue(allows("In dengue season, watch for bleeding gums."))
    }

    @Test fun downplayIsBlockedOnlyWhenADangerIsShown() {
        val text = "Okay ra, no need to go to the hospital."
        assertFalse(allows(text, dangerShown = true))
        assertTrue(allows(text, dangerShown = false))
        assertFalse(allows("It's not serious.", dangerShown = true))
    }

    // Seen on the emulator (Oct 10, stock Sailor2): ice on a burn, "no water", UK/US emergency numbers.
    @Test fun firstAidMythsAreBlocked() {
        assertFalse(allows("Ice Pack: Apply a cold pack (e.g., ice) to the burned area for 10-15 minutes."))
        assertFalse(allows("Butangi hin yelo an paso."))
        assertFalse(allows("Put toothpaste on the burn."))
        assertFalse(allows("Rub butter on it to soothe the skin."))
        assertFalse(allows("Pop the blister so it drains."))
        assertFalse(allows("Avoid Moisture: Do not apply any moisture (e.g., water, lotion) to the wound."))
    }

    @Test fun warningsAgainstMythsAreAllowed() {
        assertTrue(allows("Cool it under running water. Don't put ice or toothpaste on it."))
        assertTrue(allows("Ayaw butangi hin yelo o toothpaste."))
        assertTrue(allows("Huwag lagyan ng yelo ang paso."))
        assertTrue(allows("Never use butter, and do not pop the blisters."))
    }

    @Test fun onlyThePhilippineEmergencyNumberIsAllowed() {
        assertFalse(allows("Call emergency services (112 in the UK, 999 in the UK, 111 in the USA)."))
        assertFalse(allows("Dial 117 right away."))
        assertTrue(allows("Call 911 now."))
        assertTrue(allows("Give fluids for 2 days and rest 8 hours."))
    }

    @Test fun emptyAndOverlongAreBlocked() {
        assertFalse(allows(""))
        assertFalse(allows("   \n "))
        assertFalse(allows("a ".repeat(Guardrail.MAX_CHARS)))
    }
}
