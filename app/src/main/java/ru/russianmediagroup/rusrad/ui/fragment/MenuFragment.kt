package ru.russianmediagroup.rusrad.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fmt_menu.*
import rmg.droid.rmgcore.app.CoreRMGApp
import rmg.droid.rmgcore.extensions.openEmail
import rmg.droid.rmgcore.extensions.openUrl
import rmg.droid.rmgcore.media.MediaPlayerService
import rmg.droid.rmgcore.ui.fragment.BaseFragment
import ru.russianmediagroup.rusrad.BuildConfig
import ru.russianmediagroup.rusrad.R
import ru.russianmediagroup.rusrad.ui.activity.MusicActivity

/**
 * Created by Arthur Korchagin on 10.06.17
 */
class MenuFragment : BaseFragment() {

    companion object {
        fun newInstance() = MenuFragment()
    }

    private val app: CoreRMGApp?
        get() = (activity?.application as? CoreRMGApp)

    var mDrawerLayout: DrawerLayout? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val fragmentView = inflater.inflate(R.layout.fmt_menu, container, false)
        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvRadioStation.setOnClickListener {
            (activity as? MusicActivity)?.openRadio()
            mDrawerLayout?.closeDrawer(GravityCompat.START)
        }

        tvMenuAlarm.setOnClickListener {
            (activity as? MusicActivity)?.openAlarm()
            mDrawerLayout?.closeDrawer(GravityCompat.START)
        }

        tvMenuContest.setOnClickListener {
            (activity as? MusicActivity)?.openContest()
            mDrawerLayout?.closeDrawer(GravityCompat.START)
        }

        tvMenuWatch.setOnClickListener {
            (activity as? MusicActivity)?.openWatch()
            mDrawerLayout?.closeDrawer(GravityCompat.START)
        }

        tvClose.setOnClickListener {
            if (app?.serviceBound == true) {
                val playerIntent = Intent(app, MediaPlayerService::class.java)
                playerIntent.action = MediaPlayerService.ACTION_CLOSE
                app?.startService(playerIntent)
            }
            activity?.finish()
        }

        tvRateApp.setOnClickListener {
            activity?.openUrl(getString(R.string.play_address_f, BuildConfig.APPLICATION_ID))
            mDrawerLayout?.closeDrawer(GravityCompat.START)
        }

        tvFeedback.setOnClickListener {
            activity?.openEmail(getString(R.string.email_support), getString(R.string.label_feedback))
            mDrawerLayout?.closeDrawer(GravityCompat.START)
        }


        tvPrivacy.setOnClickListener {
            activity?.openUrl(getString(R.string.link_privacy))
            mDrawerLayout?.closeDrawer(GravityCompat.START)
        }
    }

}