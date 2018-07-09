package rmg.droid.rmgcore.media.player

import android.content.Context
import android.net.Uri
import android.os.PowerManager
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import rmg.droid.rmgcore.media.icy.IcyDataSource
import rmg.droid.rmgcore.media.icy.IcyDataSourceFactory
import java.io.IOException
import java.util.*


/**
 * @author Arthur Korchagin on 10.06.17
 */

class ExoPlayerWrapper(context: Context) : MediaPlayerWrapper, IcyDataSource.Listener, ExoPlayer.EventListener {

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSeekProcessed() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    private val mExoPlayer: SimpleExoPlayer

    private var mWakeLock: PowerManager.WakeLock? = null

    var exoWrapperListener: ExoWrapperListener? = null
    var dataListener: IcyDataSource.Listener? = null

    init {
        val bandwidthMeter = DefaultBandwidthMeter()
        val trackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(trackSelectionFactory)

        mExoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector)
    }

    override fun onMetaData(artist: String, title: String) {
        dataListener?.onMetaData(artist, title)
    }

    override fun onServerDate(serverDate: Date?) {
        dataListener?.onServerDate(serverDate)
    }

    @Throws(IOException::class)
    override fun setDataSource(url: String) {

        val uri = Uri.parse(url)

        val dataSourceFactory = IcyDataSourceFactory(this)
        val extractorsFactory = DefaultExtractorsFactory()
        val radioChannelSource = ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null)

        mExoPlayer.addListener(this)

        mExoPlayer.prepare(radioChannelSource)

        mExoPlayer.playWhenReady = true

    }

    override fun seekTo(offsetMs: Long) {
        mExoPlayer.seekTo(offsetMs)
    }

    override val duration = mExoPlayer.duration

    override val currentPosition: Long
        get() = mExoPlayer.currentPosition

    override fun resume() {
        mExoPlayer.playWhenReady = true
        stayAwake(true)
    }

    override fun stop() {
        mExoPlayer.stop()
        stayAwake(false)
    }

    override fun pause() {
        mExoPlayer.playWhenReady = false
        stayAwake(false)
    }

    override fun release() {
        mExoPlayer.release()
        stayAwake(false)
    }

    override val isPlaying: Boolean
        get() = mExoPlayer.playbackState == ExoPlayer.STATE_READY

    override fun setVolume(audioVolume: Float) {
        mExoPlayer.volume = audioVolume
    }

    override fun setWakeMode(context: Context, mode: Int) {
        var washeld = false
        if (mWakeLock != null) {
            if (mWakeLock!!.isHeld) {
                washeld = true
                mWakeLock!!.release()
            }
            mWakeLock = null
        }

        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock = pm.newWakeLock(mode or PowerManager.ON_AFTER_RELEASE, EXO_PLAYER)
        mWakeLock!!.setReferenceCounted(false)
        if (washeld) {
            mWakeLock!!.acquire()
        }
    }

    private fun stayAwake(awake: Boolean) {
        if (mWakeLock != null) {
            if (awake && !mWakeLock!!.isHeld) {
                mWakeLock!!.acquire()
            } else if (!awake && mWakeLock!!.isHeld) {
                mWakeLock!!.release()
            }
        }
    }

    override fun toString(): String {
        return EXO_PLAYER
    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {/**/
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) { /**/
    }

    override fun onLoadingChanged(isLoading: Boolean) { /**/
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        if (exoWrapperListener == null) return

        when (playbackState) {
            ExoPlayer.STATE_BUFFERING -> {
                exoWrapperListener?.onStateChanged(false)

            }
            ExoPlayer.STATE_ENDED -> {
                exoWrapperListener!!.onCompletion()
            }
            ExoPlayer.STATE_IDLE -> {
                exoWrapperListener!!.onCompletion()
            }
            ExoPlayer.STATE_READY -> {
                exoWrapperListener!!.onStateChanged(playWhenReady)
            }
            else -> {
            }
        }


    }

    override fun onPlayerError(error: ExoPlaybackException) {
        if (exoWrapperListener != null) {
            exoWrapperListener!!.onError(error)
        }
    }

    override fun onPositionDiscontinuity(reason: Int) {
    }


    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}

    companion object {
        val EXO_PLAYER = "ExoPlayer"
    }
}