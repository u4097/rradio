package ru.russianmediagroup.rusrad.ui.entity

import ru.russianmediagroup.rusrad.network.models.ActiveTranslation
import ru.russianmediagroup.rusrad.network.models.Video

/**
 * @author by Arthur Korchagin on 30.01.18.
 */
data class VideoEntity(
        val videoList: List<Video>,
        val translation: ActiveTranslation
)