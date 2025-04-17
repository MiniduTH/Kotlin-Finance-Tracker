package com.mth.financetracker.model

import java.util.Date

data class Budget(
    val id: String,
    val amount: Double,
    val startDate: Date,
    val endDate: Date,
    val categoryId: String? = null  // null means budget for all categories
)