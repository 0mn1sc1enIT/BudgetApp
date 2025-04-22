package com.example.budgetapp.ui.transactions

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetapp.R
import com.example.budgetapp.databinding.ItemTransactionBinding // Импорт ViewBinding для элемента списка
import com.example.budgetapp.model.Transaction
import com.example.budgetapp.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val context: Context // Контекст нужен для доступа к ресурсам (цвета, строки)
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    // Форматтер для даты
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    // Создает ViewHolder (когда RecyclerView нужен новый элемент)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        // Используем ViewBinding для inflate макета элемента
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    // Связывает данные (transaction) с ViewHolder (элементом списка)
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.bind(transaction)
    }

    // Возвращает общее количество элементов в списке
    override fun getItemCount(): Int = transactions.size

    // Функция для обновления данных в адаптере
    fun updateData(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged() // Уведомляем RecyclerView, что данные изменились (простой способ)
        // Для лучшей производительности позже можно использовать DiffUtil
    }

    // ViewHolder - хранит ссылки на View внутри элемента списка
    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.textTransactionCategory.text = transaction.category
            binding.textTransactionAmount.text = formatAmount(transaction.amount) // Форматируем сумму

            // Устанавливаем описание или дату, если описания нет
            binding.textTransactionDetail.text = if (!transaction.description.isNullOrBlank()) {
                transaction.description
            } else {
                dateFormat.format(transaction.date)
            }

            // Устанавливаем иконку и цвет суммы в зависимости от типа
            when (transaction.type) {
                TransactionType.INCOME -> {
                    binding.imageTransactionType.setImageResource(R.drawable.ic_income)
                    binding.textTransactionAmount.setTextColor(ContextCompat.getColor(context, R.color.income_color)) // Определим цвет в colors.xml
                }
                TransactionType.EXPENSE -> {
                    binding.imageTransactionType.setImageResource(R.drawable.ic_expense)
                    binding.textTransactionAmount.setTextColor(ContextCompat.getColor(context, R.color.expense_color)) // Определим цвет в colors.xml
                }
            }

            // TODO: Добавить обработчик клика на элемент (itemView.setOnClickListener { ... })
            // для перехода к деталям транзакции
        }

        // Вспомогательная функция для форматирования суммы (можно улучшить)
        private fun formatAmount(amount: Double): String {
            // Можно добавить символ валюты из настроек позже
            return String.format(Locale.getDefault(), "%.2f ₽", amount)
        }
    }
}