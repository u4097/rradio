package ru.russianmediagroup.rusrad.network.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * @author by Arthur Korchagin on 15.04.18.
 */
@Parcelize
data class Poll(
        val id: String,
        val logo: Image,
        val subtitle: String,
        val sharing_text: String,
        val bg: Image,
        val date: Date,
        val is_displayed: Boolean,
        val name: String,
        val poll_type: String?,
        val items: List<PollOption>
) : Parcelable