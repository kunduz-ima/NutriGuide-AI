package com.example.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.ui.screens.Translations

object NotificationHelper {

    private const val CHANNEL_REMINDERS_ID = "nutriguide_reminders"
    private const val CHANNEL_REMINDERS_NAME = "NutriGuide AI Reminders"
    private const val CHANNEL_REMINDERS_DESC = "Notifications for meal times, water targets and daily logs"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_REMINDERS_ID,
                CHANNEL_REMINDERS_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_REMINDERS_DESC
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun showReminderNotification(context: Context, reminderTag: String, title: String, content: String) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderTag.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(reminderTag.hashCode(), builder.build())
    }

    fun getLocalizedNotificationDetails(reminderTag: String, lang: String): Pair<String, String> {
        return when (reminderTag) {
            "breakfast" -> {
                if (lang == "ru") Pair("Время завтрака 🍳", "Пора зафиксировать здоровый клинический завтрак.")
                else Pair("Breakfast Time 🍳", "It is time to log your clinical breakfast nutrition.")
            }
            "lunch" -> {
                if (lang == "ru") Pair("Обед на связи 🥗", "Не забудьте внести ваш питательный обед по диете.")
                else Pair("Lunch Reminder 🥗", "Do not forget to record your nutritious lunch compliance.")
            }
            "dinner" -> {
                if (lang == "ru") Pair("Здоровый ужин 🐟", "Пора записать ужин и проверить дневной лимит солей.")
                else Pair("Dinner Reminder 🐟", "Time for a healthy dinner. Review your daily sodium targets.")
            }
            "water" -> {
                if (lang == "ru") Pair("Режим гидратации 💧", "Выпейте стакан чистой воды и наберите 2.5л сегодня!")
                else Pair("Water Reminder 💧", "Drink a fresh glass of water to support metabolic filtration!")
            }
            else -> {
                if (lang == "ru") Pair("Закрытие дневника 📝", "Дневник питания пустует? Запишите перекусы прямо сейчас!")
                else Pair("Daily Logging Finish 📝", "Ensure all nutrition entries for today are registered safely!")
            }
        }
    }
}
