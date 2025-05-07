package com.example.budgetapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.budgetapp.databinding.ActivityMainBinding
import com.example.budgetapp.ui.categories.CategoriesActivity
import com.example.budgetapp.ui.converter.CurrencyConverterActivity
import com.example.budgetapp.ui.overview.OverviewFragment
import com.example.budgetapp.ui.settings.SettingsActivity
import com.example.budgetapp.ui.transactions.TransactionsListFragment
import androidx.core.view.get
import androidx.core.view.size


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    companion object {
        const val EXTRA_DESTINATION_ID = "com.example.budgetapp.DESTINATION_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        WindowCompat.setDecorFitsSystemWindows(window, true)

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

        navView.setNavigationItemSelectedListener { menuItem ->
            drawerLayout.closeDrawer(GravityCompat.START)

            val destinationId = menuItem.itemId
            val currentDestinationId = navController.currentDestination?.id

            if (destinationId == currentDestinationId &&
                destinationId != R.id.nav_categories &&
                destinationId != R.id.nav_settings &&
                destinationId != R.id.nav_converter) {
                Log.d("MainActivity", "Already at destination $destinationId")
                return@setNavigationItemSelectedListener true
            }

            var handled = false

            when (destinationId) {
                R.id.nav_overview,
                R.id.nav_transactions_list,
                R.id.nav_charts -> {
                    val navOptions = NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .setPopUpTo(navController.graph.startDestinationId, false) // Возврат к старту перед переходом
                        .setEnterAnim(android.R.anim.fade_in)
                        .setExitAnim(android.R.anim.fade_out)
                        .setPopEnterAnim(android.R.anim.fade_in)
                        .setPopExitAnim(android.R.anim.fade_out)
                        .build()
                    try {
                        navController.navigate(destinationId, null, navOptions)
                        handled = true
                        Log.d("MainActivity", "[Manual Nav] Navigated to fragment ID: $destinationId")
                    } catch (e: Exception) {
                        Log.e("MainActivity", "[Manual Nav] Failed to navigate to $destinationId", e)
                        handled = false
                    }
                }
                R.id.nav_categories -> {
                    val intent = Intent(this, CategoriesActivity::class.java)
                    startActivity(intent)
                    handled = true
                }
                R.id.nav_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    handled = true
                }
                R.id.nav_converter -> {
                    val intent = Intent(this, CurrencyConverterActivity::class.java)
                    startActivity(intent)
                    handled = true
                }
                else -> {
                    handled = false
                }
            }
            handled
        }
        // -----------------------------------------------


        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d("MainActivity", "Destination changed to: ${destination.label} (ID: ${destination.id})")
            // Синхронизируем выделение
            val menu = navView.menu
            for (i in 0 until menu.size) {
                val item = menu[i]
                if (item.hasSubMenu()) {
                    val subMenu = item.subMenu!!
                    for (j in 0 until subMenu.size) {
                        val subItem = subMenu[j]
                        subItem.isChecked = (subItem.itemId == destination.id)
                    }
                } else {
                    item.isChecked = (item.itemId == destination.id)
                }
            }
            if (menu.findItem(destination.id) != null) {
                Log.d("MainActivity", "Set checked item in NavView to: ${destination.id}")
            } else {
                Log.w("MainActivity", "Destination ID ${destination.id} not found in NavView menu.")
            }
        }

        // Обработчик FAB
        binding.fabAddTransaction.setOnClickListener {
            val intent = Intent(this, com.example.budgetapp.ui.addedit.AddTransactionActivity::class.java)
            addTransactionLauncher.launch(intent)
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
        setIntent(intent)
    }

    // Метод обработки Intent (ОСТАВЛЯЕМ)
    private fun handleIntent(intent: Intent) {
        if (intent.hasExtra(EXTRA_DESTINATION_ID)) {
            val destinationId = intent.getIntExtra(EXTRA_DESTINATION_ID, 0)
            Log.d("MainActivity", "Handling Intent with destination ID: $destinationId")
            if (destinationId != 0 && navController.graph.findNode(destinationId) != null) {
                if (navController.currentDestination?.id != destinationId) {
                    val navOptions = NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .setPopUpTo(navController.graph.startDestinationId, false)
                        .build()
                    navController.navigate(destinationId, null, navOptions)
                }
                intent.removeExtra(EXTRA_DESTINATION_ID)
            } else {
                Log.w("MainActivity", "Invalid or missing destination ID in Intent: $destinationId.")
            }
        }
    }

    private val addTransactionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.d("MainActivity", "Returned from AddTransactionActivity with RESULT_OK")
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
            val currentFragment = navHostFragment.childFragmentManager.primaryNavigationFragment
            when (currentFragment) {
                is TransactionsListFragment -> currentFragment.refreshTransactions()
                is OverviewFragment -> currentFragment.loadAndDisplayOverview()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    @Deprecated("Use OnBackPressedDispatcher instead")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}