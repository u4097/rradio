package ru.russianmediagroup.rusrad.preferences

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.text.TextUtils
import ru.russianmediagroup.rusrad.BuildConfig

const val PREFS_FILENAME = "${BuildConfig.APPLICATION_ID}.prefs"

const val ALARM_ENABLED = "alarm_enabled"
const val ALARM_CHANNEL = "alarm_channel"
const val ALARM_DAYS = "alarm_days"
const val ALARM_HOURS = "alarm_hours"
const val ALARM_MINUTES = "alarm_minutes"
const val DEVICE_ID = "device_id"

val Context.prefs: SharedPreferences
    get() = this.getSharedPreferences(PREFS_FILENAME, MODE_PRIVATE)

var Context.alarmEnabled: Boolean
    get() = prefs.getBoolean(ALARM_ENABLED, false)
    set(value) = prefs.edit().putBoolean(ALARM_ENABLED, value).apply()

var Context.alarmChannel: Int
    get() = prefs.getInt(ALARM_CHANNEL, 0)
    set(value) = prefs.edit().putInt(ALARM_CHANNEL, value).apply()

var Context.alarmDays: List<Int>
    get() = TextUtils.split(prefs.getString(ALARM_DAYS, ""), ",").map { it.toInt() }
    set(value) = prefs.edit().putString(ALARM_DAYS, TextUtils.join(",", value)).apply()

var Context.alarmHours: Int
    get() = prefs.getInt(ALARM_HOURS, 6)
    set(value) = prefs.edit().putInt(ALARM_HOURS, value).apply()

var Context.alarmMinutes: Int
    get() = try {
        prefs.getInt(ALARM_MINUTES, 0)
    } catch (e: ClassCastException) {
        0
    }
    set(value) = prefs.edit().putInt(ALARM_MINUTES, value).apply()

var Context.deviceId: String?
    get() = prefs.getString(DEVICE_ID, null)
    set(value) = prefs.edit().putString(DEVICE_ID, value).apply()


