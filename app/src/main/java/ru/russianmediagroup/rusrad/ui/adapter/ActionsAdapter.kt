package ru.russianmediagroup.rusrad.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import org.jetbrains.anko.find
import ru.russianmediagroup.rusrad.R
import ru.russianmediagroup.rusrad.network.models.Action
import ru.russianmediagroup.rusrad.ui.glide.RoundedCornersTransformation


/**
 * Created by lliepmah on 25.09.17
 */

typealias OnActionMoreListener = (Action) -> Unit

class ActionsAdapter(private val items: List<Action>,
                     private val listener: OnActionMoreListener) : RecyclerView.Adapter<ActionsAdapter.ActionHolder>() {

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ActionHolder, position: Int) = holder.bindDay(items[position])


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionHolder =
            ActionHolder(LayoutInflater.from(parent.context).inflate(R.layout.l_action, parent, false) as ViewGroup, listener)


    class ActionHolder(item: ViewGroup, private val listener: (Action) -> Unit) : RecyclerView.ViewHolder(item) {

        private var mAction: Action? = null

        val context = item.context
        val imgChannelCover: ImageView = item.find(R.id.imgChannelCover)
        val tvContestLabel: TextView = item.find(R.id.tvContestLabel)
        val tvContestDescription: TextView = item.find(R.id.tvContestDescription)
        val btnDetails: Button = item.find(R.id.btnDetails)

        init {
            btnDetails.setOnClickListener { mAction?.let(listener) }
        }

        fun bindDay(action: Action) {
            mAction = action
            action.apply {

                Glide.with(context)

                        .load(picture_path)
                        .asBitmap()
                        .placeholder(R.drawable.img_placeholder)
                        .animate(R.anim.fade_in)
                        .transform(RoundedCornersTransformation(context, 20, 5))
                        .into(imgChannelCover)

                tvContestLabel.text = name
                tvContestDescription.text = body
            }
        }

    }
}