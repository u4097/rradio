package ru.russianmediagroup.rusrad.di.components

import android.content.Context
import dagger.Component
import rmg.droid.rmgcore.analytics.AnalyticsService
import ru.russianmediagroup.rusrad.app.RusRadioApp
import ru.russianmediagroup.rusrad.di.modules.AppModule
import ru.russianmediagroup.rusrad.di.modules.NetworkModule
import ru.russianmediagroup.rusrad.network.NetworkService
import ru.russianmediagroup.rusrad.services.ConfigService
import javax.inject.Singleton

/**
 * @author by Arthur Korchagin on 03.12.17.
 */
@Singleton
@Component(modules = [AppModule::class, NetworkModule::class])
interface AppComponent {

    fun app(): RusRadioApp
    fun networkService(): NetworkService
    fun configService(): ConfigService
    fun analyticsService(): AnalyticsService
    fun context(): Context

}