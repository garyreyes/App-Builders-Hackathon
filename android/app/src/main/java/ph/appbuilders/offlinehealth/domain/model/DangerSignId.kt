package ph.appbuilders.offlinehealth.domain.model

/** Danger signs found by keyword triage. Each one maps to a pre-translated danger message. */
enum class DangerSignId {
    CANNOT_DRINK,
    VOMITS_EVERYTHING,
    BLOOD_IN_STOOL,
    VERY_SLEEPY,
    DIFFICULTY_BREATHING,
    SEVERE_BLEEDING,
    PREGNANCY_BLEEDING,
    SEIZURE,
    CHEST_PAIN,
    UNCONSCIOUS,
}
