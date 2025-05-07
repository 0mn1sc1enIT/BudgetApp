package com.example.budgetapp.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ExchangeRateApiService {

    // Запрос на получение последних курсов для указанной базовой валюты
    @GET("v6/{apiKey}/latest/{baseCurrency}")
    suspend fun getLatestRates(
        @Path("apiKey") apiKey: String,
        @Path("baseCurrency") baseCurrency: String = "USD"
    ): Response<ExchangeRateResponse>
}