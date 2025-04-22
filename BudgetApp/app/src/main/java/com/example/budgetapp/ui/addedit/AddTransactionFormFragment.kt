package com.example.budgetapp.ui.addedit

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

    // Переменная для хранения выбранной даты
    private val calendar: Calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    private var categoriesForSpinner: List<Category> = listOf()
    private lateinit var categoryAdapter: ArrayAdapter<String>

    private var selectedCategory: Category? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDatePicker()
        updateDateInView()
        setupCategorySpinner()
        setupTransactionTypeListener() // Слушатель для RadioGroup
        loadCategoriesForType(TransactionType.EXPENSE)

        binding.buttonSaveTransaction.setOnClickListener {
            saveTransaction() // Вызываем тот же метод сохранения, что и раньше
        }
    }

    private fun setupDatePicker() {
        // Создаем слушатель для DatePickerDialog
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView() // Обновляем текст в поле ввода
            }

        // Устанавливаем слушатель клика на поле ввода даты
        binding.editTextDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        // Также обрабатываем клик на родительский TextInputLayout
        binding.inputLayoutDate.setOnClickListener {
            binding.editTextDate.performClick()
        }
    }

    // Обновляет текст в поле даты
    private fun updateDateInView() {
        binding.editTextDate.setText(dateFormat.format(calendar.time))
    }

    private fun setupCategorySpinner() {
        // Создаем адаптер для Spinner (будем показывать только имена категорий)
        categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            mutableListOf<String>()
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = categoryAdapter

        // Слушатель выбора элемента в Spinner
        binding.spinnerCategory.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    // Сохраняем выбранную категорию
                    if (position >= 0 && position < categoriesForSpinner.size) {
                        selectedCategory = categoriesForSpinner[position]
                    } else {
                        selectedCategory = null
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedCategory = null
                }
            }
    }

    private fun setupTransactionTypeListener() {
        binding.radioGroupTransactionType.setOnCheckedChangeListener { _, checkedId ->
            val selectedType =
                if (checkedId == R.id.radio_income) TransactionType.INCOME else TransactionType.EXPENSE
            // Перезагружаем категории в Spinner для выбранного типа
            loadCategoriesForType(selectedType)
        }
    }

    private fun loadCategoriesForType(type: TransactionType) {
        categoriesForSpinner = SharedPreferencesManager.getCategoriesByType(type)
        // Получаем только имена категорий для отображения в адаптере
        val categoryNames = categoriesForSpinner.map { it.name }

        // Обновляем данные в адаптере Spinner
        categoryAdapter.clear()
        if (categoryNames.isNotEmpty()) {
            categoryAdapter.addAll(categoryNames)
            // Сбрасываем выбор, чтобы onItemSelected сработал для первого элемента
            binding.spinnerCategory.setSelection(0, false)
            // Вызываем onItemSelected вручную для установки selectedCategory для первого элемента
            binding.spinnerCategory.post { // Используем post, чтобы дождаться отрисовки
                binding.spinnerCategory.onItemSelectedListener?.onItemSelected(
                    binding.spinnerCategory,
                    null,
                    0,
                    0
                )
            }
        } else {
            // Если категорий нет, показываем сообщение (можно добавить TextView для этого)
            categoryAdapter.add("Нет категорий для этого типа")
            selectedCategory = null
        }
        categoryAdapter.notifyDataSetChanged()

    }

    fun saveTransaction() {
        // 1. Получаем данные из полей ввода
        val amountStr = binding.editTextAmount.text.toString()
        // ... (остальная логика получения данных: description, selectedTypeId) ...

        // 2. Валидация суммы
        if (amountStr.isBlank() || amountStr == ".") {
            binding.inputLayoutAmount.error = "Введите сумму"
            return
        } else {
            binding.inputLayoutAmount.error = null
        }
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.inputLayoutAmount.error = "Неверная сумма"
            return
        } else {
            binding.inputLayoutAmount.error = null
        }

        // 3. Валидация категории (из Spinner)
        if (selectedCategory == null) {
            Toast.makeText(requireContext(), "Выберите категорию", Toast.LENGTH_SHORT).show()
            // Можно добавить подсветку Spinner
            return
        }

        // 4. Определяем тип транзакции
        val transactionType = when (binding.radioGroupTransactionType.checkedRadioButtonId) { // Используем binding здесь
            R.id.radio_income -> TransactionType.INCOME
            R.id.radio_expense -> TransactionType.EXPENSE
            else -> {
                Toast.makeText(requireContext(), "Выберите тип транзакции", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // 5. Создаем объект Transaction
        val newTransaction = Transaction(
            amount = amount,
            type = transactionType,
            categoryId = selectedCategory!!.id, // Используем ID выбранной категории
            description = binding.editTextDescription.text.toString().trim().ifBlank { null }, // Используем binding
            date = calendar.time
        )

        // 6. Сохраняем через SharedPreferencesManager и закрываем Activity
        try {
            SharedPreferencesManager.addTransaction(newTransaction)
            Toast.makeText(requireContext(), "Транзакция сохранена", Toast.LENGTH_SHORT).show()
            activity?.setResult(AppCompatActivity.RESULT_OK) // Устанавливаем результат
            activity?.finish() // Закрываем
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Ошибка сохранения: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}