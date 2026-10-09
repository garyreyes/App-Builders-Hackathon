package ph.appbuilders.offlinehealth.domain.model

/** The 7 first-aid topics, plus [NONE] for "not covered, go to the health center". */
enum class TopicId {
    CHILD_DIARRHEA,
    FEVER,
    COUGH_BREATHING,
    WOUND_BLEEDING,
    BURN,
    PREGNANCY_WARNING,
    DENGUE_WARNING,
    NONE,
}
