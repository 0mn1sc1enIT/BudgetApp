package com.example.budgetapp.ui.categories

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog // Импорт для диалога
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ConcatAdapter // Импорт ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetapp.R // Убедись, что R импортирован
import com.example.budgetapp.SharedPreferencesManager
import com.example.budgetapp.databinding.FragmentCategoryListBinding
import com.example.budgetapp.model.Category
import com.example.budgetapp.model.TransactionType // Импорт TransactionType

class CategoryListFragment : Fragment() {

    private var _binding: FragmentCategoryListBinding? = null
    private val binding get() = _binding!!

    // Адаптеры для каждой секции
    private lateinit var incomeHeaderAdapter: HeaderAdapter
    private lateinit var incomeCategoryAdapter: CategoryAdapter
    private lateinit var expenseHeaderAdapter: HeaderAdapter
    private lateinit var expenseCategoryAdapter: CategoryAdapter

    private lateinit var concatAdapter: ConcatAdapter // Главный адаптер

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters() // Инициализируем адаптеры
        setupRecyclerView()
        loadCategories() // Загружаем и разделяем категории

        binding.fabAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        // Перезагружаем при возвращении, так как категории могли измениться
        loadCategories()
    }

    // Инициализация всех адаптеров
    private fun setupAdapters() {
        val onCategoryClickAction = { category: Category ->
            // TODO: Обработка клика (например, переход к редактированию) - Оставляем Toast
            Toast.makeText(requireContext(), "Клик: ${category.name}", Toast.LENGTH_SHORT).show()
        }
        val onCategoryLongClickAction = { category: Category ->
            // Показываем диалог подтверждения удаления
            showDeleteCategoryConfirmationDialog(category)
            true // Возвращаем true, т.к. событие обработано (диалог показан)
        }

        // Создаем адаптеры заголовков
        incomeHeaderAdapter = HeaderAdapter(getString(R.string.income_categories_header)) // Используем строку из ресурсов
        expenseHeaderAdapter = HeaderAdapter(getString(R.string.expense_categories_header)) // Используем строку из ресурсов

        // Создаем адаптеры для списков категорий (пока с пустыми списками)
        incomeCategoryAdapter = CategoryAdapter(mutableListOf(), onCategoryClickAction, onCategoryLongClickAction)
        expenseCategoryAdapter = CategoryAdapter(mutableListOf(), onCategoryClickAction, onCategoryLongClickAction)

        // Создаем ConcatAdapter (пока пустой, добавим адаптеры в loadCategories)
        concatAdapter = ConcatAdapter()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewCategories.apply {
            layoutManager = LinearLayoutManager(context)
            // Устанавливаем ConcatAdapter
            adapter = concatAdapter
        }
    }

    private fun loadCategories() {
        // 1. Загружаем все категории
        val allCategories = SharedPreferencesManager.loadCategories()

        // 2. Разделяем на доходы и расходы
        val incomeCategories = allCategories.filter { it.type == TransactionType.INCOME }
        val expenseCategories = allCategories.filter { it.type == TransactionType.EXPENSE }

        // 3. Обновляем данные в адаптерах категорий
        incomeCategoryAdapter.updateData(incomeCategories)
        expenseCategoryAdapter.updateData(expenseCategories)

        // 4. Пересобираем ConcatAdapter в зависимости от наличия данных
        // Сначала удаляем все предыдущие адаптеры
        concatAdapter.adapters.forEach { concatAdapter.removeAdapter(it) }

        // Добавляем секцию доходов, если есть категории
        if (incomeCategories.isNotEmpty()) {
            concatAdapter.addAdapter(incomeHeaderAdapter)
            concatAdapter.addAdapter(incomeCategoryAdapter)
        }

        // Добавляем секцию расходов, если есть категории
        if (expenseCategories.isNotEmpty()) {
            concatAdapter.addAdapter(expenseHeaderAdapter)
            concatAdapter.addAdapter(expenseCategoryAdapter)
        }

        // 5. Обновляем видимость текста "Список пуст"
        updateEmptyView(allCategories.isEmpty())
    }

    // Теперь принимает Boolean (true, если список ПОЛНОСТЬЮ пуст)
    private fun updateEmptyView(isEmpty: Boolean) {
        if (isEmpty) {
            binding.textEmptyCategories.visibility = View.VISIBLE
            binding.recyclerViewCategories.visibility = View.GONE
        } else {
            binding.textEmptyCategories.visibility = View.GONE
            binding.recyclerViewCategories.visibility = View.VISIBLE
        }
    }

    private fun showAddCategoryDialog() {
        val dialogFragment = AddCategoryFragment()
        dialogFragment.setTargetFragment(this, ADD_CATEGORY_REQUEST_CODE)
        dialogFragment.show(parentFragmentManager, "AddCategoryDialog")
    }

    // Диалог подтверждения удаления категории
    private fun showDeleteCategoryConfirmationDialog(category: Category) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить категорию?")
            .setMessage("Вы уверены, что хотите удалить категорию \"${category.name}\"?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Удалить") { _, _ ->
                deleteCategory(category)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    // Логика удаления категории
    private fun deleteCategory(category: Category) {
        try {
            SharedPreferencesManager.deleteCategory(category.id)
            Toast.makeText(requireContext(), "Категория \"${category.name}\" удалена", Toast.LENGTH_SHORT).show()
            loadCategories() // Перезагружаем список для обновления UI
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Ошибка при удалении категории: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_CATEGORY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            loadCategories() // Перезагружаем, чтобы обновить обе секции
            Toast.makeText(requireContext(), "Категория добавлена!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ADD_CATEGORY_REQUEST_CODE = 101
    }
}