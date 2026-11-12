package com.example.data.notification

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    /**
     * Set up all reminders: breakfast, lunch, dinner, water, logging reminders
     */
    fun scheduleAllReminders(context: Context) {
        val workManager = WorkManager.getInstance(context)

        // Cancel previous work to avoid duplicates
        cancelAllReminders(context)

        val reminderTags = listOf("breakfast", "lunch", "dinner", "water", "logging")

        reminderTags.forEach { tag ->
            val data = workDataOf("reminder_tag" to tag)
            
            // Custom delays for testing & demonstration in simulation environments
            val intervalMinutes = when(tag) {
                "breakfast" -> 60L
                "lunch" -> 120L
                "dinner" -> 200L
                "water" -> 45L
                else -> 240L
            }

            val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
                intervalMinutes, TimeUnit.MINUTES
            )
            .setInputData(data)
            .addTag("reminder_$tag")
            .build()

            workManager.enqueueUniquePeriodicWork(
                "nutriguide_reminder_$tag",
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }
    }

    /**
     * Disable and cancel all active notification reminder workers
     */
    fun cancelAllReminders(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag("reminder_breakfast")
        workManager.cancelAllWorkByTag("reminder_lunch")
        workManager.cancelAllWorkByTag("reminder_dinner")
        workManager.cancelAllWorkByTag("reminder_water")
        workManager.cancelAllWorkByTag("reminder_logging")
        workManager.cancelAllWork()
    }

    /**
     * Single-tap quick trigger trigger for testing notifications instantly in streaming preview!
     */
    fun triggerInstantMockReminder(context: Context, tag: String) {
        val data = workDataOf("reminder_tag" to tag)
        val oneOffRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(data)
            .build()
        WorkManager.getInstance(context).enqueue(oneOffRequest)
    }
}
