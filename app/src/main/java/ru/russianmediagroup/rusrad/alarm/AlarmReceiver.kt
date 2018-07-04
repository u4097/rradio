package ru.russianmediagroup.rusrad.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ru.russianmediagroup.rusrad.app.RusRadioApp

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        (context.applicationContext as? RusRadioApp)?.alarm?.wakeupAlarm()
    }

}
