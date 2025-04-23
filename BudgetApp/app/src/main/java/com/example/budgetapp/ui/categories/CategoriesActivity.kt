package com.example.budgetapp.ui.categories

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import com.example.budgetapp.MainActivity
import com.example.budgetapp.R
import com.example.budgetapp.SharedPreferencesManager
import com.example.budgetapp.databinding.ActivityCategoriesBinding
import com.example.budgetapp.ui.converter.CurrencyConverterActivity
import com.example.budgetapp.ui.settings.SettingsActivity
import com.google.android.material.navigation.NavigationView

class CategoriesActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityCategoriesBinding
    private lateinit var toggle: ActionBarDrawerToggle // Для кнопки-гамбургера

    companion object {
        const val EXTRA_DESTINATION_ID = "com.example.budgetapp.DESTINATION_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Теперь вызываем super.onCreate

        WindowCompat.setDecorFitsSystemWindows(window, true)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- Настройка Toolbar и Drawer ---
        setSupportActionBar(binding.toolbarCategories)

        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayoutCategories, // ID нашего DrawerLayout
            binding.toolbarCategories,      // Toolbar
            R.string.navigation_drawer_open, // Строка для accessibility
            R.string.navigation_drawer_close // Строка для accessibility
        )
        binding.drawerLayoutCategories.addDrawerListener(toggle)
        toggle.syncState() // Показывает иконку-гамбургер и синхронизирует ее состояние

        supportActionBar?.title = "Категории" // Устанавливаем заголовок

        // Устанавливаем слушатель для NavigationView
        binding.navViewCategories.setNavigationItemSelectedListener(this)
        // ----------------------------------

        // Добавляем CategoryListFragment, если Activity создается впервые
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_categories, CategoryListFragment())
                .commit()
        }

        // Выделяем текущий пункт меню "Категории" при запуске
        binding.navViewCategories.setCheckedItem(R.id.nav_categories)
    }

    // Вызывается при выборе пункта в NavigationView
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val destinationId = item.itemId

        when (destinationId) {
            R.id.nav_overview, R.id.nav_transactions_list, R.id.nav_charts -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                // ВОЗВРАЩАЕМ передачу ID назначения
                intent.putExtra(EXTRA_DESTINATION_ID, destinationId)
                startActivity(intent)
                finish()
            }
            R.id.nav_categories -> {
                // Уже здесь
            }
            R.id.nav_settings -> {
                val intent = Intent(this, SettingsActivity::class.java) // Создаем Intent для SettingsActivity
                startActivity(intent) // Запускаем SettingsActivity
            }
            R.id.nav_converter -> {
                val intent = Intent(this, CurrencyConverterActivity::class.java)
                startActivity(intent)
                true
            }
        }
        binding.drawerLayoutCategories.closeDrawer(GravityCompat.START)
        return true
    }

    // Обработка кнопки "назад" - закрываем drawer, если он открыт
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        if (binding.drawerLayoutCategories.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayoutCategories.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed() // Стандартное поведение (выход из Activity)
        }
    }
}