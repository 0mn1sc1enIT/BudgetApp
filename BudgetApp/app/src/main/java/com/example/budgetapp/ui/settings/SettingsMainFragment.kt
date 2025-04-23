package com.example.budgetapp.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.budgetapp.R
import com.example.budgetapp.SharedPreferencesManager
import com.example.budgetapp.databinding.FragmentSettingsMainBinding

class SettingsMainFragment : Fragment() {

    private var _binding: FragmentSettingsMainBinding? = null
    private val binding get() = _binding!!

    private val currencySymbols = listOf("₽", "$", "€", "₸")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCurrencySpinner()
        setupThemeSelector()

        binding.buttonManageData.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_settings, DataManagementFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupCurrencySpinner() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencySymbols)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCurrencySymbol.adapter = adapter

        val savedSymbol = SharedPreferencesManager.getCurrencySymbol()
        val savedPosition = currencySymbols.indexOf(savedSymbol).coerceAtLeast(0)
        binding.spinnerCurrencySymbol.setSelection(savedPosition, false)

        binding.spinnerCurrencySymbol.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedSymbol = currencySymbols[position]
                SharedPreferencesManager.saveCurrencySymbol(selectedSymbol)
                // Обновление форматтера не требуется немедленно, он будет пересоздан при след. использовании
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupThemeSelector() {
        // Получаем текущую сохраненную настройку темы (true = темная, false = светлая)
        val isDarkModeEnabled = SharedPreferencesManager.isDarkModeEnabled() // Новый метод

        // Устанавливаем выбранный RadioButton
        if (isDarkModeEnabled) {
            binding.radioThemeDark.isChecked = true
        } else {
            binding.radioThemeLight.isChecked = true
        }

        // Слушатель изменения выбора в RadioGroup
        binding.radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            val newMode = when (checkedId) {
                R.id.radio_theme_light -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.radio_theme_dark -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_NO // По умолчанию светлая
            }

            // Определяем, включен ли темный режим для сохранения
            val newIsDarkMode = (newMode == AppCompatDelegate.MODE_NIGHT_YES)
            Log.d("Settings", "Theme changed. Is dark mode: $newIsDarkMode")

            // Сохраняем выбор (новый метод)
            SharedPreferencesManager.saveDarkModeEnabled(newIsDarkMode)

            // Применяем тему немедленно
            // Этот вызов может вызвать "мерцание", но нужен для мгновенного эффекта
            AppCompatDelegate.setDefaultNightMode(newMode)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}