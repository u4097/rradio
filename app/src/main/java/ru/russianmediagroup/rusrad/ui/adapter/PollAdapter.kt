package ru.russianmediagroup.rusrad.ui.adapter

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import org.jetbrains.anko.find
import ru.russianmediagroup.rusrad.R
import ru.russianmediagroup.rusrad.network.models.Poll
import ru.russianmediagroup.rusrad.network.models.PollOption
import java.util.*

/**
 * @author by Arthur Korchagin on 16.04.18.
 */

typealias OnPollClickListener = (PollOption) -> Unit

typealias OnPollLikeListener = (PollOption) -> Unit

class PollAdapter(private val poll: Poll,
                  private val listener: OnPollClickListener,
                  private val likeListener: OnPollLikeListener) : RecyclerView.Adapter<PollAdapter.PollHolder>() {

    private val mPollOptions: MutableList<PollOption> = LinkedList()

    init {
        mPollOptions.addAll(poll.items)
    }

    override fun getItemCount() = mPollOptions.size + 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            LayoutInflater.from(parent.context).inflate(viewType, parent, false)
                    .let { it as ViewGroup }
                    .let {
                        when (viewType) {
                            R.layout.li_poll_header -> PollHeaderHolder(it)
                            R.layout.li_poll_item -> PollItemHolder(it, listener, likeListener)
                            else -> throw Error("Unknown View Type")
                        }
                    }

    override fun getItemViewType(position: Int) =
            if (position == 0) R.layout.li_poll_header else R.layout.li_poll_item

    override fun onBindViewHolder(holder: PollHolder, position: Int) = when (position) {
        0 -> holder.bind(poll)
        else -> holder.bind(mPollOptions[position - 1])
    }

    fun replaceOption(pollOption: PollOption) {
        val index = mPollOptions.indexOfFirst { TextUtils.equals(it.id, pollOption.id) }
        mPollOptions.removeAt(index)
        mPollOptions.add(index, pollOption)

        mPollOptions.sortByDescending(PollOption::rating)
        mPollOptions.forEachIndexed { index, pollOption ->
            val newPos = index + 1
            if (newPos > pollOption.position) {
                pollOption.dynamic = -1
            } else if (newPos < pollOption.position) {
                pollOption.dynamic = 1
            }
            pollOption.position = newPos
        }

        notifyDataSetChanged()
    }

    abstract class PollHolder(item: ViewGroup) : RecyclerView.ViewHolder(item) {

        open fun bind(poll: Poll) = Unit

        open fun bind(option: PollOption) = Unit

    }

    class PollHeaderHolder(item: ViewGroup) : PollHolder(item) {

        private val ivCover: ImageView = item.find(R.id.liPollHeaderIvCover)
        private val tvDate: TextView = item.find(R.id.liPollHeaderTvDate)
        private val tvLabel: TextView = item.find(R.id.liPollHeaderTvLabel)
        private val context = item.context

        override fun bind(poll: Poll) {

            tvDate.text = poll.subtitle
            tvLabel.text = poll.name

            Glide.with(context)
                    .load(poll.bg.url)
                    .asBitmap()
                    .placeholder(R.drawable.bg_placeholder)
                    .animate(R.anim.fade_in)
                    .into(ivCover)
        }
    }

    class PollItemHolder(item: ViewGroup, listener: OnPollClickListener, likeListener: OnPollLikeListener) : PollHolder(item) {

        private var pollOption: PollOption? = null

        private val liPollItemRoot: View = item.find(R.id.liPollItemRoot)
        private val liPollItemVUp: View = item.find(R.id.liPollItemVUp)
        private val liPollItemVDown: View = item.find(R.id.liPollItemVDown)
        private val liPollItemTvPlace: TextView = item.find(R.id.liPollItemTvPlace)
        private val liPollItemIvAvatar: ImageView = item.find(R.id.liPollItemIvAvatar)
        private val liPollItemTvName: TextView = item.find(R.id.liPollItemTvName)
        private val liPollItemTvLikes: TextView = item.find(R.id.liPollItemTvLikes)

        private val context = item.context

        init {
            liPollItemRoot.setOnClickListener { pollOption?.let(listener) }
            liPollItemTvLikes.setOnClickListener { pollOption?.let(likeListener) }
        }

        override fun bind(option: PollOption) {
            pollOption = option

            liPollItemVUp.visibility = if (option.dynamic > 0) View.VISIBLE else View.GONE
            liPollItemVDown.visibility = if (option.dynamic < 0) View.VISIBLE else View.GONE

            liPollItemTvPlace.text = option.position.toString()
            liPollItemTvLikes.text = option.rating.toString()
            liPollItemTvName.text = option.poll_item?.title ?: ""

            val heart = ContextCompat.getDrawable(context, if (option.is_voted) R.drawable.ic_heart_accent else R.drawable.ic_heart_empty_accent);
            liPollItemTvLikes.setCompoundDrawablesWithIntrinsicBounds(null, null, heart, null)

            option.poll_item?.pictures?.firstOrNull()?.url
                    ?.let {
                        Glide.with(context)
                                .load(it)
                                .asBitmap()
                                .placeholder(R.drawable.bg_placeholder)
                                .animate(R.anim.fade_in)
                                .into(liPollItemIvAvatar)
                    }
        }
    }
}