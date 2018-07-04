package ru.russianmediagroup.rusrad.ui.entity

import android.view.View
import android.widget.Toast
import ru.russianmediagroup.rusrad.extensions.apiError

/**
 * @author by Arthur Korchagin on 16.04.18.
 */

interface Stateable<in T> {

    val progressView: View?
    val dataView: View
    val errorView: View

    fun setData(data: T)

    fun onProgress() = setViewVisibility(progress = true)

    fun onSuccess(data: T) {
        setViewVisibility(dataVisibility = true)
        setData(data)
    }

    fun onError(error: Throwable) {
        setViewVisibility(error = true)
        processError(error)
    }

    fun processError(error: Throwable) {
        error.apiError()?.let {
            Toast.makeText(dataView.context, it, Toast.LENGTH_LONG).show()
        }
    }

    fun setViewVisibility(progress: Boolean = false, dataVisibility: Boolean = false, error: Boolean = false) {
        progressView?.visibility = if (progress) View.VISIBLE else View.GONE
        dataView.visibility = if (dataVisibility) View.VISIBLE else View.GONE
        errorView.visibility = if (error) View.VISIBLE else View.GONE
    }

}