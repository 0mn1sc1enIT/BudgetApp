package com.example.budgetapp.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.budgetapp.SharedPreferencesManager
import com.example.budgetapp.databinding.FragmentDataManagementBinding

class DataManagementFragment : Fragment() {

    private var _binding: FragmentDataManagementBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDataManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Устанавливаем заголовок в Toolbar родительской Activity
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Управление данными"

        binding.buttonClearAllData.setOnClickListener {
            showConfirmationDialog()
        }
    }

    private fun showConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Подтверждение")
            .setMessage("Вы уверены, что хотите удалить ВСЕ транзакции и категории? Это действие нельзя будет отменить.")
            .setIcon(android.R.drawable.ic_dialog_alert) // Иконка предупреждения
            .setPositiveButton("Да, удалить") { _, _ ->
                clearData() // Вызываем метод очистки
            }
            .setNegativeButton("Отмена", null) // Просто закрываем диалог
            .show()
    }

    private fun clearData() {
        try {
            SharedPreferencesManager.clearAllData()
            Toast.makeText(requireContext(), "Все данные успешно удалены", Toast.LENGTH_LONG).show()
            // Возвращаемся на предыдущий экран (SettingsMainFragment)
            parentFragmentManager.popBackStack()
            // Можно добавить перезапуск приложения или обновление данных во всех экранах,
            // но пока ограничимся возвратом.
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Ошибка при очистке данных: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Восстанавливаем заголовок "Настройки" при выходе из этого фрагмента
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Настройки"
        _binding = null
    }
}