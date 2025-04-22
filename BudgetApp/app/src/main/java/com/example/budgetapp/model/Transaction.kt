package com.example.budgetapp.model

import java.util.Date
import java.util.UUID

enum class TransactionType {
    INCOME, // Доход
    EXPENSE // Расход
}

data class Transaction(
    val id: String = UUID.randomUUID().toString(), // Уникальный идентификатор для каждой транзакции
    val amount: Double,                             // Сумма
    val type: TransactionType,                      // Тип (Доход/Расход)
    val category: String,                           // Категория (пока просто строка)
    val description: String?,                       // Описание (необязательное)
    val date: Date = Date()                         // Дата транзакции (по умолчанию - текущая)
) {
    // Пустой конструктор без аргументов может понадобиться некоторым библиотекам
    // (хотя для Gson с data class обычно не требуется)
    // constructor() : this("", 0.0, TransactionType.EXPENSE, "", null, Date())
}
