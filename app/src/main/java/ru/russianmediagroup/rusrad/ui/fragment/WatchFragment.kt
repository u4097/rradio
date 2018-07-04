package ru.russianmediagroup.rusrad.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import kotlinx.android.synthetic.main.fmt_video.*
import rmg.droid.rmgcore.app.CoreRMGApp
import rmg.droid.rmgcore.media.MediaPlayerService
import ru.russianmediagroup.rusrad.R
import ru.russianmediagroup.rusrad.app.RusRadioApp
import ru.russianmediagroup.rusrad.network.NetworkServiceImpl
import ru.russianmediagroup.rusrad.network.OnResultListener
import ru.russianmediagroup.rusrad.network.models.ActiveTranslation
import ru.russianmediagroup.rusrad.network.models.Video
import ru.russianmediagroup.rusrad.ui.activity.MusicActivity
import ru.russianmediagroup.rusrad.ui.activity.VideoActivity
import ru.russianmediagroup.rusrad.ui.adapter.VideoAdapter
import ru.russianmediagroup.rusrad.ui.entity.VideoEntity


/**
 * @author by Arthur Korchagin on 05.12.17.
 */
class WatchFragment : BaseRusFragment(), PlayerManager.OnPlayerListener, OnResultListener<VideoEntity> {

    companion object {
        fun newInstance() = WatchFragment()
    }

    private val app: CoreRMGApp?
        get() = (activity?.application as? CoreRMGApp)

    private val playerManager by lazy { PlayerManager() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fmt_video, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app?.analyticsService?.logScreen(getString(R.string.label_watch))

        (activity as? MusicActivity)?.selectButton(R.id.btnWatch)

        if (app?.serviceBound == true) {
            val playerIntent = Intent(app, MediaPlayerService::class.java)
            playerIntent.action = MediaPlayerService.ACTION_CLOSE
            app?.startService(playerIntent)
        }

        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        fmtVideoTvNow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_now_translation, 0, 0, 0)

        fmtVideoBtnFullscreen.setOnClickListener {

            activity?.apply {
                startActivity(Intent(this, VideoActivity::class.java))
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        fmtVideoProgress?.visibility = View.VISIBLE
        fmtVideoExoView?.visibility = View.GONE
        fmtVideoLPlayback?.visibility = View.GONE

        Single.zip(RusRadioApp.inst.networkService.loadVideos(), RusRadioApp.inst.networkService.loadVideoUrl(),
                BiFunction { videos: List<Video>, translation: ActiveTranslation -> VideoEntity(videos, translation) })
                .subscribe(this::onResult, this::onError)
                .unsubscribeOnDestroy()
    }

    private fun startTranslation(url: String) {
        context?.let { playerManager.init(it, fmtVideoExoView, this, url) }
    }

    override fun onPause() {
        super.onPause()
        playerManager.reset()
    }

    override fun onDestroy() {
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        playerManager.release()
        super.onDestroy()
    }

    override fun onLoading() {
        fmtVideoProgress?.visibility = View.VISIBLE
        fmtVideoTvError?.visibility = View.GONE
        fmtVideoNoTranslation?.visibility = View.GONE
    }

    override fun onError(error: String) {
        fmtVideoProgress?.visibility = View.GONE
        fmtVideoTvError?.visibility = View.VISIBLE
        fmtVideoNoTranslation?.visibility = View.GONE
    }

    override fun onPlay() {
        fmtVideoExoView?.visibility = View.VISIBLE
        fmtVideoLPlayback?.visibility = View.VISIBLE
        fmtVideoProgress?.visibility = View.GONE
        fmtVideoTvError?.visibility = View.GONE
        fmtVideoNoTranslation?.visibility = View.GONE
    }

    override fun onIdle() {
        context?.let {
            playerManager.restart()
        }
    }

    override fun onEnded() {
        context?.let {
            playerManager.restart()
        }
    }

    override fun onResult(result: VideoEntity) {

        fmtVideoRvVideos.layoutManager = LinearLayoutManager(context)
        fmtVideoRvVideos.adapter = VideoAdapter(result)
        fmtVideoRvVideos.adapter.notifyDataSetChanged()

        val activeTranslation = result.translation

        val url = activeTranslation.primary_stream_url
        if (url != null && activeTranslation.show_time?.isBlank() == false) {
            startTranslation(url)
        } else {
            fmtVideoProgress?.visibility = View.GONE
            fmtVideoTvError?.visibility = View.GONE
            fmtVideoNoTranslation?.visibility = View.VISIBLE
        }
    }

    override fun onError(error: Throwable) {
        fmtVideoProgress?.visibility = View.GONE
        fmtVideoTvError?.visibility = View.VISIBLE
    }

}