package ru.russianmediagroup.rusrad.ui.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fmt_alarm.*
import ru.russianmediagroup.rusrad.R
import ru.russianmediagroup.rusrad.app.RusRadioApp
import ru.russianmediagroup.rusrad.ui.activity.MusicActivity
import ru.russianmediagroup.rusrad.ui.adapter.ChannelAdapter
import ru.russianmediagroup.rusrad.ui.entity.AlarmChannel
import rmg.droid.rmgcore.app.CoreRMGApp
import rmg.droid.rmgcore.ui.fragment.BaseFragment
import ru.russianmediagroup.rusrad.preferences.*
import java.util.*


/**
 *  @author Arthur Korchagin on 16.06.17.
 */
class AlarmFragment : BaseRusFragment(), ChannelAdapter.OnChannelChooseListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val fragmentView = inflater.inflate(R.layout.fmt_alarm, container, false)
        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = activity!!.application as? CoreRMGApp
        app?.analyticsService?.logScreen(getString(R.string.label_alarm))

        (activity as? MusicActivity)?.selectButton(R.id.btnAlarm)

        pickerHours.setLoopListener(this::onHourChosen)
        pickerHours.setDataList((0..23).map { getString(R.string.format_digits_2, it) })
        pickerHours.setInitPosition(context!!.alarmHours)

        pickerMinutes.setLoopListener(this::onMinuteChosen)
        pickerMinutes.setDataList((0..59).map { getString(R.string.format_digits_2, it) })
        pickerMinutes.setInitPosition(context!!.alarmMinutes)

        val channels = (activity!!.application as CoreRMGApp).mediaRepository.channels
        val alarmChannels = (0 until channels.size).map { AlarmChannel(it, channels[it].channelName, channels[it].coverRes) }
        alarmChannels[context!!.alarmChannel].selected = true

        rvMusic.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvMusic.adapter = ChannelAdapter(alarmChannels, this)

        swAlarm.isChecked = context!!.alarmEnabled

        val alarmDays = context!!.alarmDays
        vMonday.isSelected = alarmDays.contains(Calendar.MONDAY)
        vTuesday.isSelected = alarmDays.contains(Calendar.TUESDAY)
        vWednesday.isSelected = alarmDays.contains(Calendar.WEDNESDAY)
        vThursday.isSelected = alarmDays.contains(Calendar.THURSDAY)
        vFriday.isSelected = alarmDays.contains(Calendar.FRIDAY)
        vSaturday.isSelected = alarmDays.contains(Calendar.SATURDAY)
        vSunday.isSelected = alarmDays.contains(Calendar.SUNDAY)

        vMonday.onSelectListener = this::onDaySelected
        vTuesday.onSelectListener = this::onDaySelected
        vWednesday.onSelectListener = this::onDaySelected
        vThursday.onSelectListener = this::onDaySelected
        vFriday.onSelectListener = this::onDaySelected
        vSaturday.onSelectListener = this::onDaySelected
        vSunday.onSelectListener = this::onDaySelected

        swAlarm.setOnClickListener(this::onAlarmSwitch)
    }

    fun onDaySelected(weekDay: Int, selected: Boolean) {

        context?.alarmDays = ArrayList<Int>().apply {
            if (vMonday.isSelected) add(Calendar.MONDAY)
            if (vTuesday.isSelected) add(Calendar.TUESDAY)
            if (vWednesday.isSelected) add(Calendar.WEDNESDAY)
            if (vThursday.isSelected) add(Calendar.THURSDAY)
            if (vFriday.isSelected) add(Calendar.FRIDAY)
            if (vSaturday.isSelected) add(Calendar.SATURDAY)
            if (vSunday.isSelected) add(Calendar.SUNDAY)
        }

        Log.d(javaClass.name, "onDaySelected-> weekDay=$weekDay selected=$selected")
        updateAlarm()
    }

    fun onAlarmSwitch(view: View) {
        context?.alarmEnabled = swAlarm.isChecked
        Log.d(javaClass.name, "onAlarmSwitch-> swAlarm=${swAlarm.isChecked}")
        updateAlarm()
    }


    override fun onChannelChoose(channel: AlarmChannel) {
        Log.d(javaClass.name, "onChannelChoose-> channel=$channel")
        context?.alarmChannel = channel.index
        updateAlarm()
    }

    fun onHourChosen(hour: Int?) {
        context?.alarmHours = hour ?: 0
        updateAlarm()
    }

    fun onMinuteChosen(minute: Int?) {
        context?.alarmMinutes = minute ?: 0
        updateAlarm()
    }

    private fun updateAlarm() {
        (context?.applicationContext as? RusRadioApp)?.alarm?.updateAlarm()

//        val intent = Intent(context, MusicActivity::class.java)
//        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
//
//        //FLAG_CANCEL_CURRENT seems to be required to prevent a bug where the intent doesn't fire after app reinstall in KitKat
//        val amSender = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT)
//        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//
//        val cal = Calendar.getInstance()
//
////        cal.time = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24)
//
////        cal.set(Calendar.MONTH, 2017)
//
////        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
////
//        cal.set(Calendar.HOUR_OF_DAY, context.alarmHours)
//        cal.set(Calendar.MINUTE, context.alarmMinutes)
//        cal.set(Calendar.SECOND, 0)
//
//        val time = cal.timeInMillis
//
//        if (Build.VERSION.SDK_INT >= 23) {
//            /* Wakes up the device in Doze Mode */
//            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, amSender)
//        } else if (Build.VERSION.SDK_INT >= 19) {
//            /* Wakes up the device in Idle Mode */
//            am.setExact(AlarmManager.RTC_WAKEUP, time, amSender)
//        } else {
//            /* Old APIs */
//            am.set(AlarmManager.RTC_WAKEUP, time, amSender)
//        }
//
//        Log.d(javaClass.name, "updateAlarm-> context.alarmEnabled=${context.alarmEnabled}")
//        Log.d(javaClass.name, "updateAlarm-> Date Was Setted= ${cal.time}")

    }


    companion object {
        fun newInstance() = AlarmFragment()
    }

}