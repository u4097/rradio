package ru.russianmediagroup.rusrad.ui.fragment

import android.os.Bundle
import android.support.annotation.StringRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fmt_radio.*
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.textResource
import rmg.droid.rmgcore.entity.Channel
import rmg.droid.rmgcore.entity.Stub
import rmg.droid.rmgcore.extensions.*
import rmg.droid.rmgcore.media.MediaPlayerService
import rmg.droid.rmgcore.preferences.isSoundDisliked
import rmg.droid.rmgcore.preferences.isSoundLiked
import rmg.droid.rmgcore.preferences.prefs
import rmg.droid.rmgcore.ui.fragment.BaseMediaFragment
import ru.russianmediagroup.rusrad.R
import ru.russianmediagroup.rusrad.network.models.Settings
import ru.russianmediagroup.rusrad.ui.activity.MusicActivity

/**
 * Created by Arthur Korchagin on 10.06.17
 */
class RadioFragment : BaseMediaFragment() {

    override var mVolume: Int
        get() = mMediaRepository.volume
        set(value) {
            mMediaRepository.volume = value
        }

    override val timelineEnabled: Boolean
        get() = resources.getBoolean(R.bool.timeline_enabled)

    override val mCoverLayoutRes = R.layout.l_cover

    // TODO Temporary!!!
    protected val ac: MusicActivity?
        get() = (activity as? MusicActivity)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val fragmentView = inflater.inflate(R.layout.fmt_radio, container, false)
        return fragmentView
    }

    private var mIsShowMetatags = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initListeners()
        (activity as? MusicActivity)?.selectButton(R.id.btnRadio)

        mIsShowMetatags = context!!.prefs.getBoolean(Settings.SHOW_METATAGS, true)

        tabLayout.setupWithViewPager(pagerStations)

        pagerStations.adapter = mChannelsPagerAdapter
        pagerStations.currentItem = mMediaRepository.currentChannel

        tabLayout.requestLayout()
        pagerStations.useScale()
        seekVolume.progress = mVolume

        tabLayout.performAfterOnLayout {
            getTabAt(mMediaRepository.currentChannel)?.select()
        }

        mAnalytics.logRadio(mMediaRepository.channel.channelName)
        updateToolbar()
    }

    // TODO temporary
    fun updateToolbar() {
        ac?.musicToolbar?.imageViewLogo?.setImageResource(R.drawable.logo_group_toolbar)
        ac?.musicToolbar?.showMenuButton()
        ac?.buttonBack?.visibility = View.GONE
    }

    private fun initListeners() {
        seekVolume.onSeekChange { mMediaRepository.volume = it }

        pagerStations.setListener {
            playAudio(it)
            mAnalytics.logRadio(mMediaRepository.channel.channelName)
        }

        btnPlayStop.setOnClickListener {
            togglePlayStop()
        }

        btnDislike.setOnClickListener {
            if (trackIsNotDetected()) {
                Toast.makeText(context, R.string.error_cannot_like, Toast.LENGTH_LONG).show()
            } else {
                btnDislike.isSelected = !btnDislike.isSelected
                setDisliked(btnDislike.isSelected)
                flipDislike()
                val channel = mMediaRepository.channel
                mAnalytics.dislikeSong(channel)
            }
        }

        btnLike.setOnClickListener {
            if (trackIsNotDetected()) {
                Toast.makeText(context, R.string.error_cannot_like, Toast.LENGTH_LONG).show()
            } else {
                btnLike.isSelected = !btnLike.isSelected
                setLiked(btnLike.isSelected)
                flipLike()
                val channel = mMediaRepository.channel
                mAnalytics.likeSong(channel)
            }
        }
    }

    private fun flipLike() {
        context?.outAnimation(AnimationType.FLIP_FAST)?.playWith(btnLike) {
            btnLike?.imageResource = if (btnLike.isSelected)
                R.drawable.ic_like_selected else R.drawable.ic_like
            context?.inAnimation(AnimationType.FLIP_FAST)?.playWith(btnLike)
        }
    }

    private fun flipDislike() {
        context?.outAnimation(AnimationType.FLIP_FAST)?.playWith(btnDislike) {
            btnDislike?.imageResource = if (btnDislike.isSelected)
                R.drawable.ic_dislike_selected else R.drawable.ic_dislike
            context?.inAnimation(AnimationType.FLIP_FAST)?.playWith(btnDislike)
        }
    }

    private fun initUI() {
        btnDislike.imageResource = R.drawable.ic_dislike
        btnLike.imageResource = R.drawable.ic_like

        tvArtist.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_microphone_small_grey, 0, 0, 0)
    }

    override fun setTimeline(progress: Int, timeProgress: String) = Stub

    override fun updatePlayback() {
        btnPlayStop.imageResource = if (mMediaRepository.isPlaying)
            R.drawable.ic_pause_ui else R.drawable.ic_play_ui

        when (mMediaRepository.playbackState) {
            MediaPlayerService.PlaybackState.PLAYING -> onChannelUpdated(mMediaRepository.channel)
            MediaPlayerService.PlaybackState.PAUSED -> onChanelIdle(R.string.label_paused)
            MediaPlayerService.PlaybackState.LOADING -> onChanelIdle(R.string.label_loading)
            MediaPlayerService.PlaybackState.ERROR -> onChanelIdle(R.string.label_error_loading)
        }
    }

    private fun onChanelIdle(@StringRes titleRes: Int) {
        tvTitle.textResource = titleRes
        tvArtist.text = mMediaRepository.channel.defaultArtist
    }

    override fun onChannelUpdated(channel: Channel) {
        // TODO This is shit code!
        tvTitle.text = if (mIsShowMetatags || mMediaRepository.channel.isLive) mMediaRepository.title() else mMediaRepository.channel.defaultTitle
        tvArtist.text = if (mIsShowMetatags || mMediaRepository.channel.isLive) mMediaRepository.artist() else mMediaRepository.channel.defaultArtist

        val track = channel.track

        btnDislike.isSelected = !track.isDefault && context?.isSoundDisliked(track) == true
        btnDislike.imageResource = if (btnDislike.isSelected) R.drawable.ic_dislike_selected else R.drawable.ic_dislike

        btnLike.isSelected = !track.isDefault && context?.isSoundLiked(track) == true
        btnLike.imageResource = if (btnLike.isSelected) R.drawable.ic_like_selected else R.drawable.ic_like

        mChannelsPagerAdapter.replace(mMediaRepository.channels.indexOf(channel), channel.cover)
    }

    companion object {
        fun newInstance() = RadioFragment()
    }

}

