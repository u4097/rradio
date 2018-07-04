package ru.russianmediagroup.rusrad.network.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * @author by Arthur Korchagin on 18.04.18.
 */
@Parcelize
data class Parameter(val key: String, val value: String) : Parcelable