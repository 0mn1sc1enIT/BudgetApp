package com.example.budgetapp.ui.categories

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetapp.R
import com.example.budgetapp.databinding.ItemCategoryBinding
import com.example.budgetapp.model.Category
import com.example.budgetapp.model.TransactionType

class CategoryAdapter(
    private var categories: MutableList<Category>,
    private val onCategoryClick: (Category) -> Unit, // Лямбда для обычного клика
    private val onCategoryLongClick: (Category) -> Boolean // Лямбда для долгого клика
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding, onCategoryClick, onCategoryLongClick) // Передаем лямбды во ViewHolder
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    // Метод для обновления данных в адаптере
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newCategories: List<Category>) {
        categories.clear()
        categories.addAll(newCategories)
        // Сортируем для наглядности: сначала доходы, потом расходы, внутри по алфавиту
        categories.sortBy { it.name }
        categories.sortBy { it.type }
        notifyDataSetChanged() // Уведомляем адаптер об изменениях
    }

    // ViewHolder для элемента категории
    inner class CategoryViewHolder(
        private val binding: ItemCategoryBinding,
        private val onCategoryClick: (Category) -> Unit,
        private val onCategoryLongClick: (Category) -> Boolean
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            binding.textCategoryName.text = category.name

            // Установка текста и цвета для типа категории
            val context = binding.root.context
            when (category.type) {
                TransactionType.INCOME -> {
                    binding.textCategoryType.text = context.getString(R.string.income) // Используем строку из ресурсов
                    binding.textCategoryType.setTextColor(ContextCompat.getColor(context, R.color.income_color))
                }
                TransactionType.EXPENSE -> {
                    binding.textCategoryType.text = context.getString(R.string.expense) // Используем строку из ресурсов
                    binding.textCategoryType.setTextColor(ContextCompat.getColor(context, R.color.expense_color))
                }
            }

            // Установка слушателей кликов
            binding.root.setOnClickListener {
                onCategoryClick(category)
            }
            binding.root.setOnLongClickListener {
                onCategoryLongClick(category)
            }
        }
    }
}