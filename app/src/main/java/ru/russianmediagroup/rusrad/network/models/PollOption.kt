package ru.russianmediagroup.rusrad.network.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * @author by Arthur Korchagin on 15.04.18.
 */
@Parcelize
data class PollOption(

        val id: String? = "",
        var dynamic: Int = 0,
        val order: Int = 0,
        val poll_id: String? = "",
        val poll_item: PollItem? = PollItem(),
        val poll_item_id: String? = "",
        var position: Int = 0,
        var rating: Int = 0,
        val saved_position: Int = 0,
        var is_voted: Boolean = false

) : Parcelable
