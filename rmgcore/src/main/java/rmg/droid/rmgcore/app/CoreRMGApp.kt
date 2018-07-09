package rmg.droid.rmgcore.app

import android.app.Application
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.support.v7.app.AppCompatDelegate
import com.crashlytics.android.Crashlytics
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker
import com.google.firebase.FirebaseApp
import io.fabric.sdk.android.Fabric
import rmg.droid.rmgcore.media.MediaPlayerService
import rmg.droid.rmgcore.analytics.AnalyticsManager
import rmg.droid.rmgcore.media.MediaPreferences
import rmg.droid.rmgcore.services.MediaRepository


/**
 * Created by Arthur Korchagin on 10.06.17
 */
abstract class CoreRMGApp : Application() {

    abstract val mediaPreferences: MediaPreferences
    abstract val googleAnalyticsKey: String /* "UA-1866658-33" */
    abstract val activityClass: Class<*>

    private var player: MediaPlayerService? = null
    var serviceBound = false

    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as MediaPlayerService.LocalBinder
            player = binder.service
            serviceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serviceBound = false
        }
    }

    private var mTracker: Tracker? = null

    val analyticsManager: AnalyticsManager by lazy {
        AnalyticsManager(getDefaultTracker())
    }

    companion object {
        lateinit var inst: CoreRMGApp
            private set
            get
    }

    lateinit var mediaRepository: MediaRepository
        private set
        get

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        inst = this
        FirebaseApp.initializeApp(this)
        Fabric.with(this, Crashlytics())
        mediaRepository = MediaRepository(this)
    }

    @Synchronized fun getDefaultTracker(): Tracker {
        if (mTracker == null) {
            val analytics = GoogleAnalytics.getInstance(this)
            mTracker = analytics.newTracker(googleAnalyticsKey)
        }
        return mTracker!!
    }


}