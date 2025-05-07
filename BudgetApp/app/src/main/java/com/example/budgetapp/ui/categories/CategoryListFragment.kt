package com.example.budgetapp.ui.categories

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetapp.R
import com.example.budgetapp.SharedPreferencesManager
import com.example.budgetapp.databinding.FragmentCategoryListBinding
import com.example.budgetapp.model.Category
import com.example.budgetapp.model.TransactionType

class CategoryListFragment : Fragment() {

    private var _binding: FragmentCategoryListBinding? = null
    private val binding get() = _binding!!

    private lateinit var incomeHeaderAdapter: HeaderAdapter
    private lateinit var incomeCategoryAdapter: CategoryAdapter
    private lateinit var expenseHeaderAdapter: HeaderAdapter
    private lateinit var expenseCategoryAdapter: CategoryAdapter
    private lateinit var concatAdapter: ConcatAdapter

    companion object {
        // Используем тот же ключ для добавления и редактирования
        const val ADD_EDIT_CATEGORY_REQUEST_KEY = "addEditCategoryRequest"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryListBinding.inflate(inflater, container, false)
        setupResultListener()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        setupRecyclerView()
        loadCategories()

        binding.fabAddCategory.setOnClickListener {
            showAddCategoryDialog() // Вызываем старый метод для добавления
        }
    }

    private fun setupResultListener() {
        parentFragmentManager.setFragmentResultListener(
            ADD_EDIT_CATEGORY_REQUEST_KEY, // Слушаем общий ключ
            viewLifecycleOwner
        ) { requestKey, bundle ->
            if (requestKey == ADD_EDIT_CATEGORY_REQUEST_KEY) {
                val success = bundle.getBoolean(AddCategoryFragment.RESULT_KEY_SUCCESS, false)
                if (success) {
                    val isEdit = bundle.getBoolean(AddCategoryFragment.RESULT_KEY_IS_EDIT, false)
                    val message = if (isEdit) "Категория обновлена!" else "Категория добавлена!"
                    loadCategories() // Перезагружаем список в обоих случаях
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadCategories()
    }

    private fun setupAdapters() {
        // Обновляем onCategoryClickAction для запуска редактирования
        val onCategoryClickAction = { category: Category ->
            Log.d("CategoryListFragment", "Short click on category: ${category.name}, ID: ${category.id}")
            showEditCategoryDialog(category.id) // Вызываем метод для редактирования
        }
        val onCategoryLongClickAction = { category: Category ->
            showDeleteCategoryConfirmationDialog(category)
            true
        }

        // ... (создание адаптеров без изменений) ...
        incomeHeaderAdapter = HeaderAdapter(getString(R.string.income_categories_header))
        expenseHeaderAdapter = HeaderAdapter(getString(R.string.expense_categories_header))
        incomeCategoryAdapter = CategoryAdapter(mutableListOf(), onCategoryClickAction, onCategoryLongClickAction)
        expenseCategoryAdapter = CategoryAdapter(mutableListOf(), onCategoryClickAction, onCategoryLongClickAction)
        concatAdapter = ConcatAdapter()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewCategories.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = concatAdapter
        }
    }

    private fun loadCategories() {
        val allCategories = SharedPreferencesManager.loadCategories()
        val incomeCategories = allCategories.filter { it.type == TransactionType.INCOME }
        val expenseCategories = allCategories.filter { it.type == TransactionType.EXPENSE }

        incomeCategoryAdapter.updateData(incomeCategories)
        expenseCategoryAdapter.updateData(expenseCategories)

        concatAdapter.adapters.forEach { concatAdapter.removeAdapter(it) }

        if (incomeCategories.isNotEmpty()) {
            concatAdapter.addAdapter(incomeHeaderAdapter)
            concatAdapter.addAdapter(incomeCategoryAdapter)
        }
        if (expenseCategories.isNotEmpty()) {
            concatAdapter.addAdapter(expenseHeaderAdapter)
            concatAdapter.addAdapter(expenseCategoryAdapter)
        }
        updateEmptyView(allCategories.isEmpty())
    }

    private fun updateEmptyView(isEmpty: Boolean) {
        if (isEmpty) {
            binding.textEmptyCategories.visibility = View.VISIBLE
            binding.recyclerViewCategories.visibility = View.GONE
        } else {
            binding.textEmptyCategories.visibility = View.GONE
            binding.recyclerViewCategories.visibility = View.VISIBLE
        }
    }

    // Метод для показа диалога добавления (FAB)
    private fun showAddCategoryDialog() {
        // Вызываем newInstance без ID для режима добавления
        val dialogFragment = AddCategoryFragment.newInstance(null)
        dialogFragment.show(parentFragmentManager, "AddCategoryDialog")
    }

    // Новый метод для показа диалога редактирования (по клику)
    private fun showEditCategoryDialog(categoryId: String) {
        // Вызываем newInstance с ID для режима редактирования
        val dialogFragment = AddCategoryFragment.newInstance(categoryId)
        dialogFragment.show(parentFragmentManager, "EditCategoryDialog") // Можно использовать другой тег
    }


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

    private fun deleteCategory(category: Category) {
        try {
            SharedPreferencesManager.deleteCategory(category.id)
            Toast.makeText(requireContext(), "Категория \"${category.name}\" удалена", Toast.LENGTH_SHORT).show()
            loadCategories()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Ошибка при удалении категории: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}