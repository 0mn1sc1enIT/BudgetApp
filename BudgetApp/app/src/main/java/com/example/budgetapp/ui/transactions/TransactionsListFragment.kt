package com.example.budgetapp.ui.transactions

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetapp.SharedPreferencesManager
import com.example.budgetapp.databinding.FragmentTransactionsListBinding
import com.example.budgetapp.model.Transaction
import com.example.budgetapp.model.TransactionType
import java.util.* // Для Date()

class TransactionsListFragment : Fragment() {

    private var _binding: FragmentTransactionsListBinding? = null
    private val binding get() = _binding!!

    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadTransactions()

        // --- Добавление тестовых данных (ВРЕМЕННО, УДАЛИТЬ ПОЗЖЕ) ---
        addSampleDataIfNeeded()
        // -----------------------------------------------------------
    }

    private fun setupRecyclerView() {
        // Создаем адаптер с пустым списком и передаем контекст
        transactionAdapter = TransactionAdapter(listOf(), requireContext())

        binding.recyclerViewTransactions.apply {
            // Устанавливаем LayoutManager (как будут располагаться элементы - линейно)
            layoutManager = LinearLayoutManager(context)
            // Устанавливаем адаптер
            adapter = transactionAdapter
        }
    }

    private fun loadTransactions() {
        // Загружаем транзакции из SharedPreferences
        val transactions = SharedPreferencesManager.loadTransactions()

        // Обновляем данные в адаптере
        transactionAdapter.updateData(transactions)

        // Показываем/скрываем текст "Список пуст"
        updateEmptyView(transactions)
    }

    private fun updateEmptyView(transactions: List<Transaction>) {
        if (transactions.isEmpty()) {
            binding.textEmptyList.visibility = View.VISIBLE
            binding.recyclerViewTransactions.visibility = View.GONE
        } else {
            binding.textEmptyList.visibility = View.GONE
            binding.recyclerViewTransactions.visibility = View.VISIBLE
        }
    }

    // --- ВРЕМЕННЫЙ МЕТОД ДЛЯ ДОБАВЛЕНИЯ ДАННЫХ ---
    private fun addSampleDataIfNeeded() {
        val currentTransactions = SharedPreferencesManager.loadTransactions()
        if (currentTransactions.isEmpty()) {
            val sampleTransactions = listOf(
                Transaction(amount = 1500.0, type = TransactionType.EXPENSE, category = "Продукты", description = "Молоко, хлеб", date = Date()),
                Transaction(amount = 50000.0, type = TransactionType.INCOME, category = "Зарплата", description = "Аванс", date = Date(System.currentTimeMillis() - 86400000 * 2)), // 2 дня назад
                Transaction(amount = 350.0, type = TransactionType.EXPENSE, category = "Транспорт", description = "Метро", date = Date(System.currentTimeMillis() - 86400000)), // Вчера
                Transaction(amount = 2000.0, type = TransactionType.EXPENSE, category = "Развлечения", description = "Кино", date = Date()),
                Transaction(amount = 10000.0, type = TransactionType.INCOME, category = "Фриланс", description = null, date = Date())
            )
            sampleTransactions.forEach { SharedPreferencesManager.addTransaction(it) }
            // Перезагружаем данные после добавления
            loadTransactions()
        }
    }
    // ------------------------------------------------
    fun refreshTransactions() {
        Log.d("TransactionsListFragment", "Refreshing transactions list...")
        loadTransactions() // Просто перезагружаем данные из SharedPreferences
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Очищаем binding
    }
}