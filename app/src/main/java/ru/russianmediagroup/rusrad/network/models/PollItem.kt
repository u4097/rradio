package ru.russianmediagroup.rusrad.network.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * @author by Arthur Korchagin on 15.04.18.
 */
@Parcelize
data class PollItem(
        val id: String? = "",
        val description: String? = "",
        val info: List<Parameter>? = emptyList(),
        val title: String? = "",
        val pictures: List<Image>? = emptyList()
) : Parcelable