package com.example.budgetapp.ui.overview


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.budgetapp.databinding.FragmentOverviewBinding // Импортируем ViewBinding для фрагмента

class OverviewFragment : Fragment() {

    // Переменная для ViewBinding. Используем _binding, чтобы сделать ее nullable
    // и binding для безопасного доступа после onCreateView.
    private var _binding: FragmentOverviewBinding? = null
    // Это свойство валидно только между onCreateView и onDestroyView.
    private val binding get() = _binding!! // Оператор !! гарантирует, что binding не null в этот период

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Инициализируем ViewBinding
        _binding = FragmentOverviewBinding.inflate(inflater, container, false)
        val view = binding.root // Получаем корневой View из биндинга
        return view // Возвращаем View для отображения
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Здесь можно настроить View, например, добавить слушатели или обновить текст
        // binding.textOverviewTitle.text = "Динамический Заголовок Обзора"
    }

    // Крайне важно очищать ссылку на binding в onDestroyView,
    // чтобы избежать утечек памяти, когда фрагмент уничтожается, но его View еще может жить.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}