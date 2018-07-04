package ru.russianmediagroup.rusrad.network

import android.app.Application
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.gson.Gson
import io.reactivex.Single
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rmg.droid.rmgcore.preferences.putPrefs
import ru.russianmediagroup.rusrad.app.RusRadioApp
import ru.russianmediagroup.rusrad.extensions.applySchedulers
import ru.russianmediagroup.rusrad.network.models.Action
import ru.russianmediagroup.rusrad.network.models.ApiChannel
import ru.russianmediagroup.rusrad.network.models.Poll
import ru.russianmediagroup.rusrad.network.models.Settings
import ru.russianmediagroup.rusrad.preferences.deviceId
import ru.russianmediagroup.rusrad.services.AdsService


/**
 * Created by lliepmah on 25.09.17
 */
class NetworkServiceImpl(okHttpClient: OkHttpClient, private val gson: Gson, private val app: Application) : NetworkService {

    companion object {
        private const val SCHEME = "https"
        private const val HOST = "rusradio.ru"
        private const val BASE_URL = "$SCHEME://$HOST/api/"
    }

    private var apiService = buildRetrofit(okHttpClient, BASE_URL)
            .create(ApiService::class.java)

    var actions: List<Action>? = null

    private fun buildRetrofit(httpClient: OkHttpClient, baseUrl: String): Retrofit {
        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(createConverterFactory())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build()
    }

    private fun createConverterFactory(): Converter.Factory = GsonConverterFactory.create(gson)

    override fun loadVideoUrl() =
            apiService.activeTranslation()
                    .applySchedulers()

    override fun loadActions() =
            apiService.actionsList()
                    .map { it.items }
                    .applySchedulers()

    override fun loadVideos() =
            apiService.videoList()
                    .map { it.items.sortedBy { it.order } }
                    .applySchedulers()

    override fun loadSettings() =
            apiService.settings()
                    .map {
                        app.putPrefs(Settings.SHOW_METATAGS, it.show_metatags)
                        app.putPrefs(Settings.SHOW_ADS_EVERYWHERE, it.show_ads_everywhere)

                        if(it.show_ads_everywhere){
                            AdsService.initialize(app)
                        }

                        true
                    }
                    .applySchedulers()

    override fun loadMenu() =
            apiService.appMenu()
                    .applySchedulers()

    override fun loadPoll(pollId: String): Single<Poll> =
            restoreDeviceId()
                    .flatMap { apiService.loadPoll(pollId, it) }
                    .applySchedulers()

    override fun channelList(): Single<List<ApiChannel>> =
            apiService.channelList()
                    .map { it.items.sortedBy { it.order ?: 0 } }
                    .applySchedulers()

    override fun loadPollOption(pollOptionId: String) =
            restoreDeviceId()
                    .flatMap { apiService.loadPollOption(pollOptionId, it) }
                    .applySchedulers()

    override fun toggleLike(pollOptionId: String) =
            restoreDeviceId()
                    .flatMap { apiService.sendPollVote(pollOptionId, it) }
                    .flatMap { loadPollOption(pollOptionId) }
                    .applySchedulers()

    override fun restoreDeviceId() = Single.fromCallable {
        RusRadioApp.inst.run {
            deviceId ?: AdvertisingIdClient.getAdvertisingIdInfo(this).id.also { deviceId = it }
        }
    }

}
