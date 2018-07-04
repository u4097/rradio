package ru.russianmediagroup.rusrad.app

import com.crashlytics.android.Crashlytics
import com.google.firebase.FirebaseApp
import io.fabric.sdk.android.Fabric
import rmg.droid.rmgcore.analytics.AnalyticsService
import rmg.droid.rmgcore.app.CoreRMGApp
import rmg.droid.rmgcore.entity.Channel
import rmg.droid.rmgcore.media.MediaPreferences
import rmg.droid.rmgcore.media.cover.CoversManager
import ru.russianmediagroup.rusrad.R
import ru.russianmediagroup.rusrad.alarm.AlarmController
import ru.russianmediagroup.rusrad.di.components.DaggerAppComponent
import ru.russianmediagroup.rusrad.di.modules.AppModule
import ru.russianmediagroup.rusrad.di.modules.NetworkModule
import ru.russianmediagroup.rusrad.network.models.ApiChannel
import ru.russianmediagroup.rusrad.ui.activity.MusicActivity
import uk.co.chrisjenx.calligraphy.CalligraphyConfig


/**
 * Created by Arthur Korchagin on 10.06.17
 */
class RusRadioApp : CoreRMGApp() {

    companion object {
        lateinit var inst: RusRadioApp
    }

    var isMediaInitialized = false

    override val mediaPreferences = MediaPreferences(
            iconNotification = R.drawable.ic_notification,
            viewPagerMargin = R.dimen.view_pager_margin,
            viewPagerPageMargin = R.dimen.view_pager_page_margin,
            channelLabels = R.array.channel_labels,
            channelMediaStreams = R.array.channel_media_streams,
            channelDataStreams = R.array.channel_data_streams,
            channelJingleLabels = R.array.channel_jingle_labels,
            labelYouAreListen = R.string.label_you_are_listen,
            channelCoverPlaceholders = R.array.channel_cover_placeholders,
            channelGoNetwork = R.array.channel_go_network,
            channelProgramIds = R.array.channel_programs
    )

    override val name: CharSequence
        get() = getString(R.string.app_name)

    override val activityClass = MusicActivity::class.java

    override val googleAnalyticsKey = "UA-1866658-43"

    override val analyticsService: AnalyticsService by lazy {
        appComponent.analyticsService()
    }

    lateinit var alarm: AlarmController

    //TODO Has to be removed in new version
    val networkService by lazy { appComponent.networkService() }

    val appComponent by lazy {
        DaggerAppComponent.builder()
                .networkModule(NetworkModule(googleAnalyticsKey))
                .appModule(AppModule(this))
                .build()
    }

    override fun onCreate() {
        super.onCreate()
        inst = this
        FirebaseApp.initializeApp(this)
        Fabric.with(this, Crashlytics())

        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/proxima_nova_regular.otf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        )

        alarm = AlarmController(this)
    }

    fun updateChannels(apiChannels: List<ApiChannel>) {
        mediaRepository.apply {
            if (isPlaying) {
                togglePlayStop()
            }
        }

        val coversManager = CoversManager(this)

        val slugs = resources.getStringArray(R.array.channel_programs)
        val labels = resources.getStringArray(mediaPreferences.channelLabels)
        val mediaStreams = resources.getStringArray(mediaPreferences.channelMediaStreams)
        val dataStreams = resources.getStringArray(mediaPreferences.channelDataStreams)
        val jingleLabels = resources.getStringArray(mediaPreferences.channelJingleLabels)
        val programIds = if (mediaPreferences.channelProgramIds == 0) emptyArray<String>() else resources.getStringArray(mediaPreferences.channelProgramIds)
        val commonDefaultTitle = resources.getString(mediaPreferences.labelYouAreListen)
        val coverPlaceholders = resources.obtainTypedArray(mediaPreferences.channelCoverPlaceholders)
        val blockPlaceholders = resources.obtainTypedArray(mediaPreferences.channelBlockPlaceholders)
        val channelGoNetwork = resources.getIntArray(mediaPreferences.channelGoNetwork)

        val channels = apiChannels.map {

            val channelIndex = it.id?.let { slugs.indexOf(it) } ?: -1


            val channelName = it.name ?: labels.getOrElse(channelIndex) { "" }

            Channel(channelName = channelName,

                    streamUrl = it.stream_url ?: mediaStreams.getOrElse(channelIndex) { "" },

                    dataUrl = it.api_url ?: dataStreams.getOrElse(channelIndex) { "" },

                    defaultArtist = jingleLabels.getOrElse(channelIndex) { channelName },

                    defaultTitle = commonDefaultTitle,

                    coverRes = (if (channelIndex >= 0 && channelIndex < coverPlaceholders.length()) coverPlaceholders.getResourceId(channelIndex, -1) else null)
                            ?: R.drawable.channel_rr,

                    blockCoverRes = (if (channelIndex >= 0 && channelIndex < coverPlaceholders.length()) blockPlaceholders.getResourceId(channelIndex, -1) else null)
                            ?: R.drawable.channel_rr,

                    loader = coversManager,

                    networkUpdates = true,

                    description = it.description,

                    programId = it.id ?: programIds.getOrElse(channelIndex) { "" })
        }

        super.updateMediaRepository(coversManager, channels)
        isMediaInitialized = true
    }
}