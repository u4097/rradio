package ru.russianmediagroup.rusrad.ui.entity

import android.support.annotation.DrawableRes

/**
 *  @author Arthur Korchagin on 18.06.17.
 */
data class AlarmChannel(val index: Int,
                        val name: String,
                        @DrawableRes val image: Int,
                        var selected: Boolean = false)