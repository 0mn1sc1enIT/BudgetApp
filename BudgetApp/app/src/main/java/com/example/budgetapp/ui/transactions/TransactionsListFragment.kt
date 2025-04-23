package com.example.budgetapp.ui.transactions

import android.os.Bundle
import android.util.Log
import android.view.* // Импорт для Menu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog // Импорт AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider // Импорт для нового API меню
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetapp.R
import com.example.budgetapp.SharedPreferencesManager
import com.example.budgetapp.databinding.FragmentTransactionsListBinding
import com.example.budgetapp.model.Transaction
import com.example.budgetapp.model.TransactionType
import com.google.android.material.chip.Chip // Импорт Chip
import com.google.android.material.chip.ChipGroup // Импорт ChipGroup
import java.util.*
import kotlin.Comparator // Для сортировки

// Enum для режимов сортировки
enum class SortMode {
    DATE_DESC, // По дате (новые сначала) - По умолчанию
    DATE_ASC,  // По дате (старые сначала)
    AMOUNT_DESC, // По сумме (убывание)
    AMOUNT_ASC   // По сумме (возрастание)
}

class TransactionsListFragment : Fragment(), MenuProvider { // Реализуем MenuProvider

    private var _binding: FragmentTransactionsListBinding? = null
    private val binding get() = _binding!!

    private lateinit var transactionAdapter: TransactionAdapter
    private var allTransactions: List<Transaction> = listOf() // Храним полный список

    // Переменные для хранения текущих фильтров и сортировки
    private var currentFilterType: TransactionType? = null // null - все типы
    private var currentFilterCategoryIds: Set<String> = emptySet() // Пустое множество - все категории
    private var currentSortMode: SortMode = SortMode.DATE_DESC // Сортировка по умолчанию

