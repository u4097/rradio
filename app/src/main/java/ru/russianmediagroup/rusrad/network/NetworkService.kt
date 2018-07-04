package ru.russianmediagroup.rusrad.network

import io.reactivex.Single
import ru.russianmediagroup.rusrad.network.models.*

/**
 * @author by Arthur Korchagin on 29.05.18.
 */
interface NetworkService {
    fun loadVideoUrl(): Single<ActiveTranslation>
    fun loadActions(): Single<List<Action>>
    fun loadVideos(): Single<List<Video>>
    fun loadSettings(): Single<Boolean>
    fun loadMenu(): Single<List<Menu>>
    fun loadPoll(pollId: String): Single<Poll>
    fun channelList(): Single<List<ApiChannel>>
    fun loadPollOption(pollOptionId: String): Single<PollOption>
    fun toggleLike(pollOptionId: String): Single<PollOption>
    fun restoreDeviceId(): Single<String>?
}