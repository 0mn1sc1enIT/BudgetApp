package com.example.budgetapp.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import com.example.budgetapp.R
import com.example.budgetapp.SharedPreferencesManager
import com.example.budgetapp.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Теперь вызываем super.onCreate
        WindowCompat.setDecorFitsSystemWindows(window, true)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Настройка Toolbar
        setSupportActionBar(binding.toolbarSettings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Кнопка назад
        supportActionBar?.title = "Настройки"

        // Загружаем главный фрагмент настроек при первом запуске
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_settings, SettingsMainFragment())
                .commit()
        }
    }

    // Обработка кнопки "назад" в Toolbar
    override fun onSupportNavigateUp(): Boolean {
        // Если есть фрагменты в бэкстеке (например, после перехода к DataManagementFragment),
        // возвращаемся к предыдущему фрагменту. Иначе закрываем Activity.
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            finish()
        }
        return true
    }

    // Обработка системной кнопки "назад" для фрагментов
    @Deprecated("...")
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}