package com.reboot.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import com.reboot.app.MainActivity
import com.reboot.app.data.repository.RebootRepository
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

private const val CHANNEL_ID = "reboot_streak_reminders"
private const val NOTIFICATION_ID = 4201
private const val WORK_NAME = "reboot_evening_streak_check"

fun ensureNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID, "Напоминания о серии", NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Напоминает вечером, если серия под угрозой" }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }
}

/** Schedules a daily check around 20:00 local time; the worker itself decides whether there's
 * actually anything worth nagging about (only fires if tasks are still incomplete). */
fun scheduleEveningStreakReminder(context: Context) {
    val now = LocalDateTime.now()
    var target = now.toLocalDate().atTime(LocalTime.of(20, 0))
    if (now.isAfter(target)) target = target.plusDays(1)
    val initialDelay = Duration.between(now, target).toMinutes()

    val request = PeriodicWorkRequestBuilder<StreakReminderWorker>(24, TimeUnit.HOURS)
        .setInitialDelay(initialDelay, TimeUnit.MINUTES)
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request
    )
}

class StreakReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val repository = RebootRepository.getInstance(applicationContext)
        val profile = repository.getUserProfileOnce()
        if (!profile.isLoggedIn || !profile.isOnboarded) return Result.success()

        val notificationsOn = repository.notificationsEnabled.first()
        val silent = repository.silentMode.first()
        if (!notificationsOn || silent) return Result.success()

        val tasks = repository.tasks.first().filterNot { it.isDailyChallenge }
        val allDone = tasks.isNotEmpty() && tasks.all { it.done }
        if (allDone) return Result.success() // nothing to nag about

        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {
            return Result.success()
        }

        ensureNotificationChannel(applicationContext)
        val remaining = tasks.count { !it.done }
        val intent = android.content.Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("🔥 Серия ${profile.streakDays} дней под угрозой")
            .setContentText("Осталось $remaining ${taskWord(remaining)} — не дай серии сгореть")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, notification)
        return Result.success()
    }

    private fun taskWord(n: Int): String {
        val mod100 = n % 100
        val mod10 = n % 10
        return when {
            mod100 in 11..14 -> "задач"
            mod10 == 1 -> "задача"
            mod10 in 2..4 -> "задачи"
            else -> "задач"
        }
    }
}
