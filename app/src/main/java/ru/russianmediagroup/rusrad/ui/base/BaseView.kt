package ru.russianmediagroup.rusrad.ui.base

import android.support.v4.app.Fragment
import com.arellomobile.mvp.MvpView

interface BaseView : MvpView {
    fun hideProgress()
    fun showProgress()
    fun openFragment(fmt: Fragment)
}