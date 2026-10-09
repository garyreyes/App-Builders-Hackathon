package ph.appbuilders.offlinehealth.fakes

import ph.appbuilders.offlinehealth.domain.model.DangerMessage
import ph.appbuilders.offlinehealth.domain.model.DangerSignId

// PLACEHOLDER sample messages and replies from the design canvas, for previews, the dev gallery, and the fakes.
// Not reviewed. The real danger messages come from danger_signs.json via the core's ChatService.

object FakeSamples {
    const val USER_CEB = "Tulo na ka adlaw nga nagkalibang ang akong anak, karon naa nay dugo."
    const val USER_ENG = "My grandfather burned his arm on hot oil."
    const val USER_SHORT = "Hilanat"

    val dangerCeb = listOf(DangerMessage(DangerSignId.BLOOD_IN_STOOL, "Adunay dugo sa tae."))
    val dangerEng = listOf(DangerMessage(DangerSignId.BLOOD_IN_STOOL, "Blood in the stool."))
    val dangerEngThree = listOf(
        DangerMessage(DangerSignId.CANNOT_DRINK, "Can’t drink or breastfeed."),
        DangerMessage(DangerSignId.VOMITS_EVERYTHING, "Vomits everything."),
        DangerMessage(DangerSignId.VERY_SLEEPY, "Very sleepy or hard to wake."),
    )

    const val REPLY_CEB = "Kay naa nay dugo sa tae sa imong anak, dad-a siya dayon sa health center. " +
        "Samtang nagbiyahe, padayon og hatag og ORS ug gatas."
    const val REPLY_CEB_PARTIAL = "Kay naa nay dugo sa tae sa imong anak, dad-a siya dayon sa health center. Samtang"
    const val REPLY_ENG = "Because your baby is very sleepy and can’t keep anything down, go to the health center now. " +
        "On the way, keep offering small sips of ORS."
}
