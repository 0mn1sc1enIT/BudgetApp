package com.example.budgetapp.ui.addedit

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.budgetapp.R
import com.example.budgetapp.SharedPreferencesManager
import com.example.budgetapp.databinding.ActivityAddTransactionBinding

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTransactionBinding

    companion object {
        // Ключ для передачи ID транзакции на редактирование
        const val EXTRA_TRANSACTION_ID = "com.example.budgetapp.TRANSACTION_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // super.onCreate() вызывается первым
        WindowCompat.setDecorFitsSystemWindows(window, true)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarAddTransaction)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Получаем ID транзакции из Intent, если он есть
        val transactionIdToEdit = intent.getStringExtra(EXTRA_TRANSACTION_ID)
        var isEditMode = false // Флаг режима

        if (transactionIdToEdit != null) {
            // Проверяем, существует ли транзакция
            val transactionExists = SharedPreferencesManager.loadTransactions()
                .any { it.id == transactionIdToEdit }
            if (transactionExists) {
                // Режим редактирования
                isEditMode = true
                Log.d("AddTransactionActivity", "Edit mode, transaction ID: $transactionIdToEdit")
                supportActionBar?.title = "Редактировать транзакцию" // Меняем заголовок
            } else {
                // Ошибка - транзакция не найдена
                Log.e("AddTransactionActivity", "Transaction with ID $transactionIdToEdit not found in storage!")
                Toast.makeText(this, "Ошибка: Транзакция не найдена", Toast.LENGTH_LONG).show()
                setResult(RESULT_CANCELED) // Устанавливаем результат отмены
                finish() // Закрываем Activity
                return // Прерываем onCreate
            }
        } else {
            // Режим добавления
            Log.d("AddTransactionActivity", "Add mode")
            supportActionBar?.title = "Добавить транзакцию"
        }

        // Добавляем фрагмент формы, если он еще не добавлен.
        // Передаем ID транзакции (или null) в аргументы фрагмента
        if (savedInstanceState == null) {
            val fragmentToShow = AddTransactionFormFragment.newInstance(transactionIdToEdit)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_add_transaction, fragmentToShow)
                .commitNow() // Используем commitNow для синхронности, если нужно сразу работать с фрагментом
        }

        val appBarLayout = binding.appBarLayoutAdd // Получите AppBarLayout по ID из binding

        ViewCompat.setOnApplyWindowInsetsListener(appBarLayout) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Применяем верхний отступ как padding top для AppBarLayout
            // Это сдвинет Toolbar вниз, но фон AppBarLayout останется под статус баром
            view.updatePadding(top = insets.top)

            // Возвращаем исходные insets, чтобы другие view тоже могли их обработать
            windowInsets
        }
    }

    // Обработка нажатия кнопки "назад" в Toolbar
    override fun onSupportNavigateUp(): Boolean {
        // Устанавливаем результат CANCELLED, если пользователь ушел кнопкой назад
        setResult(RESULT_CANCELED)
        finish()
        return true
    }

    // Обработка системной кнопки "назад"
    @Deprecated("This method has been deprecated in favor of using the OnBackPressedDispatcher")
    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        super.onBackPressed()
    }
}