    // --- Lifecycle ---
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настройка нового API меню
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        setupRecyclerView()
        loadAndFilterTransactions() // Загружаем и фильтруем при создании
        addSampleDataIfNeeded()
    }

    // --- MenuProvider Implementation ---
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.transactions_list_menu, menu) // Инфлейтим наше меню
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_filter -> {
                showFilterDialog() // Показываем общий диалог фильтрации
                true // Событие обработано
            }
            R.id.action_sort -> {
                showSortDialog() // Показываем диалог сортировки
                true // Событие обработано
            }
            else -> false // Передать обработку другим
        }
    }
    // -------------------------------

    private fun setupRecyclerView() {
        // Клик на элемент списка теперь используется для редактирования
        transactionAdapter = TransactionAdapter(listOf(), requireContext()) { transaction ->
            // TODO: Запуск редактирования (если нужно из этого фрагмента)
            // launchEditTransactionActivity(transaction.id)
            Toast.makeText(requireContext(), "Клик (редактирование скоро): ${transaction.id}", Toast.LENGTH_SHORT).show()
        }

        binding.recyclerViewTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionAdapter
        }
    }

    // Основной метод загрузки, фильтрации и сортировки
    private fun loadAndFilterTransactions() {
        allTransactions = SharedPreferencesManager.loadTransactions() // Загружаем все
        applyFiltersAndSort() // Применяем фильтры и сортировку
    }

    // Применяет текущие фильтры и сортировку к allTransactions и обновляет адаптер
    private fun applyFiltersAndSort() {
        var filteredList = allTransactions

        // 1. Фильтрация по типу
        currentFilterType?.let { type ->
            filteredList = filteredList.filter { it.type == type }
        }

        // 2. Фильтрация по категориям
        if (currentFilterCategoryIds.isNotEmpty()) {
            filteredList = filteredList.filter { currentFilterCategoryIds.contains(it.categoryId) }
        }

        // 3. Сортировка
        val comparator: Comparator<Transaction> = when (currentSortMode) {
            SortMode.DATE_DESC -> compareByDescending { it.date }
            SortMode.DATE_ASC -> compareBy { it.date }
            SortMode.AMOUNT_DESC -> compareByDescending { it.amount }
            SortMode.AMOUNT_ASC -> compareBy { it.amount }
        }
        val sortedList = filteredList.sortedWith(comparator)

        // 4. Обновление адаптера и UI
        transactionAdapter.updateData(sortedList)
        updateEmptyView(sortedList)
        Log.d("TransactionsList", "Applied filters: Type=${currentFilterType}, Categories=${currentFilterCategoryIds.size}, Sort=${currentSortMode}. Result size: ${sortedList.size}")
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

    // --- Диалоги Фильтрации и Сортировки ---

    private fun showFilterDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Фильтры")

        // Используем кастомный layout для диалога (создадим его позже)
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_filter_transactions, null)
        builder.setView(dialogView)

        val chipGroupType: ChipGroup = dialogView.findViewById(R.id.chipgroup_filter_type)
        val chipGroupCategory: ChipGroup = dialogView.findViewById(R.id.chipgroup_filter_category)

        // Настройка фильтра по типу
        setupTypeFilterChips(chipGroupType)
        // Настройка фильтра по категориям
        setupCategoryFilterChips(chipGroupCategory)

        builder.setPositiveButton("Применить") { _, _ ->
            // Сохраняем выбранные фильтры из ChipGroup'ов (реализуем позже)
            updateFiltersFromDialog(chipGroupType, chipGroupCategory)
            applyFiltersAndSort() // Применяем фильтры
        }
        builder.setNegativeButton("Отмена", null)
        builder.setNeutralButton("Сбросить") { _, _ ->
            // Сбрасываем фильтры и применяем
            currentFilterType = null
            currentFilterCategoryIds = emptySet()
            applyFiltersAndSort()
        }

        builder.show()
    }

    private fun setupTypeFilterChips(chipGroup: ChipGroup) {
        // Добавляем чипы для выбора типа
        val types = mapOf(
            null to "Все", // null соответствует отсутствию фильтра
            TransactionType.INCOME to "Доходы",
            TransactionType.EXPENSE to "Расходы"
        )
        // Устанавливаем режим выбора одного чипа
        chipGroup.isSingleSelection = true
        chipGroup.isSelectionRequired = true // Хотя бы один должен быть выбран

        types.forEach { (type, name) ->
            val chip = Chip(context).apply {
                text = name
                isCheckable = true
                isChecked = (currentFilterType == type) // Отмечаем текущий фильтр
                tag = type // Сохраняем TransactionType? в теге для легкого доступа
            }
            chipGroup.addView(chip)
        }
    }

    private fun setupCategoryFilterChips(chipGroup: ChipGroup) {
        chipGroup.removeAllViews() // Очищаем на случай повторного открытия
        val categories = SharedPreferencesManager.loadCategories().sortedBy { it.name } // Загружаем и сортируем

        if (categories.isEmpty()) {
            // Если категорий нет, можно показать сообщение или скрыть ChipGroup
            // chipGroup.visibility = View.GONE
            return
        }

        // Устанавливаем режим выбора нескольких чипов
        chipGroup.isSingleSelection = false

        categories.forEach { category ->
            val chip = Chip(context).apply {
                text = category.name
                isCheckable = true
                // Отмечаем чип, если его ID есть в текущем наборе фильтров
                isChecked = currentFilterCategoryIds.contains(category.id)
                tag = category.id // Сохраняем ID категории в теге
            }
            chipGroup.addView(chip)
        }
    }

    private fun updateFiltersFromDialog(typeGroup: ChipGroup, categoryGroup: ChipGroup) {
        // Обновляем фильтр по типу
        val checkedTypeId = typeGroup.checkedChipId
        currentFilterType = if (checkedTypeId != View.NO_ID) {
            typeGroup.findViewById<Chip>(checkedTypeId)?.tag as? TransactionType?
        } else {
            null // На случай, если ничего не выбрано (хотя isSelectionRequired=true)
        }

        // Обновляем фильтр по категориям
        val selectedCategoryIds = mutableSetOf<String>()
        categoryGroup.checkedChipIds.forEach { chipId ->
            val chip = categoryGroup.findViewById<Chip>(chipId)
            (chip?.tag as? String)?.let { categoryId ->
                selectedCategoryIds.add(categoryId)
            }
        }
        currentFilterCategoryIds = selectedCategoryIds
    }


    private fun showSortDialog() {
        val sortOptions = arrayOf(
            "Дата (новые сначала)",
            "Дата (старые сначала)",
            "Сумма (убывание)",
            "Сумма (возрастание)"
        )
        // Находим индекс текущей сортировки
        val currentSortIndex = when (currentSortMode) {
            SortMode.DATE_DESC -> 0
            SortMode.DATE_ASC -> 1
            SortMode.AMOUNT_DESC -> 2
            SortMode.AMOUNT_ASC -> 3
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Сортировка")
            .setSingleChoiceItems(sortOptions, currentSortIndex) { dialog, which ->
                // Обновляем режим сортировки при выборе
                currentSortMode = when (which) {
                    0 -> SortMode.DATE_DESC
                    1 -> SortMode.DATE_ASC
                    2 -> SortMode.AMOUNT_DESC
                    3 -> SortMode.AMOUNT_ASC
                    else -> SortMode.DATE_DESC
                }
                applyFiltersAndSort() // Применяем новую сортировку
                dialog.dismiss() // Закрываем диалог
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    // --- Остальные методы ---
    private fun addSampleDataIfNeeded() { /* ... */ }
    fun refreshTransactions() { Log.d("TransactionsList", "Refreshing..."); loadAndFilterTransactions() } // Обновляем с фильтрами
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
    // ----------------------
}