package ru.russianmediagroup.rusrad.ui.screens.splash

import android.Manifest
import com.arellomobile.mvp.InjectViewState
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Single
import rmg.droid.rmgcore.analytics.AnalyticsService
import ru.kodep.quickeaters.di.PerActivity
import ru.russianmediagroup.rusrad.app.RusRadioApp
import ru.russianmediagroup.rusrad.extensions.applySchedulers
import ru.russianmediagroup.rusrad.network.NetworkService
import ru.russianmediagroup.rusrad.services.ConfigService
import ru.russianmediagroup.rusrad.ui.base.BasePresenter
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * @author by Arthur Korchagin on 29.05.18.
 */

@PerActivity
@InjectViewState
class SplashPresenter
@Inject
constructor(private val networkService: NetworkService,
            private val rxPermissions: RxPermissions,
            private val analyticsService: AnalyticsService,
            private val configService: ConfigService) : BasePresenter<SplashView>() {

    companion object {
        const val SPLASH_DELAY = 1000L
    }

    fun init(url: String? = null) {

        viewState.setTitleAndSplash(configService.splashFile(), configService.splashTitle())

        if (url != null) {
            analyticsService.logPushOpen(url)
            viewState.openUrl(url)
        } else {

            viewState.showProgress()
            rxPermissions.request(Manifest.permission.READ_PHONE_STATE)
                    .single(false)
                    .flatMap { networkService.loadSettings().onErrorReturnItem(false) }
                    .flatMap {
                        if (RusRadioApp.inst.isMediaInitialized) {
                            Single.just(Unit).delay(SPLASH_DELAY, TimeUnit.MILLISECONDS)
                        } else {
                            networkService.channelList().map(RusRadioApp.inst::updateChannels)
                        }
                    }
                    .applySchedulers()
                    .subscribe(this::onChannelsReady, this::onChannelsLoadError)
        }
    }

    private fun onChannelsLoadError(error: Throwable) {
        error.printStackTrace()
        viewState.showError()
    }

    private fun onChannelsReady(none: Unit) {
        viewState.openMusicActivity()
    }

    override fun detachView(view: SplashView?) {
        super.detachView(view)
        configService.updateSplash()
    }
}