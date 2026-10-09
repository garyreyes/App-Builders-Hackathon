package ph.appbuilders.offlinehealth.features.modelsetup

import kotlinx.coroutines.flow.StateFlow
import ph.appbuilders.offlinehealth.domain.model.SetupState

/** Contract the core session implements over the model downloader (download, SHA-256 verify, basic mode). */
interface ModelSetupService {
    val state: StateFlow<SetupState>
    fun startDownload()
    fun cancel()
    fun retry()
    fun useBasicMode()
}
