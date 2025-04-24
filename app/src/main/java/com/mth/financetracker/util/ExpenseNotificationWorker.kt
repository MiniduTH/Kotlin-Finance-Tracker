package com.mth.financetracker.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mth.financetracker.data.PreferencesManager
import java.util.Calendar
import java.util.Date

class ExpenseNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val preferencesManager = PreferencesManager(context)
    private val notificationHelper = NotificationHelper(context)

    override suspend fun doWork(): Result {
        checkUpcomingExpenses()
        return Result.success()
    }

    private fun checkUpcomingExpenses() {
        val transactions = preferencesManager.getTransactions()
        val currency = preferencesManager.getCurrency()
        
        // Get tomorrow's date (start and end)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val tomorrowStart = calendar.time

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val tomorrowEnd = calendar.time

        // Find expenses scheduled for tomorrow
        val tomorrowExpenses = transactions.filter {
            it.type == com.mth.financetracker.model.Transaction.TransactionType.EXPENSE &&
            it.date != null &&
            isDateBetween(it.date, tomorrowStart, tomorrowEnd)
        }

        // Send notification for each upcoming expense
        tomorrowExpenses.forEach { expense ->
            notificationHelper.showUpcomingExpenseNotification(expense, currency)
        }
    }

    private fun isDateBetween(date: Date, start: Date, end: Date): Boolean {
        return date.after(start) && date.before(end) || date.equals(start) || date.equals(end)
    }

    companion object {
        const val WORK_NAME = "expense_notification_worker"
    }
}