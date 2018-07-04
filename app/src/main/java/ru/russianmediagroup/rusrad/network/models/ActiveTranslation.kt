package ru.russianmediagroup.rusrad.network.models

/**
 * @author by Arthur Korchagin on 12.12.17.
 */
data class ActiveTranslation(
        var primary_stream_url: String? = null,
        var show_time: String? = null,
        var name: String? = null,
        var subtitle: String? = null
)