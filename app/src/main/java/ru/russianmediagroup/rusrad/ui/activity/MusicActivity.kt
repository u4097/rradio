package ru.russianmediagroup.rusrad.ui.activity

import android.content.Context
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.ac_music.*
import rmg.droid.rmgcore.ui.custom.Toolbar
import ru.russianmediagroup.rusrad.R
import ru.russianmediagroup.rusrad.app.RusRadioApp
import ru.russianmediagroup.rusrad.network.models.ActiveTranslation
import ru.russianmediagroup.rusrad.services.AdsService
import ru.russianmediagroup.rusrad.ui.fragment.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper


class MusicActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SCREEN = "screen"
        const val EXTRA_POLL_ID = "poll_id"

        const val SCREEN_WATCH = "watch"
        const val SCREEN_CONTEST = "contest"
        const val SCREEN_POLL = "poll"
    }

    private lateinit var mFmtMenu: MenuFragment

    val musicToolbar: Toolbar
        get() = toolbar
    val buttonBack: ImageView
        get() = btnBack

    lateinit var adsService: AdsService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ac_music)
        adsService = AdsService(this)

        if (resources.getBoolean(R.bool.drawer_enabled)) {
            initLeftMenu()
            toolbar.setOpenMenuListener(this::toggleLeftMenu)
        } else {
            drawerLayout.isEnabled = false
            drawerLayout.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED)
            toolbar.hideMenuButton()
        }

        val fmtToPush =
                when (intent?.getStringExtra(EXTRA_SCREEN)) {
                    SCREEN_WATCH -> WatchFragment.newInstance()
                    SCREEN_CONTEST -> ContestFragment.newInstance()
                    SCREEN_POLL -> intent?.getStringExtra(EXTRA_POLL_ID)?.let { PollFragment.newInstance(pollId = it) }
                            ?: RadioFragment.newInstance()
                    else -> RadioFragment.newInstance()
                }

        pushIfEmpty(fmtToPush)

        btnRadio.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_radio, 0, 0)
        btnAlarm.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_alarm, 0, 0)
        btnContest.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_contest, 0, 0)
        btnWatch.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_watch, 0, 0)
        btnMore.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_more, 0, 0)

        btnRadio.setOnClickListener { openRadio() }
        btnAlarm.setOnClickListener { openAlarm() }
        btnContest.setOnClickListener { openContest() }
        btnWatch.setOnClickListener { openWatch() }
        btnMore.setOnClickListener { openMore() }

        translationView.setOnClickListener { openWatch() }

        ivCloseTranslation.setOnClickListener { setTranslation(false) }
        ivTranslation.setOnClickListener { openWatch() }
        tvTranslation.setOnClickListener { openWatch() }
        btnBack.setOnClickListener { back(null) }

    }

    fun openAlarm() = pushRoot(AlarmFragment.newInstance())

    fun openRadio() = pushRoot(RadioFragment.newInstance())

    fun openContest() = pushRoot(ContestFragment.newInstance())

    fun openWatch() = pushRoot(WatchFragment.newInstance())

    fun openMore() = pushAddictive(MoreFragment.newInstance())

    fun selectButton(@IdRes buttonId: Int) =
            arrayOf(btnRadio, btnAlarm, btnContest, btnWatch, btnMore)
                    .forEach { it.run { isSelected = id == buttonId } }

    private fun initLeftMenu() {
        mFmtMenu = MenuFragment.newInstance()
        mFmtMenu.mDrawerLayout = drawerLayout
        attachLeftMenu(mFmtMenu)
    }

    fun attachLeftMenu(menu: Fragment) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.leftMenu, menu)
                .commit()
    }

    fun toggleLeftMenu() {
        drawerLayout.apply {
            if (isDrawerOpen(GravityCompat.START)) {
                closeDrawer(GravityCompat.START)
            } else {
                openDrawer(GravityCompat.START)
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    /* For Base Activity */
    fun pushIfEmpty(fragment: Fragment) {
        val fragmentManager = supportFragmentManager

        val topFragment = fragmentManager.findFragmentById(R.id.container)

        if (topFragment == null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment, fragment.javaClass.name)
                    .commitAllowingStateLoss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
        // TODO Update Splash !!! RusRadioApp.co.updateSplash(application)
    }

    override fun onResume() {
        super.onResume()
        setTranslation(false)
        load()
    }

    private fun load() {
        RusRadioApp.inst.networkService.loadVideoUrl()
                .subscribe(this::onResult, this::onError)
                .unsubscribeOnDestroy()
    }

    private fun onError(error: Throwable) {
        error.printStackTrace()
        setTranslation(false)
    }

    private fun onResult(translation: ActiveTranslation) {
        setTranslation(translation.primary_stream_url != null && translation.show_time?.isBlank() == false)
    }

    private fun setTranslation(hasTranslation: Boolean) {
        translationView.visibility = if (hasTranslation) View.VISIBLE else View.GONE
    }

    private val compositeDisposable = CompositeDisposable()

    private fun Disposable.unsubscribeOnDestroy() =
            compositeDisposable.add(this)

}
