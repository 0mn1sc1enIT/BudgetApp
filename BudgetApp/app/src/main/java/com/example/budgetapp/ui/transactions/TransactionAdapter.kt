package com.example.budgetapp.ui.transactions

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetapp.R
import com.example.budgetapp.SharedPreferencesManager
import com.example.budgetapp.databinding.ItemTransactionBinding // Импорт ViewBinding для элемента списка
import com.example.budgetapp.model.Category
import com.example.budgetapp.model.Transaction
import com.example.budgetapp.model.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val context: Context,
    private val onItemClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    // Форматтер для даты
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    private fun getFormatter(): NumberFormat = SharedPreferencesManager.getCurrencyFormatter()

    // Кэш для загруженных категорий, чтобы не дергать SharedPreferences для каждого элемента
    private var categoriesMap: Map<String, Category> = mapOf()

    // Перезагружаем кэш категорий при обновлении данных адаптера
    init {
        reloadCategoriesCache()
    }

    private fun reloadCategoriesCache() {
        categoriesMap = SharedPreferencesManager.loadCategories().associateBy { it.id }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        // Используем ViewBinding для inflate макета элемента
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        // Получаем категорию из кэша
        val category = categoriesMap[transaction.categoryId]
        holder.bind(transaction, category, SharedPreferencesManager.getCurrencyFormatter())
    }

    override fun getItemCount(): Int = transactions.size

    // Обновляем данные и кэш категорий
    fun updateData(newTransactions: List<Transaction>) {
        transactions = newTransactions
        reloadCategoriesCache() // Обновляем кэш категорий на случай их изменения
        notifyDataSetChanged()
    }

    inner class TransactionViewHolder(
        private val binding: ItemTransactionBinding,
        private val onItemClick: (Transaction) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var currentTransaction: Transaction

        // Теперь bind принимает и категорию
        fun bind(transaction: Transaction, category: Category?, formatter: NumberFormat) {
            currentTransaction = transaction
            // Отображаем имя категории или "Категория не найдена", если что-то пошло не так
            binding.textTransactionCategory.text = category?.name ?: "Категория?"
            binding.textTransactionAmount.text = formatAmount(transaction.amount, transaction.type, formatter) // Передаем тип для знака +/-

            binding.textTransactionDetail.text = if (!transaction.description.isNullOrBlank()) {
                transaction.description
            } else {
                dateFormat.format(transaction.date)
            }

            when (transaction.type) {
                TransactionType.INCOME -> {
                    binding.imageTransactionType.setImageResource(R.drawable.ic_income)
                    binding.textTransactionAmount.setTextColor(ContextCompat.getColor(context, R.color.income_color))
                }
                TransactionType.EXPENSE -> {
                    binding.imageTransactionType.setImageResource(R.drawable.ic_expense)
                    binding.textTransactionAmount.setTextColor(ContextCompat.getColor(context, R.color.expense_color))
                }
            }
            binding.root.setOnClickListener {
                onItemClick(currentTransaction) // Вызываем лямбду при клике
            }
        }

        // Обновляем formatAmount для добавления знака +/-
        private fun formatAmount(amount: Double, type: TransactionType, formatter: NumberFormat): String {
            val sign = if (type == TransactionType.EXPENSE) "-" else "+"
            val formattedAmount = formatter.format(kotlin.math.abs(amount)) // Используем переданный форматтер
            return "$sign $formattedAmount"
        }
    }
}