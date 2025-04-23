package com.example.budgetapp.ui.categories

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.budgetapp.R
import com.example.budgetapp.SharedPreferencesManager
import com.example.budgetapp.databinding.DialogAddCategoryBinding // Используем binding для диалога
import com.example.budgetapp.model.Category
import com.example.budgetapp.model.TransactionType

class AddCategoryFragment : DialogFragment() {

    private var _binding: DialogAddCategoryBinding? = null
    private val binding get() = _binding!!

    // Создаем View программно, так как DialogFragment не всегда использует onCreateView для стандартных диалогов
    // Но для кастомного макета удобнее переопределить onCreateDialog или onCreateView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogAddCategoryBinding.inflate(inflater, container, false)
        // Устанавливаем заголовок диалога
        dialog?.setTitle("Новая категория")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        // Устанавливаем ширину диалога
        val window = dialog?.window
        window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(), // Ширина ~90% экрана
            WindowManager.LayoutParams.WRAP_CONTENT // Высота по контенту
        )
        // Можно также настроить гравитацию, если нужно
        // window?.setGravity(Gravity.CENTER)
    }


    private fun setupListeners() {
        binding.buttonSaveCategory.setOnClickListener {
            saveCategory()
        }
        binding.buttonCancelCategory.setOnClickListener {
            dismiss() // Закрываем диалог
        }
    }

    private fun saveCategory() {
        val categoryName = binding.editTextCategoryName.text.toString().trim()

        // 1. Валидация имени
        if (categoryName.isEmpty()) {
            binding.inputLayoutCategoryName.error = "Введите название категории"
            return
        } else {
            binding.inputLayoutCategoryName.error = null // Убираем ошибку
        }

        // 2. Определяем тип
        val selectedTypeId = binding.radioGroupCategoryType.checkedRadioButtonId
        if (selectedTypeId == -1) { // Ничего не выбрано (хотя у нас есть default)
            Toast.makeText(requireContext(), "Выберите тип категории", Toast.LENGTH_SHORT).show()
            return
        }
        val categoryType = if (selectedTypeId == R.id.radio_category_income) {
            TransactionType.INCOME
        } else {
            TransactionType.EXPENSE
        }

        // 3. Проверка на дубликат (простая проверка по имени и типу)
        val existingCategories = SharedPreferencesManager.loadCategories()
        val isDuplicate = existingCategories.any {
            it.name.equals(categoryName, ignoreCase = true) && it.type == categoryType
        }
        if (isDuplicate) {
            binding.inputLayoutCategoryName.error = "Категория с таким именем и типом уже существует"
            return
        } else {
            binding.inputLayoutCategoryName.error = null
        }


        // 4. Создаем объект Category
        val newCategory = Category(
            name = categoryName,
            type = categoryType
            // ID генерируется автоматически в конструкторе Category
        )

        // 5. Сохраняем
        try {
            SharedPreferencesManager.addCategory(newCategory)
            // Отправляем результат обратно в CategoryListFragment
            sendResult(Activity.RESULT_OK)
            dismiss() // Закрываем диалог после успешного сохранения
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Ошибка сохранения: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    // Метод для отправки результата целевому фрагменту (старый способ)
    private fun sendResult(resultCode: Int) {
        targetFragment?.onActivityResult(targetRequestCode, resultCode, null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Очищаем binding
    }
}