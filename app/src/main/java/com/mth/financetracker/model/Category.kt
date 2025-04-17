package com.mth.financetracker.model

data class Category(
    val id: String,
    val name: String,
    val iconResId: Int? = null,
    val colorResId: Int? = null
)