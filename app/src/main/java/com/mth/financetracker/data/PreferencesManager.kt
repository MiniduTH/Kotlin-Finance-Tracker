package com.mth.financetracker.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mth.financetracker.model.Budget
import com.mth.financetracker.model.Transaction
import java.lang.reflect.Type
import java.util.Date

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFERENCES_NAME, Context.MODE_PRIVATE
    )
    private val gson = Gson()

    // Currency preference
    fun setCurrency(currency: String) {
        sharedPreferences.edit().putString(KEY_CURRENCY, currency).apply()
    }

    fun getCurrency(): String {
        return sharedPreferences.getString(KEY_CURRENCY, DEFAULT_CURRENCY) ?: DEFAULT_CURRENCY
    }

    // Budget preference
    fun setBudget(budget: Double) {
        sharedPreferences.edit().putFloat(KEY_BUDGET, budget.toFloat()).apply()
    }

    fun getBudget(): Double {
        return sharedPreferences.getFloat(KEY_BUDGET, 0f).toDouble()
    }

    // Transactions
    fun saveTransactions(transactions: List<Transaction>) {
        val json = gson.toJson(transactions)
        sharedPreferences.edit().putString(KEY_TRANSACTIONS, json).apply()
    }

    fun getTransactions(): List<Transaction> {
        val json = sharedPreferences.getString(KEY_TRANSACTIONS, null) ?: return emptyList()
        val type: Type = object : TypeToken<List<Transaction>>() {}.type
        return gson.fromJson(json, type)
    }

    fun addTransaction(transaction: Transaction) {
        val transactions = getTransactions().toMutableList()
        transactions.add(transaction)
        saveTransactions(transactions)
    }

    fun updateTransaction(transaction: Transaction) {
        val transactions = getTransactions().toMutableList()
        val index = transactions.indexOfFirst { it.id == transaction.id }
        if (index != -1) {
            transactions[index] = transaction
            saveTransactions(transactions)
        }
    }

    fun deleteTransaction(transactionId: String) {
        val transactions = getTransactions().toMutableList()
        transactions.removeAll { it.id == transactionId }
        saveTransactions(transactions)
    }

    companion object {
        private const val PREFERENCES_NAME = "finance_tracker_prefs"
        private const val KEY_CURRENCY = "currency"
        private const val KEY_BUDGET = "budget"
        private const val KEY_TRANSACTIONS = "transactions"
        private const val DEFAULT_CURRENCY = "USD"
    }
}