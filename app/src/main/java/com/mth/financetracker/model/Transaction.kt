package com.mth.financetracker.model

import java.util.Date
import java.util.UUID

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var amount: Double,
    var category: String,
    var date: Date,
    var type: TransactionType,
    var note: String = ""
) {
    enum class TransactionType {
        INCOME, EXPENSE
    }
}