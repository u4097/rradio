package rmg.droid.rmgcore.media.player

import com.google.android.exoplayer2.ExoPlaybackException

/**
 * @author Arthur Korchagin on 10.06.17
 */

interface ExoWrapperListener {
    fun onStateChanged(isPlaying: Boolean)
    fun onCompletion()
    fun onError(exoPlaybackError: ExoPlaybackException)
}