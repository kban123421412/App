package com.example.moneymatters.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.moneymatters.R

object NotificationHelper {
    private const val CHANNEL_ID = "money_matters_channel"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            //app name and description in phone settings
            val name = "Money Matters Notifications"
            val descriptionText = "Reminders and Goal Updates"

            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    //when goal is completed sends notification
    fun showGoalCompletedNotification(context: Context, goalTitle: String) {

        //builds the actual notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Goal Completed! \uD83C\uDF89") //🎉
            .setContentText("Congratulations! You reached your goal for: $goalTitle")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //use a unique id for each notification to avoid conflicts <-- bug fix as all notifications were the same
        notificationManager.notify(goalTitle.hashCode(), builder.build())
    }

    fun showDailyReminderNotification(context: Context) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Daily Expense Reminder")
            .setContentText("Don't forget to log your expenses for today!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1001, builder.build())
    }
}