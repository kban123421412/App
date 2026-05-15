package com.example.moneymatters.util

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

//daily notification even while app is off
class DailyReminderWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        NotificationHelper.showDailyReminderNotification(applicationContext)
        return Result.success()
    }
}