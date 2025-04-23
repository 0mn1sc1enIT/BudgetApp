package com.example.budgetapp.network // или другой пакет

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.github.mikephil.charting.BuildConfig

object NetworkClient {

    // Базовый URL API
    private const val BASE_URL = "https://v6.exchangerate-api.com/" // Убедись, что URL правильный

    // Создаем логгер для OkHttp
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Устанавливаем уровень логирования:
        // В DEBUG сборках показываем всё тело запроса/ответа,
        // в релизных сборках - лучше отключить (NONE) или оставить BASIC.
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    // Создаем OkHttp клиент с логгером и таймаутами
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // Добавляем логгер
        .connectTimeout(30, TimeUnit.SECONDS) // Таймаут соединения
        .readTimeout(30, TimeUnit.SECONDS)    // Таймаут чтения
        .writeTimeout(30, TimeUnit.SECONDS)   // Таймаут записи
        .build()

    // Создаем экземпляр Retrofit "лениво" (при первом обращении)
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)                  // Устанавливаем базовый URL
            .client(okHttpClient)              // Устанавливаем наш OkHttp клиент
            .addConverterFactory(GsonConverterFactory.create()) // Указываем Gson для конвертации JSON
            .build()
    }

    // "Ленивое" создание экземпляра нашего API сервиса
    val apiService: ExchangeRateApiService by lazy {
        retrofit.create(ExchangeRateApiService::class.java)
    }
}