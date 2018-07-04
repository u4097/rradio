package ru.russianmediagroup.rusrad.network

import io.reactivex.Single
import retrofit2.http.*
import ru.russianmediagroup.rusrad.network.models.*

/**
 * @author by Arthur Korchagin on 24.12.17
 */
interface ApiService {

    companion object {
        const val POLL_ID = "poll_id"
        const val POLL_OPTION_ID = "poll_option_id"
        const val SECRET = "secret"
    }

    @GET("settings")
    fun settings(): Single<Settings>

    @GET("active_translation")
    fun activeTranslation(): Single<ActiveTranslation>

    @GET("actions_list")
    fun actionsList(@Query("per_page") count: Int = 100,
                    @Query("is_displayed") displayed: Int = 1,
                    @Query("order_by") order: String = "created_at"): Single<ListResponse<Action>>

    @GET("video_list")
    fun videoList(@Query("per_page") count: Int = 100,
                  @Query("order_by") order: String = "published_at",
                  @Query("is_displayed") isDisplayed: Boolean = true)
            : Single<ListResponse<Video>>

    @GET("app_menu")
    fun appMenu(): Single<List<Menu>>

    @GET("poll/{$POLL_ID}")
    fun loadPoll(@Path(POLL_ID) pollId: String,
                 @Header(SECRET) secret: String): Single<Poll>

    @GET("poll_items/{$POLL_OPTION_ID}")
    fun loadPollOption(@Path(POLL_OPTION_ID) pollId: String,
                       @Header(SECRET) secret: String): Single<PollOption>

    @POST("poll_vote/{$POLL_OPTION_ID}")
    fun sendPollVote(@Path(POLL_OPTION_ID) pollId: String,
                     @Header(SECRET) secret: String): Single<String>

    @DELETE("poll_vote/{$POLL_OPTION_ID}")
    fun deletePollVote(@Path(POLL_OPTION_ID) pollId: String,
                       @Header(SECRET) secret: String): Single<String>

    @GET("channel_list")
    fun channelList(@Query("per_page") count: Int = 100,
                    @Query("page") page: Int = 1,
                    @Query("order_by") order: String = "order"): Single<ListResponse<ApiChannel>>
}