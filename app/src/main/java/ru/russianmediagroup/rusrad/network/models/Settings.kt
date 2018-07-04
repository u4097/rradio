package ru.russianmediagroup.rusrad.network.models

/**
 * Created by lliepmah on 01.10.17
 */
data class Settings(val show_metatags: Boolean, val show_ads_everywhere : Boolean) {

    companion object {
       val SHOW_METATAGS = "show_metatags"
       val SHOW_ADS_EVERYWHERE = "show_ads_everywhere"
    }
}