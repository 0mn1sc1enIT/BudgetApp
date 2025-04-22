package com.example.budgetapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.budgetapp.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализируем ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализируем SharedPreferencesManager (как и раньше)
        SharedPreferencesManager.init(applicationContext)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        val drawerLayout = binding.drawerLayout
        val navView = binding.navView

        // Определяем конфигурацию AppBar: связываем DrawerLayout и указываем top-level destinations
        // Top-level destinations - это экраны, на которых кнопка "Назад" не будет отображаться как стрелка вверх,
        // а будет открывать/закрывать Drawer. Обычно это основные экраны из меню.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_overview, R.id.nav_transactions_list, R.id.nav_charts, R.id.nav_categories, R.id.nav_settings // ID из menu/activity_main_drawer.xml
            ), drawerLayout
        )

        // Связываем ActionBar (Toolbar) с NavController для автоматического обновления заголовка
        // и отображения кнопки навигации (гамбургер/стрелка назад)
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Связываем NavigationView с NavController, чтобы переходы происходили при нажатии на пункты меню
        navView.setupWithNavController(navController)

        // --- Дополнительно: Настройка FAB (Floating Action Button) ---
        binding.fabAddTransaction.setOnClickListener {
            // TODO: Реализовать переход на экран добавления транзакции
            // Например, можно использовать navController.navigate(R.id.action_global_to_addTransactionActivity)
            // или запустить AddTransactionActivity через Intent
            // Пока просто выведем сообщение:
            android.widget.Toast.makeText(this, "Добавить транзакцию (пока не реализовано)", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    // Этот метод необходим для обработки нажатия кнопки "Вверх" (стрелка назад или гамбургер) в ActionBar
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    // TODO: Обработка нажатия системной кнопки "Назад", если Drawer открыт (необязательно, но улучшает UX)
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(androidx.core.view.GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}