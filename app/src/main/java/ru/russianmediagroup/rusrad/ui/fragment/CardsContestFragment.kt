package ru.russianmediagroup.rusrad.ui.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fmt_competitor.*
import rmg.droid.rmgcore.app.CoreRMGApp
import ru.russianmediagroup.rusrad.R
import ru.russianmediagroup.rusrad.network.models.Poll
import ru.russianmediagroup.rusrad.ui.activity.MusicActivity
import ru.russianmediagroup.rusrad.ui.adapter.CompetitorsPagerAdapter
import ru.russianmediagroup.rusrad.ui.custom.DepthTransformation

/**
 * @author by Arthur Korchagin on 17.04.18
 */
class CardsContestFragment : BaseRusFragment() {

    companion object {
        private const val KEY_POLL = "poll"
        private const val KEY_OPTION_ID = "option_id"

        fun newInstance(poll: Poll, optionId: String) = CardsContestFragment().apply {
            arguments = Bundle().apply {
                putParcelable(KEY_POLL, poll)
                putString(KEY_OPTION_ID, optionId)
            }
        }
    }

    private var mCompetitorsPagerAdapter: CompetitorsPagerAdapter? = null

    private val app: CoreRMGApp?
        get() = (activity?.application as? CoreRMGApp)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fmt_competitor, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        app?.analyticsService?.logScreen(getString(R.string.label_poll))
        (activity as? MusicActivity)?.selectButton(R.id.btnMore)

        vpCompetitor.setPageTransformer(false, DepthTransformation)
    }


    override fun updateToolbar() {
        ac?.musicToolbar?.hideMenuButton()
        ac?.buttonBack?.visibility = View.VISIBLE
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val poll: Poll = arguments?.getParcelable(KEY_POLL) ?: throw Error("No Poll")
        val optionId = arguments?.getString(KEY_OPTION_ID) ?: throw Error("No Option Id")

        mCompetitorsPagerAdapter = CompetitorsPagerAdapter(poll)
        vpCompetitor.adapter = mCompetitorsPagerAdapter

        vpCompetitor.currentItem = poll.items.indexOfFirst { TextUtils.equals(it.id, optionId) }
    }

}