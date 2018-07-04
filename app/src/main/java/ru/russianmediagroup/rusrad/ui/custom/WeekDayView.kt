package ru.russianmediagroup.rusrad.ui.custom

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.support.annotation.AttrRes
import android.support.annotation.StyleRes
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import org.jetbrains.anko.backgroundDrawable
import ru.russianmediagroup.rusrad.R
import java.text.DateFormatSymbols
import java.util.*

/**
 * @author Arthur Korchagin on 16.06.17.
 */

class WeekDayView : TextView, View.OnClickListener {

    var onSelectListener: ((weekDay: Int, selected: Boolean) -> Unit)? = null

    var weekdays = DateFormatSymbols(Locale.getDefault()).shortWeekdays

    var weekDay: Int = 0
        get
        set(value) {
            field = value
            text = weekdays[value] ?: ""
        }

    constructor(context: Context) : super(context) {
        initView(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context, attrs)
    }

    @Suppress("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int, @StyleRes defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initView(context, attrs)
    }

    fun initView(context: Context, attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.WeekDayView)
        weekDay = a.getInt(R.styleable.WeekDayView_wdv_day_number, weekDay)
        a.recycle()

        val desiredSp = resources.getDimension(R.dimen.text_size_medium)
        val density = resources.displayMetrics.density
        setTextSize(TypedValue.COMPLEX_UNIT_SP, desiredSp / density)

        gravity = Gravity.CENTER
        setTextColor(ContextCompat.getColorStateList(context, R.color.color_selectable))
        backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.bg_selectable)
        isClickable = true

        setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        isSelected = !isSelected
        onSelectListener?.invoke(weekDay, isSelected)
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredWidth)
    }

}
