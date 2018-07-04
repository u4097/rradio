package ru.russianmediagroup.rusrad.network.models

import java.util.*

/**
 * @author by Arthur Korchagin on 30.01.18.
 */
data class Video(var created_at: Date?,
                 var name: String?,
                 var description: String?,
                 var id: String?,
                 var video_id: String?,
                 var published_at: Date?,
                 var url: String?,
                 var order: Int?
)