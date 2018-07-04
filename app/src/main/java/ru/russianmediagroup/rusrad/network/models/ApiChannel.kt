package ru.russianmediagroup.rusrad.network.models

/**
 * @author by Arthur Korchagin on 20.02.18.
 */
data class ApiChannel(
        var api_url: String? = "",
        var body: String? = "",
        var description: String? = "",
        var dummy_id: String? = "",
        var dummy_path: String? = "",
        var id: String? = "",
        var is_live: Boolean? = false,
        var is_new: Boolean? = false,
        var logo_id: String? = "",
        var logo_path: String? = "",
        var name: String? = "",
        var order: Int? = 0,
        var stream_url: String? = "",
        var url_slug: String? = ""
)