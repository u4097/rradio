package ru.russianmediagroup.rusrad.ui.adapter

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import ru.russianmediagroup.rusrad.network.models.Poll
import ru.russianmediagroup.rusrad.network.models.PollOption
import ru.russianmediagroup.rusrad.ui.custom.CompetitorView

/**
 * Created by lliepmah on 26.09.17
 */
class CompetitorsPagerAdapter(private val poll: Poll) : PagerAdapter() {

    private var pollOptions: List<PollOption> = poll.items

    private var mCurrentPosition: Int? = null

    override fun instantiateItem(viewGroup: ViewGroup, position: Int) =
            CompetitorView(viewGroup.context, pollOptions[position], poll)
                    .apply(viewGroup::addView)

    override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
        mCurrentPosition = position
    }

    override fun getCount() = pollOptions.size
    override fun isViewFromObject(view: View, any: Any): Boolean = view === any
    override fun destroyItem(viewGroup: ViewGroup, position: Int, view: Any) = viewGroup.removeView(view as View)

}