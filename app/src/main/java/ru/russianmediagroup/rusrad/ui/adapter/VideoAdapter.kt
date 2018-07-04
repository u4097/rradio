package ru.russianmediagroup.rusrad.ui.adapter

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.github.curioustechizen.ago.RelativeTimeTextView
import org.jetbrains.anko.find
import ru.russianmediagroup.rusrad.R
import ru.russianmediagroup.rusrad.network.models.Video
import ru.russianmediagroup.rusrad.ui.entity.VideoEntity


/**
 *  @author Arthur Korchagin on 16.06.17.
 */
class VideoAdapter(val video: VideoEntity) : RecyclerView.Adapter<VideoAdapter.VideoHolder>() {


    override fun onBindViewHolder(holder: VideoHolder, position: Int) {
        holder.bind(video.videoList[position])
    }

    override fun getItemCount() = video.videoList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder =
            VideoHolder(LayoutInflater.from(parent?.context).inflate(R.layout.li_video, parent, false) as ViewGroup)

    class VideoHolder(item: ViewGroup) : RecyclerView.ViewHolder(item) {

        private val tvDate: RelativeTimeTextView = item.find(R.id.li_video_tv_date)
        private val tvTitle: TextView = item.find(R.id.li_video_tv_title)
        private val ivCover: ImageView = item.find(R.id.li_video_iv_cover)
        private val vRoot: View = item.find(R.id.li_video_root)

        init {
            vRoot.setOnClickListener { onVideoClick() }
        }

        private fun onVideoClick() {
            mVideo?.video_id?.let(this::watchYoutubeVideo)
        }

        private var mVideo: Video? = null
        private val mContext = item.context

        fun bind(video: Video) {
            mVideo = video

            tvTitle.text = video.name

            video.published_at?.time?.let(tvDate::setReferenceTime)

            Glide.with(mContext)
                    .load("https://i.ytimg.com/vi/${video.video_id}/mqdefault.jpg")
//                    .load("https://img.youtube.com/vi/${video.video_id}/maxresdefault.jpg")
                    .asBitmap()
                    .placeholder(R.drawable.img_placeholder)
                    .animate(R.anim.fade_in)
                    .centerCrop()
                    .into(ivCover)
        }

        fun watchYoutubeVideo(id: String) {
            val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id))
            val webIntent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v=" + id))
            try {
                mContext.startActivity(appIntent)
            } catch (ex: ActivityNotFoundException) {
                mContext.startActivity(webIntent)
            }

        }

    }


}