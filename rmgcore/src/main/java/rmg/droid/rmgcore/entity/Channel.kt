package rmg.droid.montecarlo.entity

import android.support.annotation.DrawableRes
import android.util.Log
import org.greenrobot.eventbus.EventBus
import rmg.droid.rmgcore.event.NotificationUpdateEvent
import rmg.droid.rmgcore.event.UIUpdateEvent
import rmg.droid.rmgcore.media.icy.IcyDataSource
import java.util.*

/**
 *  @author Arthur Korchagin on 13.06.17.
 */

data class Channel(val channelName: String, val streamUrl: String,
                   val dataUrl: String, val defaultArtist: String,
                   val defaultTitle: String, @DrawableRes val coverRes: Int,
                   val loader: Loader, val networkUpdates: Boolean) : IcyDataSource.Listener {

    /* Covers */
    var cover = Cover(coverRes)
        private set
        get

    var coverUrl: String?
        get() = cover.url
        set(value) {
            cover = Cover(coverRes, value)
        }

    /* Tracks */
    var track = Track(defaultArtist, defaultTitle)
        private set
        get

    var artist = track.artist
        get() = track.artist

    var title = track.title
        get() = track.title

    /* Timelines */
    var timeline = Timeline()
        private set
        get

    var duration = DEFAULT_SONG_DURATION
        get() = timeline.duration ?: getDefaultDuration(isJingle(track))

    var startTime = Date()
        get() = timeline.startTime

    /* Updaters */
    fun updateUI() =
            EventBus.getDefault().postSticky(UIUpdateEvent(this))

    fun updateNotification() =
            EventBus.getDefault().post(NotificationUpdateEvent())

    override fun onMetaData(artist: String, title: String) {

        if (artist == this.artist && title == this.title) {
            updateUI()
            updateNotification()
            return
        }

        val checkTrack = Track(artist, title)
        this.track = if (isJingle(checkTrack)) Track(defaultArtist, defaultTitle) else checkTrack

        cover = Cover(coverRes)
        timeline = Timeline()

        updateUI()
        updateNotification()

        Log.d(javaClass.name, "onMetaData->& title='$artist' artist='$artist' isJingle=${isJingle(track)}")

        if (!isJingle(this.track) && networkUpdates) {

            loader.loadStartTime(this.track) {
                it?.apply {
                    Log.d(javaClass.name, "onMetaData-loadStartTime> song duration=$duration")
                    timeline = Timeline(this, duration)
                    updateUI()
                }
            }

            loader.loadSongInfo(this.track) { songCover, songDuration ->

                if (songDuration > 0) {
                    Log.d(javaClass.name, "onMetaData-> song duration=$songDuration")
                    timeline = Timeline(startTime, songDuration)
                }

                cover = Cover(coverRes, songCover)

                Log.d(javaClass.name, "onMetaData-> duration=$songDuration")

                updateUI()
                updateNotification()
            }
        }
    }

    /* Utilities */
    fun isJingle(track: Track) =
            JINGLE_NAMES.contains(track.artist.toUpperCase()) || (track.artist.isBlank() && track.title.isBlank())


    private fun getDefaultDuration(isJingle: Boolean) =
            (if (isJingle) DEFAULT_JINGLE_DURATION else DEFAULT_SONG_DURATION)

    /* Constants */
    companion object {
        val JINGLE_NAMES = arrayListOf("JINGLE", "GC")
        private val MINUTE = 1000 * 60

        private val DEFAULT_JINGLE_DURATION = 1 * MINUTE
        private val DEFAULT_SONG_DURATION = 5 * MINUTE
    }

    /* Callbacks */
    interface Loader {
        fun loadStartTime(track: Track, listener: (Date?) -> Unit)
        fun loadSongInfo(audioTrack: Track, listener: (String, Int) -> Unit)
    }


    override fun onServerDate(serverDate: Date?) {
        /**/
    }
}