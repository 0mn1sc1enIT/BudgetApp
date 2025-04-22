package com.example.budgetapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.budgetapp.databinding.ActivityMainBinding
import com.example.budgetapp.ui.transactions.TransactionsListFragment
import com.example.budgetapp.ui.addedit.AddTransactionActivity
import kotlin.jvm.java

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, true)
        // Инициализируем ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализируем SharedPreferencesManager (как и раньше)
        SharedPreferencesManager.init(applicationContext)
        //SharedPreferencesManager.clearAllData()
        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        // Настройка DrawerLayout и NavigationView с NavController
        val drawerLayout = binding.drawerLayout
        val navView = binding.navView

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_overview, R.id.nav_transactions_list, R.id.nav_charts, R.id.nav_categories, R.id.nav_settings
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // --- Настройка FAB (Floating Action Button) ---
        binding.fabAddTransaction.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            addTransactionLauncher.launch(intent)
        }

    }

    private val addTransactionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            // Транзакция была добавлена/изменена, нужно обновить список
            // Нам нужно как-то уведомить текущий видимый фрагмент (если это список транзакций)
            // Это более сложная часть, есть несколько подходов:
            // 1. Использовать общую ViewModel для MainActivity и фрагментов.
            // 2. Использовать FragmentResultListener.
            // 3. Просто заставить текущий фрагмент перезагрузить данные (менее элегантно).

            // Пока простой вариант: попробуем найти TransactionsListFragment и вызвать его метод
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
            val currentFragment = navHostFragment.childFragmentManager.fragments.firstOrNull() // Получаем текущий видимый фрагмент

            if (currentFragment is TransactionsListFragment) {
                // Если текущий фрагмент - это список транзакций, вызываем его метод для обновления
                // Нужно будет добавить такой метод в TransactionsListFragment
                currentFragment.refreshTransactions()
            }
            // TODO: Рассмотреть обновление и для OverviewFragment, если он показывает данные

            Log.d("MainActivity", "Returned from AddTransactionActivity with RESULT_OK")
            Toast.makeText(this, "Список будет обновлен", Toast.LENGTH_SHORT).show()
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