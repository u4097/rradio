package ru.russianmediagroup.rusrad.network.models

/**
 * Created by lliepmah on 25.09.17
 */
data class ListResponse<out T>(val items: List<T> = emptyList())