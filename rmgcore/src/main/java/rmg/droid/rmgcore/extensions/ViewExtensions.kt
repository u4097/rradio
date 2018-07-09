package rmg.droid.rmgcore.extensions

import android.widget.SeekBar

/**
 *  @author Arthur Korchagin on 15.06.17.
 */
fun SeekBar.onSeekChange(listener: (progress: Int) -> Unit) {

    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) =
                listener.invoke(progress)

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}

    })
}