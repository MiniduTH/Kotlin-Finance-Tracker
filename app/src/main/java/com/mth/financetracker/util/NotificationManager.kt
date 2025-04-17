package com.mth.financetracker.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.mth.financetracker.MainActivity
import com.mth.financetracker.R
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

class NotificationHelper(private val context: Context) {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
        as NotificationManager
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = context.getString(R.string.budget_alert_channel_name)
            val channelDescription = context.getString(R.string.budget_alert_channel_desc)
            val channelImportance = NotificationManager.IMPORTANCE_DEFAULT
            
            val channel = NotificationChannel(CHANNEL_ID, channelName, channelImportance)
            channel.description = channelDescription
            
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun showBudgetWarningNotification(spentAmount: Double, budgetAmount: Double, currencyCode: String) {
        val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        formatter.currency = Currency.getInstance(currencyCode)
        
        val percent = (spentAmount / budgetAmount * 100).toInt()
        val remaining = budgetAmount - spentAmount
        
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0, 
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_budget_warning)
            .setContentTitle(context.getString(R.string.budget_warning_title))
            .setContentText(context.getString(
                R.string.budget_warning_text,
                percent,
                formatter.format(remaining)
            ))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(BUDGET_WARNING_NOTIFICATION_ID, notification)
    }
    
    fun showBudgetExceededNotification(spentAmount: Double, budgetAmount: Double, currencyCode: String) {
        val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        formatter.currency = Currency.getInstance(currencyCode)
        
        val overBudget = spentAmount - budgetAmount
        
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0, 
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_budget_exceeded)
            .setContentTitle(context.getString(R.string.budget_exceeded_title))
            .setContentText(context.getString(
                R.string.budget_exceeded_text,
                formatter.format(overBudget)
            ))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(BUDGET_EXCEEDED_NOTIFICATION_ID, notification)
    }
    
    companion object {
        private const val CHANNEL_ID = "budget_notification_channel"
        private const val BUDGET_WARNING_NOTIFICATION_ID = 1
        private const val BUDGET_EXCEEDED_NOTIFICATION_ID = 2
    }
}