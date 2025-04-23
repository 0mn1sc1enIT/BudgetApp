package com.example.budgetapp

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class BudgetInit : Application() {

    override fun onCreate() {
        super.onCreate()

        SharedPreferencesManager.init(applicationContext)

        // Определяем режим на основе сохраненного boolean значения
        val isDarkMode = SharedPreferencesManager.isDarkModeEnabled() // Новый метод
        val nightMode = if (isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(nightMode) // Устанавливаем режим
    }
}