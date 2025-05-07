package com.example.budgetapp.ui.addedit

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.budgetapp.R
import com.example.budgetapp.SharedPreferencesManager
import com.example.budgetapp.databinding.FragmentAddTransactionFormBinding
import com.example.budgetapp.model.Category
import com.example.budgetapp.model.Transaction
import com.example.budgetapp.model.TransactionType
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionFormFragment : Fragment() {

    private var _binding: FragmentAddTransactionFormBinding? = null
    private val binding get() = _binding!!

    private val calendar: Calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    private var categoriesForSpinner: List<Category> = listOf()
    private lateinit var categoryAdapter: ArrayAdapter<String>

    private var selectedCategory: Category? = null

    // Переменные для режима редактирования
    private var transactionIdToEdit: String? = null
    private var transactionToEdit: Transaction? = null // Храним загруженную транзакцию

    companion object {
        private const val ARG_TRANSACTION_ID = "transaction_id"

        fun newInstance(transactionId: String?): AddTransactionFormFragment {
            val fragment = AddTransactionFormFragment()
            val args = Bundle().apply {
                putString(ARG_TRANSACTION_ID, transactionId) // Помещаем ID в аргументы
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Получаем ID из аргументов при создании фрагмента
        arguments?.let {
            transactionIdToEdit = it.getString(ARG_TRANSACTION_ID)
        }
        Log.d("AddTransactionForm", "onCreate - Received transaction ID: $transactionIdToEdit")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("AddTransactionForm", "onViewCreated - Transaction ID: $transactionIdToEdit")

        setupDatePicker()
        setupCategorySpinner()
        setupTransactionTypeListener()

        if (transactionIdToEdit != null && transactionToEdit == null) {
            // Если ID есть, но транзакция еще не загружена (например, после поворота)
            loadTransactionDataAndPopulateForm()
        } else if (transactionIdToEdit != null) {
            // Транзакция уже была загружена ранее (например, до поворота)
            // Просто заполняем форму еще раз, чтобы восстановить состояние UI
            populateFormWithExistingData()
        } else {
            // Режим добавления: инициализируем значения по умолчанию
            updateDateInView() // Устанавливаем текущую дату
            loadCategoriesForType(TransactionType.EXPENSE) // Загружаем категории для расходов по умолчанию
        }

        binding.buttonSaveTransaction.setOnClickListener {
            saveTransaction()
        }
    }

    // Загрузка данных транзакции (вызывается, если transactionToEdit еще null)
    private fun loadTransactionDataAndPopulateForm() {
        Log.d("AddTransactionForm", "Loading transaction data...")
        transactionIdToEdit?.let { id ->
            transactionToEdit = SharedPreferencesManager.loadTransactions().find { it.id == id }
            if (transactionToEdit != null) {
                populateFormWithExistingData() // Заполняем форму после загрузки
            } else {
                Log.e("AddTransactionForm", "Failed to load transaction to edit with ID $id during population")
                Toast.makeText(requireContext(), "Ошибка загрузки транзакции", Toast.LENGTH_SHORT).show()
                activity?.setResult(Activity.RESULT_CANCELED)
                activity?.finish()
            }
        }
    }

    // Заполнение полей формы данными из transactionToEdit
    private fun populateFormWithExistingData() {
        val transaction = transactionToEdit ?: return // Если транзакция null, выходим
        Log.d("AddTransactionForm", "Populating form for transaction: ${transaction.id}")

        binding.editTextAmount.setText(transaction.amount.toString())
        binding.editTextDescription.setText(transaction.description ?: "")
        calendar.time = transaction.date
        updateDateInView()

        if (transaction.type == TransactionType.INCOME) {
            binding.radioIncome.isChecked = true
        } else {
            binding.radioExpense.isChecked = true
        }

        // Загружаем категории для нужного типа и ВЫБИРАЕМ нужную ПОСЛЕ загрузки
        loadCategoriesForType(transaction.type) {
            val categoryPosition = categoriesForSpinner.indexOfFirst { it.id == transaction.categoryId }
            if (categoryPosition >= 0) {
                binding.spinnerCategory.setSelection(categoryPosition)
                selectedCategory = categoriesForSpinner[categoryPosition] // Убедимся, что selectedCategory установлен
                Log.d("AddTransactionForm", "Category '${selectedCategory?.name}' selected at position $categoryPosition")
            } else {
                Log.w("AddTransactionForm", "Category ${transaction.categoryId} not found in spinner for type ${transaction.type}")
                if (categoriesForSpinner.isNotEmpty()) {
                    binding.spinnerCategory.setSelection(0) // Выбираем первую, если не нашли
                    selectedCategory = categoriesForSpinner[0]
                } else {
                    selectedCategory = null // Категорий вообще нет
                }
            }
        }
    }

    private fun setupDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }
        binding.editTextDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        binding.inputLayoutDate.setOnClickListener {
            binding.editTextDate.performClick()
        }
    }

    private fun updateDateInView() {
        binding.editTextDate.setText(dateFormat.format(calendar.time))
    }

    private fun setupCategorySpinner() {
        categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, mutableListOf<String>())
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = categoryAdapter

        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategory = if (position >= 0 && position < categoriesForSpinner.size) {
                    categoriesForSpinner[position]
                } else {
                    null
                }
                Log.d("AddTransactionForm", "Spinner item selected: ${selectedCategory?.name}")
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCategory = null
            }
        }
    }

    private fun setupTransactionTypeListener() {
        binding.radioGroupTransactionType.setOnCheckedChangeListener { _, checkedId ->
            val selectedType = if (checkedId == R.id.radio_income) TransactionType.INCOME else TransactionType.EXPENSE
            Log.d("AddTransactionForm", "Transaction type changed to: $selectedType")
            // Перезагружаем категории и сбрасываем выбор в spinner,
            // так как старая категория может быть невалидна для нового типа
            loadCategoriesForType(selectedType) {
                // После загрузки нового списка категорий, если он не пуст, выбираем первую
                if (categoriesForSpinner.isNotEmpty()) {
                    binding.spinnerCategory.setSelection(0)
                    selectedCategory = categoriesForSpinner[0]
                } else {
                    selectedCategory = null
                }
            }
        }
    }

    // Обновленная версия loadCategoriesForType с callback
    private fun loadCategoriesForType(type: TransactionType, onLoaded: (() -> Unit)? = null) {
        Log.d("AddTransactionForm", "Loading categories for type: $type")
        categoriesForSpinner = SharedPreferencesManager.getCategoriesByType(type)
        val categoryNames = categoriesForSpinner.map { it.name }

        categoryAdapter.clear()
        if (categoryNames.isNotEmpty()) {
            categoryAdapter.addAll(categoryNames)
            binding.spinnerCategory.isEnabled = true // Включаем спиннер.
            // Вызываем callback после обновления данных адаптера.
            // Запускаем через post, чтобы дать время адаптеру обновиться
            binding.spinnerCategory.post {
                // Выбираем первую категорию по умолчанию, если не режим редактирования
                if (transactionToEdit == null || transactionToEdit?.type != type) {
                    binding.spinnerCategory.setSelection(0, false)
                    // Вызываем onItemSelected вручную, чтобы selectedCategory установился
                    binding.spinnerCategory.onItemSelectedListener?.onItemSelected(binding.spinnerCategory, null, 0, 0)
                }
                onLoaded?.invoke() // Вызываем внешний callback
                Log.d("AddTransactionForm", "Categories loaded. Executing onLoaded callback.")
            }
        } else {
            categoryAdapter.add("Нет категорий")
            binding.spinnerCategory.isEnabled = false // Отключаем спиннер
            selectedCategory = null
            // Вызываем callback даже если категорий нет
            binding.spinnerCategory.post {
                onLoaded?.invoke()
                Log.d("AddTransactionForm", "No categories found. Executing onLoaded callback.")
            }
        }
        categoryAdapter.notifyDataSetChanged()
    }

    // Метод сохранения
    fun saveTransaction() {
        // 1. Валидация суммы
        val amountStr = binding.editTextAmount.text.toString()
        if (amountStr.isBlank() || amountStr == ".") { binding.inputLayoutAmount.error = "Введите сумму"; return } else { binding.inputLayoutAmount.error = null }
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) { binding.inputLayoutAmount.error = "Неверная сумма"; return } else { binding.inputLayoutAmount.error = null }

        // 2. Валидация категории
        if (selectedCategory == null) { Toast.makeText(requireContext(), "Выберите категорию", Toast.LENGTH_SHORT).show(); return }

        // 3. Определяем тип транзакции
        val transactionType = when (binding.radioGroupTransactionType.checkedRadioButtonId) {
            R.id.radio_income -> TransactionType.INCOME
            R.id.radio_expense -> TransactionType.EXPENSE
            else -> { Toast.makeText(requireContext(), "Выберите тип", Toast.LENGTH_SHORT).show(); return }
        }

        // 4. Создаем объект Transaction
        val transactionData = Transaction(
            id = transactionIdToEdit ?: UUID.randomUUID().toString(), // Используем старый ID или генерируем новый
            amount = amount,
            type = transactionType,
            categoryId = selectedCategory!!.id,
            description = binding.editTextDescription.text.toString().trim().ifBlank { null },
            date = calendar.time
        )

        // 5. Сохраняем через нужный метод SharedPreferencesManager
        try {
            val isEditMode = (transactionIdToEdit != null)
            if (isEditMode) {
                SharedPreferencesManager.updateTransaction(transactionData)
                Toast.makeText(requireContext(), "Транзакция обновлена", Toast.LENGTH_SHORT).show()
                Log.d("AddTransactionForm", "Transaction updated: ${transactionData.id}")
            } else {
                SharedPreferencesManager.addTransaction(transactionData)
                Toast.makeText(requireContext(), "Транзакция сохранена", Toast.LENGTH_SHORT).show()
                Log.d("AddTransactionForm", "Transaction added: ${transactionData.id}")
            }
            // Устанавливаем результат OK и закрываем Activity
            activity?.setResult(Activity.RESULT_OK)
            activity?.finish()
        } catch (e: Exception) {
            val action = if (transactionIdToEdit != null) "обновлении" else "сохранении"
            Toast.makeText(requireContext(), "Ошибка при $action: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("AddTransactionForm", "Error saving/updating transaction", e)
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d("AddTransactionForm", "onDestroyView")
    }
}