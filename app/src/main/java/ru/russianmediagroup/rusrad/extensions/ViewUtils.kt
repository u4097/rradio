package ru.russianmediagroup.rusrad.extensions

import android.content.Context
import android.graphics.Point
import android.view.WindowManager
import ru.russianmediagroup.rusrad.app.RusRadioApp

object ViewUtils {

    val screenWidth by lazy { getSize(RusRadioApp.inst).x }
    val screenHeight by lazy { getSize(RusRadioApp.inst).y }

    private fun getSize(context: Context): Point {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val outSize = Point()
        display.getSize(outSize)
        return outSize
    }
}