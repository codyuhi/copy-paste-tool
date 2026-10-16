package online.codyuhi.copypastetool

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import online.codyuhi.copypastetool.data.sync.SyncWorker
import online.codyuhi.copypastetool.di.AppContainer

class CopyPasteApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        SyncWorker.schedule(this)

        CoroutineScope(Dispatchers.IO).launch {
            container.copyPasteRepository.seedDefaultsIfEmpty()
        }
    }
}
