package ru.russianmediagroup.rusrad.ui.screens.splash

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.ac_splash.*
import rmg.droid.rmgcore.entity.Stub
import rmg.droid.rmgcore.utils.setBadge
import ru.russianmediagroup.rusrad.R
import ru.russianmediagroup.rusrad.app.RusRadioApp
import ru.russianmediagroup.rusrad.di.components.ActivityComponent
import ru.russianmediagroup.rusrad.di.components.DaggerActivityComponent
import ru.russianmediagroup.rusrad.di.modules.ActivityModule
import ru.russianmediagroup.rusrad.ui.activity.MusicActivity
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import java.io.File
import javax.inject.Inject


class SplashActivity : AppCompatActivity(), SplashView {

    val app: RusRadioApp
        get() = application as RusRadioApp

    @Inject
    lateinit var presenter: SplashPresenter

    private val activityComponent: ActivityComponent by lazy {
        DaggerActivityComponent.builder()
                .activityModule(ActivityModule(this))
                .appComponent(app.appComponent)
                .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ac_splash)

        activityComponent.inject(this)
        presenter.attachView(this)

        setBadge(0)

        presenter.init(intent?.extras?.run { getString("url") ?: getString("link") })
    }


    private fun Intent.copyExtra(intent: Intent, extraKey: String) =
            intent.getStringExtra(extraKey)?.let { putExtra(extraKey, it) }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView(this)
    }

    override fun setTitleAndSplash(splashFile: File, splashTitle: String) {
        tvSplashTitle.text = splashTitle

        Glide.with(this)
                .load("file://${splashFile.absolutePath}")
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(imgSplashBg)
    }

    override fun openMusicActivity() {

        Intent(this, MusicActivity::class.java)
                .run {
                    copyExtra(intent, MusicActivity.EXTRA_SCREEN)
                    copyExtra(intent, MusicActivity.EXTRA_POLL_ID)
                    startActivity(this)
                }

        finish()
    }

    override fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        finish()
    }

    override fun showError() {
        acSplashLError.visibility = View.VISIBLE
        acSplashBtnRepeat.setOnClickListener { presenter.init() }
    }

    override fun hideProgress() = Stub

    override fun showProgress() {
        acSplashLError.visibility = View.GONE
        acSplashBtnRepeat.setOnClickListener(null)
    }

    override fun openFragment(fmt: Fragment) = Stub
}
