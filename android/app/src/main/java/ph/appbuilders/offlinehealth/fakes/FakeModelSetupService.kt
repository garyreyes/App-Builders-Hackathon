package ph.appbuilders.offlinehealth.fakes

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ph.appbuilders.offlinehealth.domain.model.SetupState
import ph.appbuilders.offlinehealth.features.modelsetup.ModelSetupService

/**
 * FAKE: no network, no file. Walks Intro → Downloading (about 8 s) → Verifying (2 s) → Done, so the setup
 * screens can be shown. [show] is the debug-only way to reach each error state (B5–B8).
 * [onModelReady] and [onBasicMode] let AppContainer keep the fake chat's status pill in step; the core's
 * real services share that state directly.
 */
class FakeModelSetupService(
    private val scope: CoroutineScope,
    private val onModelReady: () -> Unit,
    private val onBasicMode: () -> Unit,
    initial: SetupState = SetupState.Intro(SIZE_MB),
) : ModelSetupService {

    private val _state = MutableStateFlow(initial)
    override val state: StateFlow<SetupState> = _state.asStateFlow()

    private var job: Job? = null

    override fun startDownload() = download(fromMb = 0)

    override fun cancel() {
        job?.cancel()
        _state.value = SetupState.Intro(SIZE_MB)
    }

    /** Resumes a dropped download where it stopped; every other error starts over. */
    override fun retry() {
        val from = (_state.value as? SetupState.DownloadFailed)?.doneMb ?: 0
        download(from)
    }

    override fun useBasicMode() {
        job?.cancel()
        _state.value = SetupState.Intro(SIZE_MB)
        onBasicMode()
    }

    /** Debug only: jump straight to a state (the error screens can't happen for real here). */
    fun show(state: SetupState) {
        job?.cancel()
        _state.value = state
    }

    private fun download(fromMb: Int) {
        job?.cancel()
        job = scope.launch {
            var done = fromMb
            while (done < SIZE_MB) {
                _state.value = SetupState.Downloading(done, SIZE_MB)
                delay(TICK_MS)
                done = (done + MB_PER_TICK).coerceAtMost(SIZE_MB)
            }
            _state.value = SetupState.Verifying
            delay(VERIFY_MS)
            _state.value = SetupState.Done
            onModelReady()
        }
    }

    companion object {
        const val SIZE_MB = 740           // Setup.dc.html's figure; the core reports the real size
        private const val TICK_MS = 100L
        private const val MB_PER_TICK = 9 // about 8 s for the whole file
        private const val VERIFY_MS = 2_000L
    }
}
