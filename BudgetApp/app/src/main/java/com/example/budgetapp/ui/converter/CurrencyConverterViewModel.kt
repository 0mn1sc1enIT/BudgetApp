package com.example.budgetapp.ui.converter

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetapp.network.NetworkClient
import kotlinx.coroutines.launch

// Data class для хранения одного результата конвертации
data class ConversionResult(
    val currencyCode: String,
    val amount: Double
)

class CurrencyConverterViewModel : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _conversionRates = MutableLiveData<Map<String, Double>?>()
    val conversionRates: LiveData<Map<String, Double>?> get() = _conversionRates

    // Заменяем _convertedAmount на список результатов
    private val _conversionResults = MutableLiveData<List<ConversionResult>?>()
    val conversionResults: LiveData<List<ConversionResult>?> get() = _conversionResults

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // Загрузка курсов остается такой же
    fun fetchRates(apiKey: String, baseCurrency: String = "USD") {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = NetworkClient.apiService.getLatestRates(apiKey, baseCurrency)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.result == "success" && body.conversionRates != null) {
                        _conversionRates.value = body.conversionRates
                        _errorMessage.value = null
                        Log.d("ConverterViewModel", "Rates loaded successfully.")
                    } else {
                        val errorType = body?.errorType ?: "Unknown API error"
                        Log.e("ConverterViewModel", "API Error: $errorType")
                        _errorMessage.value = "Ошибка API: $errorType"
                        _conversionRates.value = null
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "HTTP Error ${response.code()}"
                    Log.e("ConverterViewModel", "HTTP Error: ${response.code()} - $errorMsg")
                    _errorMessage.value = "Ошибка сети: ${response.code()}"
                    _conversionRates.value = null
                }
            } catch (e: Exception) {
                Log.e("ConverterViewModel", "Network Exception: ${e.message}", e)
                _errorMessage.value = "Ошибка сети: ${e.message}"
                _conversionRates.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Функция для расчета ВСЕХ конвертаций
    fun calculateConversions(amount: Double, fromCurrency: String) {
        val rates = _conversionRates.value
        if (rates == null) {
            Log.w("ConverterViewModel", "Cannot calculate conversions, rates not loaded.")
            _conversionResults.value = null // Очищаем результаты
            return
        }
        if (amount == 0.0) {
            _conversionResults.value = emptyList() // Пустой список, если сумма 0
            return
        }


        val fromRate = rates[fromCurrency]
        if (fromRate == null || fromRate == 0.0) {
            Log.e("ConverterViewModel", "Rate for source currency $fromCurrency not found or is zero.")
            _errorMessage.value = "Не найден курс для $fromCurrency"
            _conversionResults.value = null
            return
        }

        val results = mutableListOf<ConversionResult>()
        // Итерируем по всем доступным курсам
        rates.forEach { (targetCurrency, targetRate) ->
            // Конвертируем во все, КРОМЕ исходной валюты
            if (targetCurrency != fromCurrency) {
                if (targetRate != null) { // Убедимся, что курс для целевой валюты есть
                    val resultAmount = amount * (targetRate / fromRate)
                    results.add(ConversionResult(targetCurrency, resultAmount))
                } else {
                    Log.w("ConverterViewModel", "Rate for target currency $targetCurrency not found.")
                }
            }
        }

        // Сортируем результаты по коду валюты для единообразия
        results.sortBy { it.currencyCode }

        _conversionResults.value = results // Обновляем LiveData со списком результатов
        Log.d("ConverterViewModel", "Calculated conversions for $amount $fromCurrency: ${results.size} results.")
    }

    fun clearError() {
        _errorMessage.value = null
    }
}