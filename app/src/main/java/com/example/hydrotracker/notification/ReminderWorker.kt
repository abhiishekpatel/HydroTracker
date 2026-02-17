package com.example.hydrotracker.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.hydrotracker.HydroTrackApp
import com.example.hydrotracker.MainActivity
import com.example.hydrotracker.R
import kotlinx.coroutines.flow.first
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as HydroTrackApp
        val settings = app.settingsDataStore

        val remindersEnabled = settings.remindersEnabled.first()
        if (!remindersEnabled) return Result.success()

        val wakeTimeStr = settings.wakeTime.first()
        val sleepTimeStr = settings.sleepTime.first()
        val wakeTime = LocalTime.parse(wakeTimeStr)
        val sleepTime = LocalTime.parse(sleepTimeStr)
        val now = LocalTime.now()

        // Only send reminders during waking hours
        if (now.isBefore(wakeTime) || now.isAfter(sleepTime)) {
            return Result.success()
        }

        val goalMl = settings.dailyGoalMl.first()
        val currentTotal = app.repository.getTodayTotal().first()

        if (currentTotal >= goalMl) {
            return Result.success()
        }

        val remaining = goalMl - currentTotal
        val message = buildReminderMessage(currentTotal, goalMl, remaining)

        sendNotification(message)

        return Result.success()
    }

    private fun buildReminderMessage(current: Int, goal: Int, remaining: Int): String {
        val progress = current.toFloat() / goal
        return when {
            progress < 0.25f -> "Time for a glass! You're at ${formatMl(current)} -- ${formatMl(remaining)} to go."
            progress < 0.5f -> "Good progress! ${formatMl(current)} down, ${formatMl(remaining)} remaining."
            progress < 0.75f -> "Over halfway there! ${formatMl(remaining)} left to hit your goal."
            else -> "Almost there! Just ${formatMl(remaining)} more to reach your ${formatMl(goal)} goal."
        }
    }

    private fun formatMl(ml: Int): String {
        return if (ml >= 1000) {
            String.format("%.1fL", ml / 1000f)
        } else {
            "${ml}ml"
        }
    }

    private fun sendNotification(message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, HydroTrackApp.REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("HydroTrack Reminder")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }

    companion object {
        private const val WORK_NAME = "hydro_reminder"

        fun schedule(context: Context, intervalMinutes: Int) {
            val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
                intervalMinutes.toLong(), TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
