package com.example.budgetapp.network

import retrofit2.Response // Важно использовать Response от Retrofit для обработки ошибок
import retrofit2.http.GET
import retrofit2.http.Path

interface ExchangeRateApiService {

    // Запрос на получение последних курсов для указанной базовой валюты
    // Пример URL: https://v6.exchangerate-api.com/v6/YOUR_API_KEY/latest/USD
    @GET("v6/{apiKey}/latest/{baseCurrency}")
    suspend fun getLatestRates(
        @Path("apiKey") apiKey: String,         // Твой API ключ будет вставлен сюда
        @Path("baseCurrency") baseCurrency: String = "USD" // Базовая валюта (по умолчанию USD)
    ): Response<ExchangeRateResponse> // Оборачиваем в Response для получения кода ответа и заголовков

    // Можно добавить и другие запросы, например, для конвертации напрямую (если API поддерживает)
    // @GET("v6/{apiKey}/pair/{baseCurrency}/{targetCurrency}/{amount}")
    // suspend fun convertCurrency(...)
}