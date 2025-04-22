package com.example.budgetapp.ui.overview // Убедитесь, что пакет правильный

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat // Для доступа к цветам
import androidx.fragment.app.Fragment
import com.example.budgetapp.R // Для доступа к ресурсам (цветам)
import com.example.budgetapp.SharedPreferencesManager
import com.example.budgetapp.databinding.FragmentOverviewBinding // Импортируем ViewBinding
import com.example.budgetapp.model.Transaction
import com.example.budgetapp.model.TransactionType
import java.text.NumberFormat // Для форматирования валюты
import java.util.Calendar
import java.util.Locale

class OverviewFragment : Fragment() {

    private var _binding: FragmentOverviewBinding? = null
    private val binding get() = _binding!!

    // Форматтер валюты
    private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale("ru", "RU")) // Используем русский формат

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadAndDisplayOverview()
    }

    // Этот метод будет вызываться при возвращении на фрагмент (например, после добавления транзакции)
    // чтобы обновить данные.
    override fun onResume() {
        super.onResume()
        // Перезагружаем данные при каждом возвращении на экран,
        // так как транзакции могли измениться.
        loadAndDisplayOverview()
    }

    private fun loadAndDisplayOverview() {
        // 1. Загружаем все транзакции
        val transactions = SharedPreferencesManager.loadTransactions()

        // 2. Считаем общий баланс
        val totalBalance = calculateTotalBalance(transactions)
        binding.textBalanceAmount.text = currencyFormatter.format(totalBalance)
        // Устанавливаем цвет баланса
        val balanceColorRes = if (totalBalance >= 0) R.color.income_color else R.color.expense_color
        binding.textBalanceAmount.setTextColor(ContextCompat.getColor(requireContext(), balanceColorRes))


        // 3. Считаем доходы и расходы за текущий месяц
        val (monthlyIncome, monthlyExpense) = calculateMonthlySummary(transactions)
        binding.textMonthlyIncome.text = currencyFormatter.format(monthlyIncome)
        binding.textMonthlyExpenses.text = currencyFormatter.format(monthlyExpense)

        // TODO: Позже можно добавить график или более детальную информацию
    }

    // Функция расчета общего баланса
    private fun calculateTotalBalance(transactions: List<Transaction>): Double {
        var balance = 0.0
        for (transaction in transactions) {
            when (transaction.type) {
                TransactionType.INCOME -> balance += transaction.amount
                TransactionType.EXPENSE -> balance -= transaction.amount
            }
        }
        return balance
    }

    // Функция расчета доходов/расходов за текущий месяц
    private fun calculateMonthlySummary(transactions: List<Transaction>): Pair<Double, Double> {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}