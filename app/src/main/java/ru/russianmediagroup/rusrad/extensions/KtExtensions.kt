package ru.russianmediagroup.rusrad.extensions

import retrofit2.HttpException
import java.io.InputStream
import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @author by Arthur Korchagin on 28.04.18.
 */

public inline fun <T> onChangeDo(initialValue: T, crossinline onChange: () -> Unit):
        ReadWriteProperty<Any?, T> = object : ObservableProperty<T>(initialValue) {
    override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) = onChange()
}

fun Throwable.apiError() =
        (this as? HttpException)
                ?.response()
                ?.errorBody()
                ?.byteStream()
                ?.readString()
                ?.let { it.substring(1, it.length - 2) }

private fun InputStream.readString(): String {

    use {
        var result = ""
        var prev = 0

        while (available() > 0) {
            var read = read()

            if (prev == 92 && read == 117) {

                read = ByteArray(4)
                        .also { read(it, 0, 4) }
                        .toString(Charsets.UTF_8)
                        .let { Integer.parseInt(it, 16) }

            } else if (prev != 0) {
                result += prev.toChar()
            }

            prev = read
        }

        if (prev != 0) {
            result += prev.toChar()
        }

        return result
    }
}
