package com.mth.financetracker.model

import java.util.Date

data class BackupData(
    val transactions: List<Transaction>,
    val currency: String,
    val budget: Double,
    val backupDate: Date
)