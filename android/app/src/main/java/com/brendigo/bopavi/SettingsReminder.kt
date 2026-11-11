package com.brendigo.bopavi

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import java.util.Calendar

/**
 * User-opted-in local daily reminder. No AdMob, push SDK, account or server.
 * Android decides the exact delivery time for inexact repeat alarms.
 */
internal object SettingsReminder {
    private const val ACTION_REMINDER="com.brendigo.bopavi.DAILY_REMINDER"
    private const val CHANNEL="bopavi_daily_reminders"
    private const val REQUEST=7032

    private fun alarmIntent(context:Context)=PendingIntent.getBroadcast(
        context,REQUEST,Intent(context,SettingsReminderReceiver::class.java)
            .setAction(ACTION_REMINDER),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    fun allowed(context:Context):Boolean =
        Build.VERSION.SDK_INT<33 ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==
                PackageManager.PERMISSION_GRANTED

    fun configure(context:Context,enabled:Boolean) {
        val manager=context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending=alarmIntent(context)
        manager.cancel(pending)
        if(!enabled || !allowed(context))return

        val now=Calendar.getInstance()
        val next=Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY,18)
            set(Calendar.MINUTE,0)
            set(Calendar.SECOND,0)
            set(Calendar.MILLISECOND,0)
            if(!after(now))add(Calendar.DAY_OF_YEAR,1)
        }
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP,next.timeInMillis,
            AlarmManager.INTERVAL_DAY,pending)
    }

    fun deliver(context:Context) {
        if(!ProgressStore(context).notificationsEnabled() || !allowed(context))return
        val manager=context.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager
        if(Build.VERSION.SDK_INT>=26){
            manager.createNotificationChannel(NotificationChannel(CHANNEL,
                "BOPAVI daily reminders",NotificationManager.IMPORTANCE_DEFAULT))
        }
        val open=PendingIntent.getActivity(context,0,
            Intent(context,MainActivity::class.java).apply {
                flags=Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        // minSdk is 26; every supported device uses notification channels.
        val builder=Notification.Builder(context,CHANNEL)
        manager.notify(REQUEST,builder
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("BOPAVI")
            .setContentText("Your next sky adventure is ready!")
            .setAutoCancel(true)
            .setContentIntent(open)
            .build())
    }

    fun isReminder(intent:Intent?):Boolean = intent?.action==ACTION_REMINDER
}

/** Receives only an actual scheduled reminder or the system boot broadcast. */
class SettingsReminderReceiver:BroadcastReceiver() {
    override fun onReceive(context:Context,intent:Intent?) {
        when(intent?.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                if(ProgressStore(context).notificationsEnabled())
                    SettingsReminder.configure(context,true)
            }
            else -> if(SettingsReminder.isReminder(intent))
                SettingsReminder.deliver(context)
        }
    }
}
