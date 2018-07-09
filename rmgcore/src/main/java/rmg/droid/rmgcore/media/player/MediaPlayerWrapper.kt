package rmg.droid.rmgcore.media.player

import android.content.Context

import java.io.IOException

/**
 * @author Arthur Korchagin on 10.06.17
 */

interface MediaPlayerWrapper {

    val duration: Long
    val currentPosition: Long
    val isPlaying: Boolean

    @Throws(IOException::class)
    fun setDataSource(url: String)

    fun setWakeMode(context: Context, mode: Int)
    fun setVolume(audioVolume: Float)

    fun pause()
    fun resume()
    fun stop()
    fun release()
    fun seekTo(offsetMs: Long)

}