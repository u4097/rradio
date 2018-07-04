package ru.russianmediagroup.rusrad.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import ru.russianmediagroup.rusrad.R
import ru.russianmediagroup.rusrad.ui.entity.AlarmChannel


/**
 *  @author Arthur Korchagin on 16.06.17.
 */
class ChannelAdapter(val items: List<AlarmChannel>, val listener: OnChannelChooseListener) : RecyclerView.Adapter<ChannelAdapter.ChannelHolder>() {

    interface OnChannelChooseListener {
        fun onChannelChoose(channel: AlarmChannel)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ChannelHolder, position: Int)
            = holder.bindDay(items[position])


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelHolder =
            ChannelHolder(LayoutInflater.from(parent.context).inflate(R.layout.l_channel, parent, false) as ViewGroup, this::onChannelChoose)

    fun onChannelChoose(channel: AlarmChannel) {
        items.forEach { it.selected = it.index == channel.index }
        notifyDataSetChanged()
        listener.onChannelChoose(channel)
    }

    class ChannelHolder(item: ViewGroup, val listener: (AlarmChannel) -> Unit) : RecyclerView.ViewHolder(item) {

        private var mChannel: AlarmChannel? = null

        val tvChannelName: TextView = item.find(R.id.tvChannelName)
        val imgChannelCover: ImageView = item.find(R.id.imgChannelCover)
        val vChannelOverlay: View = item.find(R.id.vChannelOverlay)
        val ivCheck: View = item.find(R.id.ivCheck)

        init {
            vChannelOverlay.setOnClickListener { toggleSelection() }
            tvChannelName.setOnClickListener { toggleSelection() }
        }

        fun bindDay(channel: AlarmChannel) {
            mChannel = channel
            channel.apply {
                tvChannelName.text = name
                imgChannelCover.imageResource = image
                vChannelOverlay.isSelected = selected
                tvChannelName.isSelected = selected
                ivCheck.visibility = if (selected) VISIBLE else GONE
            }
        }

        private fun toggleSelection() {
            mChannel?.let {
                if (!it.selected) {
                    listener(it)
                }
            }
        }
    }
}