package ru.russianmediagroup.rusrad.extensions

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicReference

/**
 * @author by Arthur Korchagin on 16.04.18.
 */

private const val PATTERN_DISPLAY_DATE = "dd.MM.yy"
private val DISPLAY_DATE_FORMAT = AtomicReference(SimpleDateFormat(PATTERN_DISPLAY_DATE, Locale.getDefault()))

val Date.format: String
    get() = DISPLAY_DATE_FORMAT.get().format(this)