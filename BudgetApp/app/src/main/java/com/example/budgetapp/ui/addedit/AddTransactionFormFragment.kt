package com.example.budgetapp.ui.addedit

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.budgetapp.R
import com.example.budgetapp.SharedPreferencesManager
import com.example.budgetapp.databinding.FragmentAddTransactionFormBinding
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
        // Устанавливаем текущую дату по умолчанию
        updateDateInView()
    }

    private fun setupDatePicker() {
        // Создаем слушатель для DatePickerDialog
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
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

    // Метод, вызываемый из Activity для сохранения транзакции
    fun saveTransaction() {
        // 1. Получаем данные из полей ввода
        val amountStr = binding.editTextAmount.text.toString()
        val category = binding.editTextCategory.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim()
        val selectedTypeId = binding.radioGroupTransactionType.checkedRadioButtonId

        // 2. Валидация (простая)
        if (amountStr.isBlank() || amountStr == ".") {
            binding.inputLayoutAmount.error = "Введите сумму"
            return
        } else {
            binding.inputLayoutAmount.error = null // Убираем ошибку, если была
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.inputLayoutAmount.error = "Неверная сумма"
            return
        } else {
            binding.inputLayoutAmount.error = null
        }


        if (category.isBlank()) {
            binding.inputLayoutCategory.error = "Введите категорию"
            return
        } else {
            binding.inputLayoutCategory.error = null
        }

        // Определяем тип транзакции
        val transactionType = when (selectedTypeId) {
            R.id.radio_income -> TransactionType.INCOME
            R.id.radio_expense -> TransactionType.EXPENSE
            else -> {
                // Не должно произойти, но на всякий случай
                Toast.makeText(requireContext(), "Выберите тип транзакции", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // 3. Создаем объект Transaction
        val newTransaction = Transaction(
            amount = amount,
            type = transactionType,
            category = category,
            description = description.ifBlank { null }, // Сохраняем null, если описание пустое
            date = calendar.time // Используем выбранную дату
        )

        // 4. Сохраняем через SharedPreferencesManager
        try {
            SharedPreferencesManager.addTransaction(newTransaction)
            Toast.makeText(requireContext(), "Транзакция сохранена", Toast.LENGTH_SHORT).show()

            // Устанавливаем результат для предыдущей Activity (чтобы она обновила список)
            activity?.setResult(AppCompatActivity.RESULT_OK)
            // Закрываем AddTransactionActivity
            activity?.finish()

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Ошибка сохранения: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace() // Выводим ошибку в лог для отладки
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}