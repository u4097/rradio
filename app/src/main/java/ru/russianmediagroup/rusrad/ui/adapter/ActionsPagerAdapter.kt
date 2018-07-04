package ru.russianmediagroup.rusrad.ui.adapter

import android.app.Activity
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import org.jetbrains.anko.find
import rmg.droid.rmgcore.extensions.openUrl
import ru.russianmediagroup.rusrad.R
import ru.russianmediagroup.rusrad.network.models.Action

/**
 * Created by lliepmah on 26.09.17
 */
class ActionsPagerAdapter(private val activity: Activity,
                          private var actions: List<Action>) : PagerAdapter() {

    private var mCurrentPosition: Int? = null

    override fun instantiateItem(viewGroup: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(activity)
        val layout = inflater.inflate(R.layout.l_action, viewGroup, false) as ViewGroup

        val imgChannelCover: ImageView = layout.find(R.id.imgChannelCover)
        val tvContestTitle: TextView = layout.find(R.id.tvContestTitle)
        val tvContestLabel: TextView = layout.find(R.id.tvContestLabel)
        val tvContestDescription: TextView = layout.find(R.id.tvContestDescription)
        val btnDetails: Button = layout.find(R.id.btnDetails)
        btnDetails.setOnClickListener { onDetailsClick() }

        val action = actions[position]

        Glide.with(activity)
                .load(action.picture_path)
                .asBitmap()
                .placeholder(R.drawable.img_placeholder)
                .animate(R.anim.fade_in)
                .centerCrop()
                .into(imgChannelCover)

        tvContestLabel.text = action.subtitle
        tvContestDescription.text = action.body
        tvContestTitle.text = action.name

        viewGroup.addView(layout)
        return layout
    }

    private fun onDetailsClick() = mCurrentPosition?.let {
        activity.openUrl(actions[it].url)
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
        mCurrentPosition = position
    }

    override fun getCount() = actions.size
    override fun isViewFromObject(view: View, any: Any): Boolean = view === any
    override fun getPageTitle(position: Int): CharSequence = actions[position].name
    override fun destroyItem(viewGroup: ViewGroup, position: Int, view: Any) = viewGroup.removeView(view as View)

}