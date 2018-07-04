package ru.russianmediagroup.rusrad.network.models

/**
 * @author by Arthur Korchagin on 15.04.18
 */
data class Menu(

        val icon_id: String,
        val icon_path: String,
        val id: String,
        val link: String,
        val name: String,
        val order: Int,
        val poll_id: String,
        val type: MenuType,
        val icon: Image
)