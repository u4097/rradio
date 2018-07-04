package ru.russianmediagroup.rusrad.ui.fragment

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fmt_contest.*
import rmg.droid.rmgcore.app.CoreRMGApp
import ru.russianmediagroup.rusrad.R
import ru.russianmediagroup.rusrad.app.RusRadioApp
import ru.russianmediagroup.rusrad.network.NetworkServiceImpl
import ru.russianmediagroup.rusrad.network.OnResultListener
import ru.russianmediagroup.rusrad.network.models.Action
import ru.russianmediagroup.rusrad.ui.activity.MusicActivity
import ru.russianmediagroup.rusrad.ui.adapter.ActionsPagerAdapter


/**
 *  @author Arthur Korchagin on 16.06.17.
 */
class ContestFragment : BaseRusFragment(), OnResultListener<List<Action>> {

    private var actionsPagerAdapter: ActionsPagerAdapter? = null

    private var mActions: List<Action>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fmt_contest, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val app = activity?.application as? CoreRMGApp
        app?.analyticsService?.logScreen(getString(R.string.label_contests))
        (activity as? MusicActivity)?.selectButton(R.id.btnContest)
        btnRetry.setOnClickListener { load() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        load()
    }

    private fun load() {
        if (actionsPagerAdapter == null) {
            vProgress.visibility = VISIBLE
            vProgress.show()

            val actions = mActions
            if (actions != null && actions.isNotEmpty()) {
                onResult(actions)
            } else {
                RusRadioApp.inst.networkService.loadActions()
                        .subscribe(this::onResult, this::onError)
                        .unsubscribeOnDestroy()
            }
        }
    }

    override fun onResult(result: List<Action>) {
        mActions = result
        val ac = activity
        if (ac != null) {
            setAdapter(ac, result)
        }
    }

    private fun setAdapter(ac: FragmentActivity, result: List<Action>) {
        actionsPagerAdapter = ActionsPagerAdapter(ac, result)
        vpActions.adapter = actionsPagerAdapter
        piActions.setViewPager(vpActions)

        tvConnectionError.visibility = GONE
        btnRetry.visibility = GONE
        vpActions.visibility = VISIBLE
        vProgress.hide()
        vProgress.visibility = GONE
    }

    override fun onError(error: Throwable) {

        if (isVisible) {
            context?.let {
                Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
            }
            tvConnectionError.visibility = VISIBLE

            btnRetry.visibility = VISIBLE
            vpActions.visibility = GONE
            vProgress.hide()
            vProgress.visibility = GONE
        }
    }

    companion object {
        fun newInstance() = ContestFragment()
    }
}