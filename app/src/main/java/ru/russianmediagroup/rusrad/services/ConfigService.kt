package ru.russianmediagroup.rusrad.services

import java.io.File

/**
 * @author by Arthur Korchagin on 29.05.18.
 */
interface ConfigService {

    fun splashFile(default: String? = null): File

    fun splashTitle(): String

    fun updateSplash()

}