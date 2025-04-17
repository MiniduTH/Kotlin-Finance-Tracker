package com.mth.financetracker.util

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.google.gson.Gson
import com.mth.financetracker.data.PreferencesManager
import com.mth.financetracker.model.BackupData
import com.mth.financetracker.model.Transaction
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupManager(private val context: Context) {
    
    private val preferencesManager = PreferencesManager(context)
    private val gson = Gson()
    
    fun createBackup(uri: Uri): Boolean {
        try {
            // Create backup data
            val transactions = preferencesManager.getTransactions()
            val currency = preferencesManager.getCurrency()
            val budget = preferencesManager.getBudget()
            
            val backupData = BackupData(
                transactions = transactions,
                currency = currency,
                budget = budget,
                backupDate = Date()
            )
            
            // Convert to JSON
            val jsonData = gson.toJson(backupData)
            
            // Write to file
            context.contentResolver.openFileDescriptor(uri, "w")?.use { parcelFileDescriptor ->
                FileOutputStream(parcelFileDescriptor.fileDescriptor).use { outputStream ->
                    outputStream.write(jsonData.toByteArray())
                }
            }
            
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_SHORT).show()
            return false
        }
    }
    
    fun restoreFromBackup(uri: Uri): Boolean {
        try {
            val stringBuilder = StringBuilder()
            
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                    }
                }
            }
            
            val jsonData = stringBuilder.toString()
            val backupData = gson.fromJson(jsonData, BackupData::class.java)
            
            // Restore data
            preferencesManager.setCurrency(backupData.currency)
            preferencesManager.setBudget(backupData.budget)
            preferencesManager.saveTransactions(backupData.transactions)
            
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val backupDate = dateFormat.format(backupData.backupDate)
            
            Toast.makeText(
                context, 
                "Backup restored from $backupDate with ${backupData.transactions.size} transactions", 
                Toast.LENGTH_SHORT
            ).show()
            
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Restore failed: ${e.message}", Toast.LENGTH_SHORT).show()
            return false
        }
    }
}