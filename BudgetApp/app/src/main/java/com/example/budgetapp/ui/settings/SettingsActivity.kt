package com.example.budgetapp.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.budgetapp.R
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
        val appBarLayout = binding.appBarLayoutSettings

        ViewCompat.setOnApplyWindowInsetsListener(appBarLayout) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Применяем верхний отступ как padding top для AppBarLayout
            // Это сдвинет Toolbar вниз, но фон AppBarLayout останется под статус баром
            view.updatePadding(top = insets.top)

            // Возвращаем исходные insets, чтобы другие view тоже могли их обработать
            windowInsets
        }
    }

    // Обработка кнопки "назад" в Toolbar
    override fun onSupportNavigateUp(): Boolean {
        // Если есть фрагменты в backstack (например, после перехода к DataManagementFragment),
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