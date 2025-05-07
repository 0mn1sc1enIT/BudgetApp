package com.example.budgetapp.network

import com.google.gson.annotations.SerializedName

data class ExchangeRateResponse(
    @SerializedName("result") val result: String?,
    @SerializedName("base_code") val baseCode: String?,
    @SerializedName("conversion_rates") val conversionRates: Map<String, Double>?,
    @SerializedName("error-type") val errorType: String?
)
