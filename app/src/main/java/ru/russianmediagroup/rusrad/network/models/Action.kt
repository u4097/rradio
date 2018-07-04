package ru.russianmediagroup.rusrad.network.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Created by lliepmah on 25.09.17
 */
@Parcelize
data class Action(
        val body: String = "",
        val id: String = "",
        val region_id: String = "",
        val name: String = "",
        val picture_id: String = "",
        val picture_path: String = "",
        val subtitle: String = "",
        val url: String = "",
        val url_slug: String = "",

        val autopublish: Boolean = false,
        val is_displayed: Boolean = true,
        val created_at: Date = Date(),
        val publicated_at: Date = Date()
        ) : Parcelable