package ru.russianmediagroup.rusrad.network

/**
 * Created by lliepmah on 25.09.17
 */
interface OnResultListener<T> {
    fun onResult(result: T)
    fun onError(error: Throwable)
}