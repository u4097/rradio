package ru.russianmediagroup.rusrad.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import rmg.droid.rmgcore.entity.Stub
import ru.russianmediagroup.rusrad.R
import ru.russianmediagroup.rusrad.extensions.onChangeDo

/**
 * @author by Arthur Korchagin on 28.04.18.
 */
class IndicatorView : View {

    private var drawableGrey = ContextCompat.getDrawable(context, R.drawable.ic_indicator_line_grey)!!
    private var drawableWhite = ContextCompat.getDrawable(context, R.drawable.ic_indicator_line_white)!!
    private var internalPadding = context.resources.getDimensionPixelOffset(R.dimen.spacing_small)

    var selected by onChangeDo(0, ::invalidate)
    var count by onChangeDo(1, ::invalidate)

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context, attrs)
    }

    @Suppress("unused")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initView(context, attrs)
    }

    private fun initView(context: Context, attrs: AttributeSet? = null) = Stub

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val lineWidth = width / count - internalPadding * 2

        var drawable: Drawable
        for (i in 0 until count) {
            drawable = if (i == selected) drawableWhite else drawableGrey
            val left = i * (lineWidth + internalPadding * 2)
            drawable.setBounds(left + internalPadding, 0, left + internalPadding + lineWidth, height)
            drawable.draw(canvas)
        }
    }
}
