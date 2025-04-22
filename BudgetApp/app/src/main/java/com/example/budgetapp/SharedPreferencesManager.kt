package com.example.budgetapp

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.budgetapp.model.Transaction

object SharedPreferencesManager {

    private const val PREFS_NAME = "MyBudgetPrefs" // Имя файла SharedPreferences
    private const val KEY_TRANSACTIONS = "transactions_list" // Ключ для хранения списка транзакций

    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson() // Создаем экземпляр Gson для сериализации/десериализации

    // Метод для инициализации SharedPreferences. Нужно вызвать один раз, например, в Application классе или MainActivity.
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Метод для сохранения списка транзакций
    fun saveTransactions(transactions: List<Transaction>) {
        checkInitialized() // Проверка, что init был вызван
        val jsonTransactions = gson.toJson(transactions) // Конвертируем список в JSON строку
        sharedPreferences.edit().putString(KEY_TRANSACTIONS, jsonTransactions).apply() // Сохраняем строку
    }

    // Метод для загрузки списка транзакций
    fun loadTransactions(): MutableList<Transaction> {
        checkInitialized() // Проверка, что init был вызван
        val jsonTransactions = sharedPreferences.getString(KEY_TRANSACTIONS, null) // Загружаем JSON строку

        // Если строки нет (первый запуск) или она пустая, возвращаем пустой изменяемый список
        if (jsonTransactions.isNullOrEmpty()) {
            return mutableListOf()
        }

        // Используем TypeToken для указания Gson, что мы хотим десериализовать список объектов Transaction
        val type = object : TypeToken<MutableList<Transaction>>() {}.type
        return gson.fromJson(jsonTransactions, type) ?: mutableListOf() // Десериализуем и возвращаем, или пустой список если ошибка
    }

    // Метод для добавления одной транзакции (удобно)
    fun addTransaction(transaction: Transaction) {
        val currentTransactions = loadTransactions()
        currentTransactions.add(transaction)
        saveTransactions(currentTransactions)
    }

    // Метод для удаления транзакции (по ID, например)
    fun deleteTransaction(transactionId: String) {
        val currentTransactions = loadTransactions()
        currentTransactions.removeAll { it.id == transactionId } // Удаляем транзакцию с совпадающим ID
        saveTransactions(currentTransactions)
    }

    // Метод для обновления транзакции
    fun updateTransaction(updatedTransaction: Transaction) {
        val currentTransactions = loadTransactions()
        val index = currentTransactions.indexOfFirst { it.id == updatedTransaction.id }
        if (index != -1) {
            currentTransactions[index] = updatedTransaction
            saveTransactions(currentTransactions)
        }
    }


    // Вспомогательный метод для проверки инициализации
    private fun checkInitialized() {
        if (!::sharedPreferences.isInitialized) {
            throw IllegalStateException("SharedPreferencesManager must be initialized by calling init() first.")
        }
    }

    // Можно добавить методы для очистки данных, если нужно
    fun clearAllTransactions() {
        checkInitialized()
        sharedPreferences.edit().remove(KEY_TRANSACTIONS).apply()
    }
}