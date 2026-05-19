package com.example.moneymatters.util

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class DailyReminderWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        NotificationHelper.showDailyReminderNotification(applicationContext)
        return Result.success()
    }
}