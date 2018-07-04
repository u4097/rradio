package ru.russianmediagroup.rusrad.ui.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fmt_more.*
import rmg.droid.rmgcore.app.CoreRMGApp
import rmg.droid.rmgcore.extensions.openUrl
import ru.russianmediagroup.rusrad.R
import ru.russianmediagroup.rusrad.app.RusRadioApp
import ru.russianmediagroup.rusrad.network.NetworkServiceImpl
import ru.russianmediagroup.rusrad.network.models.Menu
import ru.russianmediagroup.rusrad.network.models.MenuType
import ru.russianmediagroup.rusrad.ui.activity.pushRoot
import ru.russianmediagroup.rusrad.ui.adapter.MoreMenuAdapter
import ru.russianmediagroup.rusrad.ui.entity.Stateable

/**
 * @author by Arthur Korchagin on 16.04.18
 */
class MoreFragment : BaseRusFragment(), Stateable<List<Menu>> {

    companion object {
        fun newInstance() = MoreFragment()
    }

    private var moreMenuAdapter: MoreMenuAdapter? = null
    private var mMenuList: List<Menu> = emptyList()

    private val app: CoreRMGApp?
        get() = (activity?.application as? CoreRMGApp)

    override val progressView: View
        get() = vProgress

    override val dataView: View
        get() = fmtMoreListMenu

    override val errorView: View
        get() = lError

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fmt_more, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        app?.analyticsService?.logScreen(getString(R.string.label_more))
        fmtMoreListMenu.layoutManager = LinearLayoutManager(context)
        fmtMoreRoot.setOnClickListener { back() }
        btnRetry.setOnClickListener { load() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        load()
    }

    private fun load() {
        if (moreMenuAdapter == null) {
            if (mMenuList.isNotEmpty()) {
                onSuccess(mMenuList)
            } else {
                onProgress()
                RusRadioApp.inst.networkService.loadMenu()
                        .subscribe(this::onSuccess, this::onError)
                        .unsubscribeOnDestroy()
            }
        }
    }

    override fun setData(data: List<Menu>) {
        mMenuList = data
        moreMenuAdapter = MoreMenuAdapter(data, ::openMenu)
        fmtMoreListMenu.adapter = moreMenuAdapter
        moreMenuAdapter?.notifyDataSetChanged()

        if (mMenuList.isEmpty()) {
            back()
        }
    }

    private fun openMenu(menu: Menu) = when (menu.type) {
        MenuType.poll -> openPoll(menu.poll_id, menu.icon.url)
        MenuType.link -> openLink(menu.link)
    }

    private fun openLink(link: String) {
        activity?.openUrl(link)
    }

    private fun openPoll(pollId: String, logoUrl: String) {
        activity?.pushRoot(PollFragment.newInstance(pollId, logoUrl))
    }

}
