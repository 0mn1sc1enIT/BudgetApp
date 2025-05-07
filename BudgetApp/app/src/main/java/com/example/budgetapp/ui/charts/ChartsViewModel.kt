package com.example.budgetapp.ui.charts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.budgetapp.SharedPreferencesManager
import com.example.budgetapp.model.Transaction
import com.example.budgetapp.model.Category
import com.example.budgetapp.model.ChartPeriod
import java.util.Calendar
import java.util.Date

// Общий ViewModel для фрагментов статистики
class ChartsViewModel : ViewModel() {

    // --- Период ---
    private val _selectedPeriod = MutableLiveData<ChartPeriod>(ChartPeriod.CURRENT_MONTH)
    val selectedPeriod: LiveData<ChartPeriod> get() = _selectedPeriod // Только для чтения извне

    // --- Данные ---
    // Загружаем один раз и предоставляем дочерним фрагментам
    private val _allTransactions = MutableLiveData<List<Transaction>>()
    val allTransactions: LiveData<List<Transaction>> get() = _allTransactions

    private val _allCategories = MutableLiveData<Map<String, Category>>() // Map для быстрого доступа
    val allCategories: LiveData<Map<String, Category>> get() = _allCategories

    // Метод для обновления выбранного периода из UI
    fun selectPeriod(period: ChartPeriod) {
        if (_selectedPeriod.value != period) {
            _selectedPeriod.value = period
            // Перезагрузка данных не нужна, так как фильтрация будет во фрагментах
        }
    }

    // Загрузка данных при инициализации ViewModel
    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        _allTransactions.value = SharedPreferencesManager.loadTransactions()
        _allCategories.value = SharedPreferencesManager.loadCategories().associateBy { it.id }
    }

    // --- Вспомогательные функции для фильтрации ---

    companion object {
        fun filterTransactionsByPeriod(transactions: List<Transaction>, period: ChartPeriod): List<Transaction> {
            val calendar = Calendar.getInstance()
            val now = calendar.time

            val startOfPeriod: Date? // Nullable для ALL_TIME
            val endOfPeriod: Date?   // Nullable для ALL_TIME

            when (period) {
                ChartPeriod.CURRENT_MONTH -> {
                    calendar.time = now
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    setCalendarToStartOfDay(calendar)
                    startOfPeriod = calendar.time

                    calendar.time = now
                    calendar.add(Calendar.MONTH, 1)
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    setCalendarToStartOfDay(calendar)
                    calendar.add(Calendar.MILLISECOND, -1) // Конец последнего дня текущего месяца
                    endOfPeriod = calendar.time
                }
                ChartPeriod.PREVIOUS_MONTH -> {
                    calendar.time = now
                    calendar.add(Calendar.MONTH, -1) // Переходим на прошлый месяц
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    setCalendarToStartOfDay(calendar)
                    startOfPeriod = calendar.time

                    calendar.time = now // Возвращаемся к текущей дате
                    calendar.set(Calendar.DAY_OF_MONTH, 1) // Начало текущего месяца
                    setCalendarToStartOfDay(calendar)
                    calendar.add(Calendar.MILLISECOND, -1) // Конец последнего дня предыдущего месяца
                    endOfPeriod = calendar.time
                }
                ChartPeriod.CURRENT_YEAR -> {
                    calendar.time = now
                    calendar.set(Calendar.DAY_OF_YEAR, 1)
                    setCalendarToStartOfDay(calendar)
                    startOfPeriod = calendar.time

                    calendar.time = now
                    calendar.add(Calendar.YEAR, 1)
                    calendar.set(Calendar.DAY_OF_YEAR, 1)
                    setCalendarToStartOfDay(calendar)
                    calendar.add(Calendar.MILLISECOND, -1) // Конец последнего дня текущего года
                    endOfPeriod = calendar.time
                }
                ChartPeriod.ALL_TIME -> {
                    startOfPeriod = null // Нет ограничений
                    endOfPeriod = null   // Нет ограничений
                }
            }

            if (startOfPeriod == null || endOfPeriod == null) {
                return transactions // Для ALL_TIME возвращаем все
            }

            // Фильтруем список
            return transactions.filter {
                !it.date.before(startOfPeriod) && !it.date.after(endOfPeriod)
            }
        }

        // Вспомогательная функция для установки времени на начало дня (00:00:00.000)
        fun setCalendarToStartOfDay(calendar: Calendar) {
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
        }

    }
}