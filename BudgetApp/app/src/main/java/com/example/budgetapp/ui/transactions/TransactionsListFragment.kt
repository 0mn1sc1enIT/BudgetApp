package com.example.budgetapp.ui.transactions

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetapp.SharedPreferencesManager
import com.example.budgetapp.databinding.FragmentTransactionsListBinding
import com.example.budgetapp.model.Transaction
import com.example.budgetapp.model.TransactionType
import com.example.budgetapp.ui.addedit.AddTransactionActivity
import java.util.* // Для Date()

class TransactionsListFragment : Fragment() {

    private var _binding: FragmentTransactionsListBinding? = null
    private val binding get() = _binding!!

    private lateinit var transactionAdapter: TransactionAdapter

    private val transactionResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Если транзакция была добавлена ИЛИ изменена, обновляем список
            Log.d("TransactionsListFragment", "Received RESULT_OK from AddTransactionActivity, refreshing list.")
            refreshTransactions() // Вызываем наш метод обновления
        }
    }

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
        transactionAdapter = TransactionAdapter(listOf(), requireContext()) { transaction ->
            // Код, который выполнится при клике на элемент списка
            Log.d("TransactionsListFragment", "Clicked on transaction: ${transaction.id}")
            launchEditTransactionActivity(transaction.id) // Вызываем метод для запуска редактирования
        }

        binding.recyclerViewTransactions.apply {
            // Устанавливаем LayoutManager (как будут располагаться элементы - линейно)
            layoutManager = LinearLayoutManager(context)
            // Устанавливаем адаптер
            adapter = transactionAdapter
        }
    }

    private fun launchEditTransactionActivity(transactionId: String) {
        val intent = Intent(requireContext(), AddTransactionActivity::class.java)
        // Передаем ID транзакции как extra
        intent.putExtra(AddTransactionActivity.EXTRA_TRANSACTION_ID, transactionId)
        // Запускаем Activity и ждем результат
        transactionResultLauncher.launch(intent)
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
            // Получаем ID дефолтных категорий (пример)
            val categoryProducts = SharedPreferencesManager.getCategoriesByType(TransactionType.EXPENSE).find { it.name == "Продукты" }
            val categorySalary = SharedPreferencesManager.getCategoriesByType(TransactionType.INCOME).find { it.name == "Зарплата" }
            val categoryTransport = SharedPreferencesManager.getCategoriesByType(TransactionType.EXPENSE).find { it.name == "Транспорт" }
            val categoryFun = SharedPreferencesManager.getCategoriesByType(TransactionType.EXPENSE).find { it.name == "Развлечения" }
            val categoryFreelance = SharedPreferencesManager.getCategoriesByType(TransactionType.INCOME).find { it.name == "Фриланс" }

            // Создаем транзакции с categoryId (только если категории найдены)
            val sampleTransactions = mutableListOf<Transaction>()
            categoryProducts?.let { sampleTransactions.add(Transaction(amount = 1500.0, type = TransactionType.EXPENSE, categoryId = it.id, description = "Молоко, хлеб", date = Date())) }
            categorySalary?.let { sampleTransactions.add(Transaction(amount = 50000.0, type = TransactionType.INCOME, categoryId = it.id, description = "Аванс", date = Date(System.currentTimeMillis() - 86400000 * 2))) }
            categoryTransport?.let { sampleTransactions.add(Transaction(amount = 350.0, type = TransactionType.EXPENSE, categoryId = it.id, description = "Метро", date = Date(System.currentTimeMillis() - 86400000))) }
            categoryFun?.let { sampleTransactions.add(Transaction(amount = 2000.0, type = TransactionType.EXPENSE, categoryId = it.id, description = "Кино", date = Date())) }
            categoryFreelance?.let { sampleTransactions.add(Transaction(amount = 10000.0, type = TransactionType.INCOME, categoryId = it.id, description = null, date = Date())) }


            sampleTransactions.forEach { SharedPreferencesManager.addTransaction(it) }
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