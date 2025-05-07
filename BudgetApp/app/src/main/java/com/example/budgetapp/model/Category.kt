package com.example.budgetapp.model

import java.util.UUID

data class Category(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: TransactionType
)
