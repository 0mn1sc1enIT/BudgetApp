package com.example.budgetapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptions // Импортируем NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.budgetapp.databinding.ActivityMainBinding
import com.example.budgetapp.ui.categories.CategoriesActivity
import com.example.budgetapp.ui.converter.CurrencyConverterActivity
import com.example.budgetapp.ui.transactions.TransactionsListFragment
import com.example.budgetapp.ui.overview.OverviewFragment
import com.example.budgetapp.ui.settings.SettingsActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    // ВОЗВРАЩАЕМ companion object с ключом
    companion object {
        const val EXTRA_DESTINATION_ID = "com.example.budgetapp.DESTINATION_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState) // Теперь вызываем super.onCreate
        // ... инициализация binding, SharedPreferences, Toolbar ...
        WindowCompat.setDecorFitsSystemWindows(window, true)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)


        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        val drawerLayout = binding.drawerLayout
        val navView = binding.navView

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_overview, R.id.nav_transactions_list, R.id.nav_charts),
            drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

        // Ручная настройка слушателя NavigationView
        navView.setNavigationItemSelectedListener { menuItem ->
            drawerLayout.closeDrawer(GravityCompat.START)
            val handled = NavigationUI.onNavDestinationSelected(menuItem, navController)
            if (!handled) {
                when (menuItem.itemId) {
                    R.id.nav_categories -> {
                        val intent = Intent(this, CategoriesActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    R.id.nav_settings -> {
                        val intent = Intent(this, SettingsActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    R.id.nav_converter -> {
                        val intent = Intent(this, CurrencyConverterActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    else -> false
                }
            } else {
                true
            }
        }

        // Установка слушателя изменения пункта назначения NavController'а
        // ОСТАВЛЯЕМ ЕГО - он будет обновлять меню ПОСЛЕ навигации
        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d("MainActivity", "Destination changed to: ${destination.label} (ID: ${destination.id})")
            if (navView.menu.findItem(destination.id) != null) {
                // Проверяем перед установкой, чтобы не вызвать лишний цикл обновлений
                if (navView.checkedItem?.itemId != destination.id) {
                    navView.setCheckedItem(destination.id)
                    Log.d("MainActivity", "Set checked item in NavView to: ${destination.id}")
                }
            } else {
                navView.checkedItem?.isChecked = false // Снимаем выделение, если пункта нет в меню
                Log.d("MainActivity", "Destination ID ${destination.id} not found in NavView menu.")
            }
        }

        // Обработчик FAB
        binding.fabAddTransaction.setOnClickListener {
            val intent = Intent(this, com.example.budgetapp.ui.addedit.AddTransactionActivity::class.java)
            addTransactionLauncher.launch(intent)
        }

        // ВОЗВРАЩАЕМ обработку Intent при создании
        handleIntent(intent)
    }

    // ВОЗВРАЩАЕМ onNewIntent
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent) // Обрабатываем новый intent
        setIntent(intent) // Устанавливаем новый intent как текущий
    }

    // УБИРАЕМ onResume (он больше не нужен для синхронизации)
    // override fun onResume() { ... }

    // ВОЗВРАЩАЕМ handleIntent
    private fun handleIntent(intent: Intent) {
        if (intent.hasExtra(EXTRA_DESTINATION_ID)) {
            val destinationId = intent.getIntExtra(EXTRA_DESTINATION_ID, 0)
            Log.d("MainActivity", "Handling Intent with destination ID: $destinationId")

            if (destinationId != 0 && navController.graph.findNode(destinationId) != null) {
                // Проверяем, не находимся ли мы уже там
                if (navController.currentDestination?.id != destinationId) {
                    val navOptions = NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .setPopUpTo(navController.graph.startDestinationId, false)
                        .build()
                    // ВЫПОЛНЯЕМ НАВИГАЦИЮ
                    navController.navigate(destinationId, null, navOptions)
                }
                // Удаляем extra, чтобы не сработал снова
                intent.removeExtra(EXTRA_DESTINATION_ID)
            } else {
                Log.w("MainActivity", "Invalid or missing destination ID in Intent: $destinationId.")
            }
        }
    }

    // Метод синхронизации УДАЛЯЕМ, так как listener делает эту работу
    // private fun synchronizeNavViewSelection() { ... }


    private val addTransactionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            Log.d("MainActivity", "Returned from AddTransactionActivity with RESULT_OK")
            // Обновляем видимый фрагмент
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
            val currentFragment = navHostFragment.childFragmentManager.primaryNavigationFragment
            when (currentFragment) {
                is TransactionsListFragment -> currentFragment.refreshTransactions()
                is OverviewFragment -> currentFragment.loadAndDisplayOverview() // Убедись, что метод доступен
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    @Deprecated("...")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}