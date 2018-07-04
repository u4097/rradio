package ru.russianmediagroup.rusrad.ui.screens.splash

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import ru.russianmediagroup.rusrad.ui.base.BaseView
import java.io.File

/**
 * @author by Arthur Korchagin on 29.05.18.
 */
@StateStrategyType(AddToEndSingleStrategy::class)
interface SplashView : BaseView {

    fun setTitleAndSplash(splashFile: File, splashTitle: String)

    fun openUrl(url: String)

    fun openMusicActivity()

    fun showError()

}