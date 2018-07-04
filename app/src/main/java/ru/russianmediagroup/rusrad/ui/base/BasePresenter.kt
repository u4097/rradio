package ru.russianmediagroup.rusrad.ui.base

import com.arellomobile.mvp.MvpPresenter
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

open class BasePresenter<View : BaseView>
constructor(private val compositeDisposable: CompositeDisposable = CompositeDisposable())
    : MvpPresenter<View>() {

    fun Disposable.unsubscribeOnDestroy() =
            compositeDisposable.add(this)

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    open fun onError(error: Throwable) {
        viewState.hideProgress()
        error.printStackTrace()
    }

}