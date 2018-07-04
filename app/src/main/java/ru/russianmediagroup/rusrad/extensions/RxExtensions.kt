package ru.russianmediagroup.rusrad.extensions

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * @author by Arthur Korchagin on 03.12.17.
 */
fun <T> Single<T>.applySchedulers(): Single<T> = compose {
    it.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
}

fun <T> Observable<T>.applySchedulers(): Observable<T> = compose {
    it.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
}

fun <T> Maybe<T>.applySchedulers(): Maybe<T> = compose {
    it.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
}

inline fun <T> justSingle(crossinline block: () -> T): Single<T> = Single.create<T> {
    try {
        it.onSuccess(block())
    } catch (error: Throwable) {
        it.onError(error)
    }
}

inline fun <T> justMaybe(crossinline block: () -> T?): Maybe<T> = Maybe.create<T> {
    try {
        block()?.apply { it.onSuccess(this) }
        it.onComplete()
    } catch (error: Throwable) {
        it.onError(error)
    }
}