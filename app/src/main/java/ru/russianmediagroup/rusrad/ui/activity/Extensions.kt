package ru.russianmediagroup.rusrad.ui.activity

import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.text.TextUtils
import android.view.ViewGroup
import ru.russianmediagroup.rusrad.R

/**
 *  @author Arthur Korchagin on 16.06.17.
 */

fun FragmentActivity.back(fragment: String?) {
    supportFragmentManager.popBackStackImmediate(fragment, FragmentManager.POP_BACK_STACK_INCLUSIVE)
}

fun FragmentActivity.pushAddictive(fragment: Fragment) {
    val name = fragment.javaClass.name
    val topFragment = supportFragmentManager.findFragmentById(R.id.container)
    if (!TextUtils.equals(topFragment.javaClass.name, name)) {
        supportFragmentManager.beginTransaction()
                .add(R.id.container, fragment, name)
                .addToBackStack(name)
                .commitAllowingStateLoss()
    }
}

fun FragmentActivity.pushRoot(fragment: Fragment) {
    findViewById<ViewGroup>(R.id.container).removeAllViews()
    try {
        supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        val topFragment = supportFragmentManager.findFragmentById(R.id.container)

        if (topFragment != null) {
            supportFragmentManager.popBackStackImmediate(topFragment.javaClass.name, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        for (i in 0 until supportFragmentManager.backStackEntryCount) {
            supportFragmentManager.popBackStack()
        }

    } catch (ex: IllegalStateException) {
    }

    supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment, fragment.javaClass.name)
            .commitAllowingStateLoss()
}

fun FragmentActivity.push(fragment: Fragment) {
    val name = fragment.javaClass.name
    val topFragment = supportFragmentManager.findFragmentById(R.id.container)
    if (!TextUtils.equals(topFragment.javaClass.name, name)) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment, name)
                .addToBackStack(name)
                .commitAllowingStateLoss()
    }
}


fun FragmentActivity.showFragment(fmt: DialogFragment) =
        fmt.show(supportFragmentManager, fmt.javaClass.name)
