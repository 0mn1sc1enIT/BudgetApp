package com.example.budgetapp.model

import java.util.Date
import java.util.UUID

enum class TransactionType {
    INCOME, // Доход
    EXPENSE // Расход
}

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val type: TransactionType,
    val categoryId: String,
    val description: String?,
    val date: Date = Date()
)
