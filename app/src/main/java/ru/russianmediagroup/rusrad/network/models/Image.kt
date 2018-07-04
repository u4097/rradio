package ru.russianmediagroup.rusrad.network.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * @author by Arthur Korchagin on 15.04.18.
 */
@Parcelize
data class Image(
        val id: String,
        val ext: String,
        val filename: String,
        val url: String
) : Parcelable