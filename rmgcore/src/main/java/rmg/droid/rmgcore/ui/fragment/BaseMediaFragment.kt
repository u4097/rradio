package rmg.droid.rmgcore.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import rmg.droid.montecarlo.entity.Channel
import rmg.droid.rmgcore.utils.formatDuration
import rmg.droid.rmgcore.analytics.AnalyticsManager
import rmg.droid.rmgcore.app.CoreRMGApp
import rmg.droid.rmgcore.event.PlaybackStatusEvent
import rmg.droid.rmgcore.event.UIUpdateEvent
import rmg.droid.rmgcore.services.MediaRepository
import rmg.droid.rmgcore.ui.adapter.ChannelsPagerAdapter

/**
 *  @author Arthur Korchagin on 15.06.17.
 */
abstract class BaseMediaFragment : BaseFragment() {

    abstract val mCoverLayoutRes: Int
    abstract var mVolume: Int
    abstract val timelineEnabled: Boolean

    abstract protected fun setTimeline(progress: Int, timeProgress: String)
    abstract fun updatePlayback()
    abstract fun onChannelUpdated(channel: Channel)

    protected var mChannelNumber: Int = 0

    protected lateinit var mMediaRepository: MediaRepository
    protected lateinit var mChannelsPagerAdapter: ChannelsPagerAdapter
    protected lateinit var mAnalytics: AnalyticsManager

    private val mHandler = Handler(Looper.getMainLooper())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(mCoverLayoutRes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val app = activity!!.application as CoreRMGApp
        mMediaRepository = app.mediaRepository
        mAnalytics = app.analyticsManager

        val channels = mMediaRepository.channels
        mChannelsPagerAdapter = ChannelsPagerAdapter(
                context = activity!!,
                layoutRes = mCoverLayoutRes,
                covers = channels.map { it.cover }.toMutableList(),
                titles = channels.map { it.channelName })

        mVolume = mMediaRepository.volume

        playAudio(mChannelNumber)

    }

    override fun onResume() {
        super.onResume()
        if (timelineEnabled)
            updates()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    protected fun togglePlayStop() {
        mMediaRepository.togglePlayStop(mChannelNumber)
    }

    protected fun playAudio(channelNumber: Int) {
        mMediaRepository.setChannel(channelNumber)
    }

    protected fun updates() {
        if (!timelineEnabled || !isResumed || !mMediaRepository.isPlaying) return
        mHandler.postDelayed({
            mMediaRepository.channels[mChannelNumber].apply {
                val out = mMediaRepository.outOfSync
                val playedMills: Long = (System.currentTimeMillis() - out - startTime.time)
                val progress = (100 * playedMills / duration).toInt()
                setTimeline(progress, timeProgress = playedMills.formatDuration())
            }
            updates()
        }, 1000)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onPlaybackStatusEvent(event: PlaybackStatusEvent) {
        EventBus.getDefault().removeStickyEvent(event)
        updates()
        updatePlayback()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: UIUpdateEvent) {
        EventBus.getDefault().removeStickyEvent(event)
        onChannelUpdated(event.channel)
    }



}