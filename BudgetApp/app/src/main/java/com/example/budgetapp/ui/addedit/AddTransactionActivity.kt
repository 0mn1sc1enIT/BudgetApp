package com.example.budgetapp.ui.addedit

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import com.example.budgetapp.R
import com.example.budgetapp.databinding.ActivityAddTransactionBinding
import androidx.core.view.WindowCompat

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var formFragment: AddTransactionFormFragment // Ссылка на фрагмент

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, true)

        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Настройка Toolbar
        setSupportActionBar(binding.toolbarAddTransaction)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Показать кнопку "назад" (стрелка)
        supportActionBar?.title = "Добавить транзакцию" // Устанавливаем заголовок

        // Добавляем фрагмент формы, если он еще не добавлен (при повороте экрана и т.д.)
        if (savedInstanceState == null) {
            formFragment = AddTransactionFormFragment() // Создаем экземпляр
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_add_transaction, formFragment)
                .commitNow() // Используем commitNow для синхронного добавления
        } else {
            // Восстанавливаем ссылку на фрагмент после пересоздания Activity
            formFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_add_transaction) as AddTransactionFormFragment
        }
    }

    // Обработка нажатия кнопки "назад" в Toolbar
    override fun onSupportNavigateUp(): Boolean {
        finish() // Просто закрываем Activity
        return true
    }
}