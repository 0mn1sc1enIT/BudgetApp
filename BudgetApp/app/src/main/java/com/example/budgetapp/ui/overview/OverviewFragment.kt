package com.example.budgetapp.ui.overview

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetapp.R
import com.example.budgetapp.SharedPreferencesManager
import com.example.budgetapp.databinding.FragmentOverviewBinding
import com.example.budgetapp.model.Transaction
import com.example.budgetapp.model.TransactionType
import com.example.budgetapp.ui.transactions.TransactionAdapter
import java.util.Calendar

class OverviewFragment : Fragment() {

    private var _binding: FragmentOverviewBinding? = null
    private val binding get() = _binding!!

    // Адаптер для недавних транзакций
    private lateinit var recentTransactionAdapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecentTransactionsRecyclerView() // Настраиваем RecyclerView
        loadAndDisplayOverview() // Загружаем все данные

        // Настраиваем кнопку "Все"
        binding.buttonSeeAllTransactions.setOnClickListener {
            try {
                // Переходим к фрагменту списка всех транзакций
                findNavController().navigate(R.id.nav_transactions_list)
            } catch (e: Exception) {
                Log.e("OverviewFragment", "Navigation failed", e)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadAndDisplayOverview() // Обновляем все данные при возвращении
    }

    // Настройка RecyclerView для недавних транзакций
    private fun setupRecentTransactionsRecyclerView() {
        // Создаем адаптер. Клик по элементу здесь пока не обрабатываем (пустая лямбда)
        recentTransactionAdapter = TransactionAdapter(mutableListOf(), requireContext()) { }

        binding.recyclerViewRecentTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recentTransactionAdapter
            // Отключаем вложенную прокрутку, так как у нас NestedScrollView
            isNestedScrollingEnabled = false
        }
    }


    internal fun loadAndDisplayOverview() {
        Log.d("OverviewFragment", "Loading and displaying overview data...")
        val transactions = SharedPreferencesManager.loadTransactions()
        val formatter = SharedPreferencesManager.getCurrencyFormatter() // Получаем актуальный formatter

        // --- Общий баланс ---
        val totalBalance = calculateTotalBalance(transactions)
        binding.textBalanceAmount.text = formatter.format(totalBalance)
        val balanceColorRes = if (totalBalance >= 0) R.color.income_color else R.color.expense_color
        binding.textBalanceAmount.setTextColor(ContextCompat.getColor(requireContext(), balanceColorRes))

        // --- Сводка за месяц ---
        val (monthlyIncome, monthlyExpense) = calculateMonthlySummary(transactions)
        binding.textMonthlyIncome.text = formatter.format(monthlyIncome)
        binding.textMonthlyExpenses.text = formatter.format(monthlyExpense)

        // --- Недавние транзакции ---
        // Сортируем по дате (самые новые сначала)
        val sortedTransactions = transactions.sortedByDescending { it.date }
        // Берем первые N (например, 5)
        val recentTransactions = sortedTransactions.take(5)

        Log.d("OverviewFragment", "Recent transactions count: ${recentTransactions.size}")

        // Обновляем адаптер недавних транзакций
        recentTransactionAdapter.updateData(recentTransactions)

        // Показываем/скрываем RecyclerView и текст "Нет недавних транзакций"
        if (recentTransactions.isEmpty()) {
            binding.recyclerViewRecentTransactions.visibility = View.GONE
            binding.textNoRecentTransactions.visibility = View.VISIBLE
        } else {
            binding.recyclerViewRecentTransactions.visibility = View.VISIBLE
            binding.textNoRecentTransactions.visibility = View.GONE
        }
    }

    // --- Функции расчета (без изменений) ---
    private fun calculateTotalBalance(transactions: List<Transaction>): Double { /* ... */
        var balance = 0.0
        for (transaction in transactions) {
            when (transaction.type) {
                TransactionType.INCOME -> balance += transaction.amount
                TransactionType.EXPENSE -> balance -= transaction.amount
            }
        }
        return balance
    }
    private fun calculateMonthlySummary(transactions: List<Transaction>): Pair<Double, Double> { /* ... */
        var income = 0.0
        var expense = 0.0
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val transactionCalendar = Calendar.getInstance()

        for (transaction in transactions) {
            transactionCalendar.time = transaction.date
            val transactionMonth = transactionCalendar.get(Calendar.MONTH)
            val transactionYear = transactionCalendar.get(Calendar.YEAR)

            if (transactionYear == currentYear && transactionMonth == currentMonth) {
                when (transaction.type) {
                    TransactionType.INCOME -> income += transaction.amount
                    TransactionType.EXPENSE -> expense += transaction.amount
                }
            }
        }
        return Pair(income, expense)
    }
    // ------------------------------------

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}