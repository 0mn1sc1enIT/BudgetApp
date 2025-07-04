package com.example.budgetapp // Убедитесь, что пакет правильный

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetapp.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    // Время показа splash-screen в миллисекундах (например, 2 секунды)
    private val splashTimeout = 2000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root) // Устанавливаем наш layout

        // Используем Handler для задержки перехода на MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            // Создаем Intent для перехода на MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            // Завершаем SplashActivity, чтобы пользователь не мог вернуться на нее кнопкой "назад"
            finish()
        }, splashTimeout)
    }
}