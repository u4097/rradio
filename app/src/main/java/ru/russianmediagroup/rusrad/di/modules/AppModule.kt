package ru.russianmediagroup.rusrad.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.russianmediagroup.rusrad.app.RusRadioApp
import javax.inject.Singleton

/**
 * @author by Arthur Korchagin on 03.12.17.
 */
@Module
class AppModule(private val app: RusRadioApp) {

    @Provides
    @Singleton
    fun provideApp() = app

    @Provides
    @Singleton
    fun provideContext() : Context = app

    @Provides
    @Singleton
    fun provideAssets() = app.assets

}