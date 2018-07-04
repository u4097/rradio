package ru.russianmediagroup.rusrad.ui.fragment

import android.os.Bundle
import android.view.View
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import rmg.droid.rmgcore.ui.fragment.BaseFragment
import ru.russianmediagroup.rusrad.R
import ru.russianmediagroup.rusrad.ui.activity.MusicActivity
import ru.russianmediagroup.rusrad.ui.activity.back

/**
 * @author by Arthur Korchagin on 24.12.17.
 */
open class BaseRusFragment : BaseFragment() {

    private val compositeDisposable = CompositeDisposable()


    protected val ac: MusicActivity?
        get() = (activity as? MusicActivity)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateToolbar()
    }

    //    btnBack
    open fun updateToolbar() {
        ac?.musicToolbar?.imageViewLogo?.setImageResource(R.drawable.logo_group_toolbar)
        ac?.musicToolbar?.showMenuButton()
        ac?.buttonBack?.visibility = View.GONE
    }

    fun Disposable.unsubscribeOnDestroy() =
            compositeDisposable.add(this)

    override fun onDestroyView() {
        compositeDisposable.clear()
        super.onDestroyView()
    }

    fun back() = activity?.back(javaClass.name)

}