package com.example.budgetapp

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.example.budgetapp.model.Category
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.budgetapp.model.Transaction
import com.example.budgetapp.model.TransactionType
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object SharedPreferencesManager {

    private const val PREFS_NAME = "MyBudgetPrefs" // Имя файла SharedPreferences
    private const val KEY_TRANSACTIONS = "transactions_list" // Ключ для хранения списка транзакций
    private const val KEY_CATEGORIES = "categories_list"
    private const val KEY_CURRENCY_SYMBOL = "currency_symbol"
    private const val DEFAULT_CURRENCY_SYMBOL = "₸"

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

    // Сохранение списка категорий
    fun saveCategories(categories: List<Category>) {
        checkInitialized()
        val jsonCategories = gson.toJson(categories)
        sharedPreferences.edit().putString(KEY_CATEGORIES, jsonCategories).apply()
    }

    // Загрузка списка категорий
    fun loadCategories(): MutableList<Category> {
        checkInitialized()
        val jsonCategories = sharedPreferences.getString(KEY_CATEGORIES, null)
        if (jsonCategories.isNullOrEmpty()) {
            // Получаем дефолтные категории
            val defaultCategories = getDefaultCategories().toMutableList()
            // !!!!! СОХРАНЯЕМ ДЕФОЛТНЫЕ КАТЕГОРИИ ПРИ ПЕРВОЙ ЗАГРУЗКЕ !!!!!
            Log.d("SharedPreferencesManager", "No categories found in Prefs. Saving defaults.") // Лог для отладки
            saveCategories(defaultCategories) // <--- ДОБАВЬТЕ ЭТУ СТРОКУ
            // Возвращаем их
            return defaultCategories
        }
        // Если категории уже есть в SharedPreferences, загружаем их из JSON
        try {
            val type = object : TypeToken<MutableList<Category>>() {}.type
            return gson.fromJson(jsonCategories, type) ?: getDefaultCategories().toMutableList() // Возвращаем дефолтные в случае ошибки парсинга
        } catch (e: Exception) {
            Log.e("SharedPreferencesManager", "Error parsing categories JSON", e)
            // Ошибка парсинга, возвращаем и сохраняем дефолтные, чтобы исправить ситуацию
            val defaultCategories = getDefaultCategories().toMutableList()
            saveCategories(defaultCategories)
            return defaultCategories
        }
    }

    // Добавление одной категории
    fun addCategory(category: Category) {
        val currentCategories = loadCategories()
        // Проверка на дубликат имени (можно сделать более строгой)
        if (currentCategories.none { it.name.equals(category.name, ignoreCase = true) && it.type == category.type }) {
            currentCategories.add(category)
            saveCategories(currentCategories)
        }
    }

    // Получение категории по ID
    fun getCategoryById(categoryId: String): Category? {
        return loadCategories().find { it.id == categoryId }
    }

    // Получение категорий определенного типа (доход/расход)
    fun getCategoriesByType(type: TransactionType): List<Category> {
        return loadCategories().filter { it.type == type }
    }

    // Метод для получения стандартного набора категорий
    private fun getDefaultCategories(): List<Category> {
        return listOf(
            // Расходы
            Category(name = "Продукты", type = TransactionType.EXPENSE),
            Category(name = "Транспорт", type = TransactionType.EXPENSE),
            Category(name = "Жилье", type = TransactionType.EXPENSE),
            Category(name = "Кафе и рестораны", type = TransactionType.EXPENSE),
            Category(name = "Развлечения", type = TransactionType.EXPENSE),
            Category(name = "Одежда и обувь", type = TransactionType.EXPENSE),
            Category(name = "Здоровье", type = TransactionType.EXPENSE),
            Category(name = "Подарки", type = TransactionType.EXPENSE),
            Category(name = "Другое (расходы)", type = TransactionType.EXPENSE),
            // Доходы
            Category(name = "Зарплата", type = TransactionType.INCOME),
            Category(name = "Фриланс", type = TransactionType.INCOME),
            Category(name = "Переводы", type = TransactionType.INCOME),
            Category(name = "Инвестиции", type = TransactionType.INCOME),
            Category(name = "Другое (доходы)", type = TransactionType.INCOME)
        )
    }

    // Метод для очистки ВСЕХ данных (транзакций и категорий)
    // Вызовите его ОДИН РАЗ для сброса старых данных транзакций без categoryId
    fun clearAllData() {
        checkInitialized()
        sharedPreferences.edit().clear().apply() // Очищает ВСЕ данные в этом файле SharedPreferences
    }

    // Метод для сохранения символа валюты
    fun saveCurrencySymbol(symbol: String) {
        checkInitialized()
        sharedPreferences.edit().putString(KEY_CURRENCY_SYMBOL, symbol).apply()
    }

    // Метод для загрузки символа валюты
    fun getCurrencySymbol(): String {
        checkInitialized()
        // Возвращаем сохраненный символ или дефолтный, если не сохранен
        return sharedPreferences.getString(KEY_CURRENCY_SYMBOL, DEFAULT_CURRENCY_SYMBOL) ?: DEFAULT_CURRENCY_SYMBOL
    }

    private const val KEY_DARK_MODE_ENABLED = "dark_mode_enabled"

    // Новый метод для сохранения настройки темы (boolean)
    fun saveDarkModeEnabled(isDarkMode: Boolean) {
        checkInitialized()
        sharedPreferences.edit().putBoolean(KEY_DARK_MODE_ENABLED, isDarkMode).apply()
    }

    // Новый метод для загрузки настройки темы (boolean)
    fun isDarkModeEnabled(): Boolean {
        checkInitialized()
        // Возвращаем сохраненное значение, по умолчанию false (светлая тема)
        return sharedPreferences.getBoolean(KEY_DARK_MODE_ENABLED, false)
    }

    fun getCurrencyFormatter(): NumberFormat {
        checkInitialized()
        val savedSymbol = getCurrencySymbol() // Получаем сохраненный символ

        // Создаем форматтер для русской локали (основа)
        val formatter = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))

        // Пытаемся установить символ валюты
        try {
            // NumberFormat использует java.util.Currency. Нам нужно создать
            // фиктивный объект Currency с нужным символом.
            // Это может не всегда работать идеально для всех символов/локалей,
            // но для основных (₽, $, €) должно быть нормально.
            val currency = Currency.getInstance(Locale.getDefault()) // Берем текущую локаль для кода
            val decimalFormatSymbols = (formatter as? java.text.DecimalFormat)?.decimalFormatSymbols ?: java.text.DecimalFormatSymbols.getInstance()
            decimalFormatSymbols.currencySymbol = savedSymbol // Устанавливаем наш символ
            (formatter as? java.text.DecimalFormat)?.decimalFormatSymbols = decimalFormatSymbols

        } catch (e: Exception) {
            // Если что-то пошло не так с Currency или форматированием,
            // логируем ошибку и возвращаем стандартный форматтер
            Log.e("SharedPreferencesManager", "Failed to set custom currency symbol '$savedSymbol'", e)
            // Вернем стандартный форматтер для RU в этом случае
            return NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
        }

        return formatter
    }
}