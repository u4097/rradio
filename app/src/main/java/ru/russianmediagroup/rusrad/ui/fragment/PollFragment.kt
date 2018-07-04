package ru.russianmediagroup.rusrad.ui.fragment

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fmt_poll.*
import rmg.droid.rmgcore.analytics.AnalyticsService
import ru.russianmediagroup.rusrad.R
import ru.russianmediagroup.rusrad.app.RusRadioApp
import ru.russianmediagroup.rusrad.network.NetworkServiceImpl
import ru.russianmediagroup.rusrad.network.models.Poll
import ru.russianmediagroup.rusrad.network.models.PollOption
import ru.russianmediagroup.rusrad.ui.activity.MusicActivity
import ru.russianmediagroup.rusrad.ui.activity.push
import ru.russianmediagroup.rusrad.ui.adapter.PollAdapter
import ru.russianmediagroup.rusrad.ui.entity.Stateable

/**
 * @author by Arthur Korchagin on 16.04.18
 */
class PollFragment : BaseRusFragment(), Stateable<Poll>, SwipeRefreshLayout.OnRefreshListener {

    companion object {
        const val KEY_POLL_ID = "poll_id"
        const val KEY_LOGO_URL = "logo_url"

        fun newInstance(pollId: String, logoUrl: String? = null) = PollFragment().apply {
            arguments = Bundle().apply {
                putString(KEY_POLL_ID, pollId)
                putString(KEY_LOGO_URL, logoUrl)
            }
        }
    }

    private var mPollAdapter: PollAdapter? = null

    private var mPoll: Poll? = null

    private val app: RusRadioApp?
        get() = (activity?.application as? RusRadioApp)

    private val musicActivity: MusicActivity?
        get() = (activity as? MusicActivity)

    override val progressView: View? = null

    override val dataView: View
        get() = fmtPollListMenu

    override val errorView: View
        get() = lError

    private val mAnalytics: AnalyticsService?
        get() = (activity?.application as? RusRadioApp)?.analyticsService

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fmt_poll, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        app?.analyticsService?.logScreen(getString(R.string.label_poll))
        (activity as? MusicActivity)?.selectButton(R.id.btnMore)

        fmtPollSwipeRefresh.setOnRefreshListener(this)
        fmtPollSwipeRefresh.setColorSchemeResources(R.color.colorAccent)
        fmtPollListMenu.layoutManager = LinearLayoutManager(context)
        fmtPollRoot.setOnClickListener { back() }
        btnRetry.setOnClickListener { load() }
        load()

        musicActivity?.adsService?.showAd()
    }

    override fun updateToolbar() {
        super.updateToolbar()
        val ivLogo = ac?.musicToolbar?.imageViewLogo
        val logoUrl = arguments?.getString(KEY_LOGO_URL)
        if (ivLogo != null && logoUrl != null) {
            updateLogo(logoUrl, ivLogo)
        }
    }

    private fun updateLogo(logoUrl: String, ivLogo: ImageView) {
        Glide.with(context)
                .load(logoUrl)
                .asBitmap()
                .placeholder(R.drawable.logo_group_toolbar)
                .into(ivLogo)
    }

    override fun onRefresh() {
        load()
    }

    private fun load() {
        onProgress()
        RusRadioApp.inst.networkService.loadPoll(arguments?.getString(KEY_POLL_ID) ?: "")
                .subscribe(this::onSuccess, this::onError)
                .unsubscribeOnDestroy()
    }

    override fun setData(data: Poll) {
        mPoll = data
        mPollAdapter = PollAdapter(data, ::onPollOpen, ::onPollLike)
        fmtPollListMenu.adapter = mPollAdapter

        val ivLogo = ac?.musicToolbar?.imageViewLogo
        val logoUrl = mPoll?.logo?.url
        if (ivLogo != null && logoUrl != null) {
            updateLogo(logoUrl, ivLogo)
        }
    }

    private fun onPollOpen(option: PollOption) {
        val poll = mPoll
        val ac = activity

        if (ac != null && poll != null && option.id != null) {
            ac.push(CardsContestFragment.newInstance(poll, option.id))
        }
    }

    private fun onPollLike(option: PollOption) {
        if (option.id != null) {
            mPollAdapter?.replaceOption(option.copy(is_voted = true, rating = option.rating + 1))
            RusRadioApp.inst.networkService.toggleLike(option.id)
                    .subscribe({ onLikeUpdate(it) },
                            { onLikeUpdate(option, it) })
                    .unsubscribeOnDestroy()
        }
    }

    override fun setViewVisibility(progress: Boolean, dataVisibility: Boolean, error: Boolean) {
        super.setViewVisibility(progress, dataVisibility, error)
        fmtPollSwipeRefresh.isRefreshing = progress
    }

    private fun onLikeUpdate(option: PollOption, error: Throwable? = null) {
        mAnalytics?.vote(option.poll_item_id ?: "")
        mPollAdapter?.replaceOption(option)
        mPoll?.items?.findLast { it.id == option.id }?.let {
            it.rating = option.rating
            it.is_voted = option.is_voted
        }
        error?.let(::processError)
    }

}