package ru.russianmediagroup.rusrad.services

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import rmg.droid.rmgcore.preferences.prefs
import ru.russianmediagroup.rusrad.network.models.Settings

/**
 * @author by Arthur Korchagin on 16.05.18.
 */
class AdsService(val context: Context) : AdListener() {

    companion object {
        const val INTERSTITIAL_AD_ID = "ca-app-pub-1474898975056196/8918010863"
        private const val MOBILE_AD_ID = "ca-app-pub-1474898975056196~1407180861"

        fun initialize(context: Context) {
            MobileAds.initialize(context, MOBILE_AD_ID)
        }
    }

    private var isNeedToShow = false

    private val mInterstitialAd = InterstitialAd(context).apply {
        adUnitId = INTERSTITIAL_AD_ID
        adListener = this@AdsService
    }

    private val isShowAd
        get() = context.prefs.getBoolean(Settings.SHOW_ADS_EVERYWHERE, false)

    init {
        loadNewAd()
    }

    private fun loadNewAd() {
        if (isShowAd) {
            mInterstitialAd.loadAd(AdRequest
                    .Builder()
                    .build())
        }
    }

    fun showAd() {
        if (isShowAd && mInterstitialAd.isLoaded) {
            mInterstitialAd.show()
        } else {
            Log.d(javaClass.name, "-> The interstitial wasn't loaded yet.")
        }
    }

    override fun onAdFailedToLoad(error: Int) {
        super.onAdFailedToLoad(error)
        val adsError = AdsError.values()[error]
        Log.d(javaClass.name, "-> onAdFailedToLoad ->${adsError.name}: ${adsError.message}")
    }

    override fun onAdLoaded() {
        if (isNeedToShow) {
            showAd()
        }
    }

    override fun onAdClosed() {
        super.onAdClosed()
        loadNewAd()
    }

    enum class AdsError(val message: String) {
        ERROR_CODE_INTERNAL_ERROR("Something happened internally; for instance, an invalid response was received from the ad server"),
        ERROR_CODE_INVALID_REQUEST("The ad request was invalid; for instance, the ad unit ID was incorrect."),
        ERROR_CODE_NETWORK_ERROR("The ad request was unsuccessful due to network connectivity."),
        ERROR_CODE_NO_FILL("The ad request was successful, but no ad was returned due to lack of ad inventory.")
    }
}

