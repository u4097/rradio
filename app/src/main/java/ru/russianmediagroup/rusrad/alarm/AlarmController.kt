package ru.russianmediagroup.rusrad.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import ru.russianmediagroup.rusrad.ui.activity.MusicActivity
import rmg.droid.rmgcore.app.CoreRMGApp
import rmg.droid.rmgcore.media.MediaPlayerService
import ru.russianmediagroup.rusrad.preferences.*
import java.text.SimpleDateFormat
import java.util.*

class AlarmController(private val mContext: Context) {

  private val mHandler = Handler(Looper.myLooper())

  companion object {
    private val DAY = 1000 * 60 * 60 * 24
  }

  fun updateAlarm() {
    mContext.apply {

      val days = alarmDays

      if (alarmEnabled && days.isNotEmpty()) {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, alarmHours)
        c.set(Calendar.MINUTE, alarmMinutes)
        c.set(Calendar.SECOND, 0)

        while (c.timeInMillis < System.currentTimeMillis() || !days.contains(
            c.get(Calendar.DAY_OF_WEEK))) {
          c.timeInMillis += DAY
        }

        restartAlarm(c)
      } else {
        cancelAlarm()
      }
    }

  }

  fun restartAlarm(c: Calendar) {
    var intent = Intent(mContext, AlarmReceiver::class.java)
    val sender = PendingIntent.getBroadcast(mContext, 0, intent, 0)

    val am = mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {        // KITKAT and later
        am.set(AlarmManager.RTC_WAKEUP, c.timeInMillis, sender)
      } else {
        am.setExact(AlarmManager.RTC_WAKEUP, c.timeInMillis, sender)
      }

      intent = Intent("android.intent.action.ALARM_CHANGED")
      intent.putExtra("alarmSet", true)

      mContext.sendBroadcast(intent)

      val fmt = SimpleDateFormat("E HH:mm")

      Settings.System.putString(mContext.contentResolver, Settings.System.NEXT_ALARM_FORMATTED,
          fmt.format(c.time))

    } else {

      val showIntent = Intent(mContext, MusicActivity::class.java)
      // TODO: 19.06.17 Put extras into showIntent

      val showOperation = PendingIntent.getActivity(mContext, 0, showIntent,
          PendingIntent.FLAG_UPDATE_CURRENT)

      val alarmClockInfo = AlarmManager.AlarmClockInfo(c.timeInMillis, showOperation)

      am.setAlarmClock(alarmClockInfo, sender)

    }
  }

  fun cancelAlarm() {
    var intent = Intent(mContext, AlarmReceiver::class.java)
    val sender = PendingIntent.getBroadcast(mContext, 0, intent, 0)
    val am = mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    am.cancel(sender)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      intent = Intent("android.intent.action.ALARM_CHANGED")
      intent.putExtra("alarmSet", false)
      mContext.sendBroadcast(intent)
      Settings.System.putString(mContext.contentResolver,
          Settings.System.NEXT_ALARM_FORMATTED, "")
    }
  }

  fun wakeupAlarm() {
    val pm = mContext.getSystemService(Context.POWER_SERVICE) as PowerManager
    val wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
        PowerManager.ACQUIRE_CAUSES_WAKEUP or
        PowerManager.ON_AFTER_RELEASE, "AlarmReceiver")
    wl.acquire(5000)

    (mContext as? CoreRMGApp)?.mediaRepository?.setChannel(mContext.alarmChannel)

    mHandler.postDelayed({ updateAlarm() }, 5000)
  }

  fun dismissAlarm() {
    mContext.stopService(Intent(mContext, MediaPlayerService::class.java))
  }
}
