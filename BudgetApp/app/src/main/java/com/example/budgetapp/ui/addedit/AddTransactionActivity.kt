package com.example.budgetapp.ui.addedit

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetapp.R
import com.example.budgetapp.databinding.ActivityAddTransactionBinding

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var formFragment: AddTransactionFormFragment // Ссылка на фрагмент

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    // --- Создание меню в Toolbar (для кнопки "Сохранить") ---
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_save, menu) // Создадим menu_save.xml
        return true
    }

    // --- Обработка нажатия на элементы меню (на кнопку "Сохранить") ---
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                // Вызываем метод сохранения во фрагменте
                formFragment.saveTransaction()
                true // Событие обработано
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}