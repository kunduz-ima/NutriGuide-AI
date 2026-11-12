package com.example.data.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.NutriGuideApp
import kotlinx.coroutines.flow.firstOrNull

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val reminderTag = inputData.getString("reminder_tag") ?: "logging"

        // Dynamically extract the language preference to show correct notifications
        val app = applicationContext as? NutriGuideApp
        val lang = app?.authRepository?.getLanguageFlow()?.firstOrNull() ?: "en"

        val details = NotificationHelper.getLocalizedNotificationDetails(reminderTag, lang)
        NotificationHelper.showReminderNotification(applicationContext, reminderTag, details.first, details.second)

        return Result.success()
    }
}
