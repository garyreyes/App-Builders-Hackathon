package ph.appbuilders.offlinehealth.domain.model

/** One-time model setup. Names match `Setup.dc.html`. */
sealed interface SetupState {
    data class Intro(val sizeMb: Int) : SetupState
    data class Downloading(val doneMb: Int, val totalMb: Int) : SetupState
    data object Verifying : SetupState
    data object Done : SetupState
    data object NoInternet : SetupState
    data class NotEnoughStorage(val neededMb: Int, val freeMb: Int) : SetupState
    data class DownloadFailed(val doneMb: Int, val totalMb: Int) : SetupState
    data object CheckFailed : SetupState
}
