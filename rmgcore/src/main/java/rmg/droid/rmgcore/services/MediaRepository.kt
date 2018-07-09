package rmg.droid.rmgcore.services

import android.content.Context
import android.content.Intent
import org.greenrobot.eventbus.EventBus
import rmg.droid.rmgcore.app.CoreRMGApp
import rmg.droid.montecarlo.entity.Channel
import rmg.droid.rmgcore.event.PlaybackStatusEvent
import rmg.droid.rmgcore.media.MediaPlayerService
import rmg.droid.rmgcore.media.cover.CoversManager
import rmg.droid.rmgcore.media.icy.IcyDataSource


/**
 *  @author Arthur Korchagin on 13.06.17.
 */

class MediaRepository(val app: CoreRMGApp) {

    private val prefs = app.mediaPreferences
    private val mCoversManager = CoversManager(app)

    val channels: List<Channel> by lazy {
        val labels = app.resources.getStringArray(prefs.channelLabels)
        val mediaStreams = app.resources.getStringArray(prefs.channelMediaStreams)
        val dataStreams = app.resources.getStringArray(prefs.channelDataStreams)
        val jingleLabels = app.resources.getStringArray(prefs.channelJingleLabels)
        val commonDefaultTitle = app.resources.getString(prefs.labelYouAreListen)
        val coverPlaceholders = app.resources.obtainTypedArray(prefs.channelCoverPlaceholders)
        val channelGoNetwork = app.resources.getIntArray(prefs.channelGoNetwork)

        (0 until labels.size).map {
            Channel(channelName = labels[it],
                    streamUrl = mediaStreams[it],
                    dataUrl = dataStreams[it],
                    defaultArtist = jingleLabels[it],
                    defaultTitle = commonDefaultTitle,
                    coverRes = coverPlaceholders.getResourceId(it, -1),
                    loader = mCoversManager,
                    networkUpdates = channelGoNetwork[it]==1)
        }
    }

    private var mCurrentChannel: Channel = channels[0]

    var outOfSync = 0L
        get() = mCoversManager.outOfSync

    var isPlaying: Boolean = false
        set(value) {
            field = value
            EventBus.getDefault().postSticky(PlaybackStatusEvent())
        }

    val dataListener: IcyDataSource.Listener?
        get() = mCurrentChannel

    fun togglePlayStop(channelNumber: Int) {
        if (!app.serviceBound) {
            setChannel(channelNumber)
        } else {
            val playerIntent = Intent(app, MediaPlayerService::class.java)
            playerIntent.action = MediaPlayerService.ACTION_TOGGLE
            app.startService(playerIntent)
        }
    }

    fun setChannel(channelNumber: Int) {
        mCurrentChannel = channels[channelNumber]
        mCurrentChannel.updateUI()

        val playerIntent = Intent(app, MediaPlayerService::class.java)
        playerIntent.putExtra(MediaPlayerService.KEY_EXTRA_MEDIA, mCurrentChannel.streamUrl)

        if (!app.serviceBound) {
            app.startService(playerIntent)
            app.bindService(playerIntent, app.serviceConnection, Context.BIND_AUTO_CREATE)
        } else {
            playerIntent.action = MediaPlayerService.ACTION_SWITCH_CHANNEL
            app.startService(playerIntent)

            // TODO updateUI()
        }
    }

    var volume: Int = 100
        get
        set(value) {
            field = value
            val playerIntent = Intent(app, MediaPlayerService::class.java)
            playerIntent.action = MediaPlayerService.ACTION_VOLUME
            playerIntent.putExtra(MediaPlayerService.KEY_EXTRA_VOLUME, volume)
            app.startService(playerIntent)
        }


    fun artist() = mCurrentChannel.artist
    fun title() = mCurrentChannel.title
    fun cover() = mCurrentChannel.cover

}