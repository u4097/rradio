package ru.russianmediagroup.rusrad.di.modules

import android.support.v7.app.AppCompatActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import dagger.Module
import dagger.Provides
import ru.kodep.quickeaters.di.PerActivity

/**
 * @author by Arthur Korchagin on 03.12.17.
 */
@Module
class ActivityModule(private val appCompatActivity: AppCompatActivity) {

    @Provides
    @PerActivity
    internal fun provideMusicActivity() = appCompatActivity

    @Provides
    @PerActivity
    internal fun provideRxPermissions() = RxPermissions(appCompatActivity)

}