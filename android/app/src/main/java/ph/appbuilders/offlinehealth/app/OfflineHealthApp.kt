package ph.appbuilders.offlinehealth.app

import android.app.Application

/** Holds the single [AppContainer], so services and their state outlive activity recreation. */
class OfflineHealthApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
