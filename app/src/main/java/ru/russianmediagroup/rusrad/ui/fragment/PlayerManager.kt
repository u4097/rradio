package ru.russianmediagroup.rusrad.ui.fragment

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import rmg.droid.rmgcore.entity.Stub
import ru.russianmediagroup.rusrad.R

/**
 * Manages the [ExoPlayer], the IMA plugin and all video playback.
 */

class PlayerManager : Player.EventListener {

    private var player: SimpleExoPlayer? = null
    private var mOnPlayerListener: OnPlayerListener? = null

    private var mContentMediaSource: MediaSource? = null

    fun init(context: Context, exoPlayerView: SimpleExoPlayerView, onPlayerListener: OnPlayerListener,
             contentUrl: String) {

        mOnPlayerListener = onPlayerListener
        // Create a default track selector.
        val bandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)

        // Create a player instance.
        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector)

        // Bind the player to the view.
        exoPlayerView.player = player
        exoPlayerView.useController = false

        val dataSourceFactory = DefaultDataSourceFactory(context,
                Util.getUserAgent(context, context.getString(R.string.app_name)))

        mContentMediaSource = HlsMediaSource(
                Uri.parse(contentUrl), dataSourceFactory, null, null)

        player?.prepare(mContentMediaSource)
        player?.playWhenReady = true
        player?.addListener(this)
    }

    fun restart() {
        player?.prepare(mContentMediaSource)
        player?.playWhenReady = true
    }

    fun reset() {
        player?.release()
        player = null
    }

    fun release() {
        player?.release()
        player = null
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        mOnPlayerListener?.onError(error?.message ?: "no")
    }

    override fun onLoadingChanged(isLoading: Boolean) = Unit

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) = Stub

    override fun onPositionDiscontinuity(reason: Int) = Unit
    override fun onSeekProcessed() = Unit
    override fun onRepeatModeChanged(repeatMode: Int) = Unit
    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) = Unit

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            STATE_IDLE -> mOnPlayerListener?.onIdle()
            STATE_BUFFERING -> mOnPlayerListener?.onLoading()
            STATE_READY -> mOnPlayerListener?.onPlay()
            STATE_ENDED -> mOnPlayerListener?.onEnded()
            else -> Unit
        }
    }


    interface OnPlayerListener {
        fun onPlay()
        fun onLoading()
        fun onError(error: String)
        fun onIdle()
        fun onEnded()
    }
}