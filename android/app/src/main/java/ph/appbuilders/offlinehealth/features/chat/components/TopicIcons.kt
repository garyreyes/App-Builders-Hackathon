package ph.appbuilders.offlinehealth.features.chat.components

import androidx.annotation.DrawableRes
import ph.appbuilders.offlinehealth.R
import ph.appbuilders.offlinehealth.domain.model.TopicId

/** Material Symbols named in Tokens.dc.html for each topic. [TopicId.NONE] is the not-covered card's home icon. */
@DrawableRes
fun TopicId.iconRes(): Int = when (this) {
    TopicId.CHILD_DIARRHEA -> R.drawable.ic_water_drop
    TopicId.FEVER -> R.drawable.ic_thermostat
    TopicId.COUGH_BREATHING -> R.drawable.ic_air
    TopicId.WOUND_BLEEDING -> R.drawable.ic_healing
    TopicId.BURN -> R.drawable.ic_local_fire_department
    TopicId.PREGNANCY_WARNING -> R.drawable.ic_pregnant_woman
    TopicId.DENGUE_WARNING -> R.drawable.ic_pest_control
    TopicId.NONE -> R.drawable.ic_home
}
