package com.example.budgetapp.network

import com.google.gson.annotations.SerializedName

// Основной класс ответа
data class ExchangeRateResponse(
    @SerializedName("result") val result: String?, // "success" или "error"
    @SerializedName("base_code") val baseCode: String?, // Базовая валюта (например, "USD")
    @SerializedName("conversion_rates") val conversionRates: Map<String, Double>?, // Карта курсов
    // Дополнительные поля, если нужны (например, для обработки ошибок)
    @SerializedName("error-type") val errorType: String?
)
