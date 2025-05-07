package com.example.budgetapp.ui.categories

import android.os.Bundle
import android.util.Log // Добавим логирование
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.example.budgetapp.R
import com.example.budgetapp.SharedPreferencesManager
import com.example.budgetapp.databinding.DialogAddCategoryBinding
import com.example.budgetapp.model.Category
import com.example.budgetapp.model.TransactionType

class AddCategoryFragment : DialogFragment() {

    private var _binding: DialogAddCategoryBinding? = null
    private val binding get() = _binding!!

    private var categoryIdToEdit: String? = null
    private var categoryToEdit: Category? = null
    private var isEditMode = false

    companion object {
        // Ключи для аргументов и результатов
        private const val ARG_CATEGORY_ID = "category_id_to_edit"
        const val REQUEST_KEY = "addEditCategoryRequest" // Общий ключ запроса
        const val RESULT_KEY_SUCCESS = "categorySuccess"
        const val RESULT_KEY_IS_EDIT = "isEditResult" // Ключ для отличия результата редактирования

        // Фабричный метод для создания экземпляра
        fun newInstance(categoryId: String? = null): AddCategoryFragment {
            val fragment = AddCategoryFragment()
            val args = Bundle().apply {
                putString(ARG_CATEGORY_ID, categoryId) // Передаем ID (может быть null для добавления)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            categoryIdToEdit = it.getString(ARG_CATEGORY_ID)
            isEditMode = categoryIdToEdit != null
        }
        Log.d("AddCategoryFragment", "onCreate - Edit mode: $isEditMode, Category ID: $categoryIdToEdit")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogAddCategoryBinding.inflate(inflater, container, false)
        // Устанавливаем заголовок в зависимости от режима
        dialog?.setTitle(if (isEditMode) "Редактировать категорию" else "Новая категория")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()

        // Если режим редактирования, загружаем и заполняем данные
        if (isEditMode && categoryToEdit == null) { // Загружаем только если еще не загружено
            loadCategoryDataForEdit()
        } else if (isEditMode && categoryToEdit != null) {
            // Восстанавливаем состояние после поворота и т.д.
            populateFormForEdit()
        }
        // В режиме добавления ничего специально делать не нужно, поля пустые
    }

    private fun loadCategoryDataForEdit() {
        categoryIdToEdit?.let { id ->
            Log.d("AddCategoryFragment", "Loading category data for ID: $id")
            categoryToEdit = SharedPreferencesManager.getCategoryById(id)
            if (categoryToEdit != null) {
                populateFormForEdit()
            } else {
                Log.e("AddCategoryFragment", "Category with ID $id not found!")
                Toast.makeText(requireContext(), "Ошибка: Категория не найдена", Toast.LENGTH_SHORT).show()
                dismissAllowingStateLoss() // Закрываем диалог, если категория не найдена
            }
        }
    }

    private fun populateFormForEdit() {
        categoryToEdit?.let { category ->
            Log.d("AddCategoryFragment", "Populating form for category: ${category.name}")
            binding.editTextCategoryName.setText(category.name)
            if (category.type == TransactionType.INCOME) {
                binding.radioCategoryIncome.isChecked = true
            } else {
                binding.radioCategoryExpense.isChecked = true
            }
            // Опционально: можно запретить изменение типа категории при редактировании
            // binding.radioCategoryIncome.isEnabled = false
            // binding.radioCategoryExpense.isEnabled = false
        }
    }


    override fun onResume() {
        super.onResume()
        val window = dialog?.window
        window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }


    private fun setupListeners() {
        binding.buttonSaveCategory.setOnClickListener {
            saveOrUpdateCategory() // Используем новый метод
        }
        binding.buttonCancelCategory.setOnClickListener {
            dismiss()
        }
    }

    private fun saveOrUpdateCategory() {
        val categoryName = binding.editTextCategoryName.text.toString().trim()

        // 1. Валидация имени
        if (categoryName.isEmpty()) {
            binding.inputLayoutCategoryName.error = "Введите название категории"
            return
        } else {
            binding.inputLayoutCategoryName.error = null
        }

        // 2. Определяем тип
        val selectedTypeId = binding.radioGroupCategoryType.checkedRadioButtonId
        if (selectedTypeId == -1) {
            Toast.makeText(requireContext(), "Выберите тип категории", Toast.LENGTH_SHORT).show()
            return
        }
        val categoryType = if (selectedTypeId == R.id.radio_category_income) {
            TransactionType.INCOME
        } else {
            TransactionType.EXPENSE
        }

        // 3. Проверка на дубликат (с учетом редактирования)
        val existingCategories = SharedPreferencesManager.loadCategories()
        val isDuplicate = existingCategories.any {
            // Проверяем имя (без учета регистра) и тип,
            // Исключаем проверку с самой редактируемой категорией
            it.name.equals(categoryName, ignoreCase = true) && it.type == categoryType && it.id != categoryIdToEdit
        }
        if (isDuplicate) {
            binding.inputLayoutCategoryName.error = "Другая категория с таким именем и типом уже существует"
            return
        } else {
            binding.inputLayoutCategoryName.error = null
        }

        // 4. Создаем или обновляем объект Category
        val categoryData = Category(
            id = categoryIdToEdit ?: categoryToEdit?.id ?: "", // Используем существующий ID или генерируется новый в конструкторе если не edit mode
            name = categoryName,
            type = categoryType
        )

        // 5. Сохраняем или обновляем
        try {
            val resultBundle = bundleOf(RESULT_KEY_SUCCESS to true) // Готовим результат

            if (isEditMode) {
                SharedPreferencesManager.updateCategory(categoryData) // Нужен метод updateCategory!
                resultBundle.putBoolean(RESULT_KEY_IS_EDIT, true) // Помечаем, что это результат редактирования
                Log.d("AddCategoryFragment", "Category updated: ${categoryData.id}")
            } else {
                SharedPreferencesManager.addCategory(categoryData)
                resultBundle.putBoolean(RESULT_KEY_IS_EDIT, false) // Результат добавления
                Log.d("AddCategoryFragment", "Category added: ${categoryData.id}")
            }

            // Отправляем результат
            setFragmentResult(REQUEST_KEY, resultBundle)
            dismiss()

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Ошибка сохранения: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("AddCategoryFragment", "Error saving/updating category", e)
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}