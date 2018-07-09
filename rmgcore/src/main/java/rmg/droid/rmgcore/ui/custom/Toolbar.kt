package rmg.droid.rmgcore.ui.custom

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.support.annotation.AttrRes
import android.support.annotation.StyleRes
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.v_toolbar.view.*
import rmg.droid.rmgcore.R

/**
 * Created by Arthur Korchagin on 10.06.17
 */

class Toolbar : FrameLayout {

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int, @StyleRes defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet? = null) {
        LayoutInflater.from(context).inflate(R.layout.v_toolbar, this)
    }

    fun setOpenMenuListener(listener: () -> Unit) {
        btnMenu.setOnClickListener { listener.invoke() }
    }

    fun hideMenuButton() {
        btnMenu?.visibility = View.GONE
    }

}
