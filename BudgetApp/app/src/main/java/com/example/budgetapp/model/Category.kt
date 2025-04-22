package com.example.budgetapp.model

import java.util.UUID

data class Category(
    val id: String = UUID.randomUUID().toString(), // Уникальный ID категории
    val name: String,                              // Название категории (например, "Продукты", "Зарплата")
    val type: TransactionType                      // Тип (INCOME или EXPENSE), чтобы знать, где ее использовать
)
