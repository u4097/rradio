package ru.russianmediagroup.rusrad.di.components

import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import dagger.Component
import ru.russianmediagroup.rusrad.di.modules.ActivityModule
import ru.kodep.quickeaters.di.PerActivity
import ru.russianmediagroup.rusrad.network.NetworkService
import ru.russianmediagroup.rusrad.ui.activity.MusicActivity
import ru.russianmediagroup.rusrad.ui.screens.splash.SplashActivity

/**
 * @author by Arthur Korchagin on 03.12.17.
 */
@PerActivity
@Component(modules = [ActivityModule::class],
        dependencies = [AppComponent::class])
interface ActivityComponent {

    fun inject(activity: MusicActivity)
    fun inject(activity: SplashActivity)

    //  Exposed to sub-graphs.
    fun musicActivity(): AppCompatActivity

    fun context(): Context

    fun networkService(): NetworkService
    fun rxPermissions(): RxPermissions

}
