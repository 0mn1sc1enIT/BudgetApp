package com.example.budgetapp // Убедитесь, что пакет правильный

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

@SuppressLint("CustomSplashScreen") // Мы используем библиотеку androidx
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Устанавливаем SplashScreen ДО super.onCreate()
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_splash) // Layout не обязателен для этого подхода

        // Здесь НЕ нужно использовать Handler или Coroutine для задержки.
        // Библиотека сама показывает тему до готовности основного Activity.

        // Опционально: можно удерживать сплеш-скрин дольше, если идет загрузка данных
        // splashScreen.setKeepOnScreenCondition { /* условие удержания, например !viewModel.isReady() */ true } // Пока просто true для примера

        // Сразу переходим к MainActivity
        startMainActivity()
    }

    private fun startMainActivity() {
        // Создаем Intent для перехода на MainActivity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        // Завершаем SplashActivity, чтобы пользователь не мог вернуться на нее кнопкой "назад"
        finish()
    }
}