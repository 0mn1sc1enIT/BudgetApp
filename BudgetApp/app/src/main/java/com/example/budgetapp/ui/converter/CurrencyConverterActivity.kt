package com.example.budgetapp.ui.converter

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import androidx.appcompat.widget.SearchView
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.budgetapp.databinding.ActivityCurrencyConverterBinding
import com.example.budgetapp.databinding.ItemConvertedCurrencyBinding
import java.text.NumberFormat
import java.util.Locale

class CurrencyConverterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCurrencyConverterBinding
    private val viewModel: CurrencyConverterViewModel by viewModels()

    // Используем тот же список валют
    private val currencies = listOf("KZT", "USD", "EUR", "RUB")
    // Форматтер для отображения результатов
    private val resultFormatter: NumberFormat = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }

    // Храним последнй список результатов для фильтрации
    private var currentResults: List<ConversionResult>? = null
    // Храним текущий поисковый запрос
    private var currentSearchQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        binding = ActivityCurrencyConverterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarConverter)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Конвертер валют"

        setupSpinners()
        setupInputListener()
        setupSearchView()
        observeViewModel()

        // Загрузка курсов
        val apiKey = "2746adc816577f250a2d2572"
        if (apiKey != null) {
            // Загружаем только если курсы еще не загружены (ViewModel переживает смену конфигурации)
            if (viewModel.conversionRates.value == null) {
                viewModel.fetchRates(apiKey)
            }
        } else {
            showError("API ключ не найден")
        }

        val appBarLayout = binding.appBarLayoutConverter

        ViewCompat.setOnApplyWindowInsetsListener(appBarLayout) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Применяем верхний отступ как padding top для AppBarLayout
            // Это сдвинет Toolbar вниз, но фон AppBarLayout останется под статус баром
            view.updatePadding(top = insets.top)

            // Возвращаем исходные инсеты, чтобы другие view тоже могли их обработать
            windowInsets
        }
    }

    // ... getApiKeyFromSomewhereSafe() ...

    private fun setupSpinners() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerFromCurrency.adapter = adapter
        // УБИРАЕМ настройку второго спиннера

        binding.spinnerFromCurrency.setSelection(currencies.indexOf("KZT")) // KZT по умолчанию

        binding.spinnerFromCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                triggerConversionCalculation() // Пересчитываем при смене валюты
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupInputListener() {
        binding.editTextAmountToConvert.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                triggerConversionCalculation() // Пересчитываем при изменении суммы
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupSearchView() {
        binding.searchViewCurrency.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            // Вызывается при нажатии Enter или кнопки поиска
            override fun onQueryTextSubmit(query: String?): Boolean {
                currentSearchQuery = query.orEmpty().trim()
                updateResultsUI(currentResults) // Обновляем UI с фильтром
                // Можно скрыть клавиатуру
                binding.searchViewCurrency.clearFocus()
                return true // Мы обработали запрос
            }

            // Вызывается при каждом изменении текста
            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText.orEmpty().trim()
                updateResultsUI(currentResults) // Обновляем UI с фильтром
                return true // Мы обработали изменение
            }
        })
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBarConverter.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.conversionResults.observe(this) { results ->
            currentResults = results // Сохраняем полный список результатов
            updateResultsUI(results) // Отображаем (возможно, уже отфильтрованный)
        }

        viewModel.conversionRates.observe(this) { rates ->
            if (rates != null) {
                triggerConversionCalculation()
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                showError(it)
                updateResultsUI(null) // Очищаем UI
                viewModel.clearError()
            }
        }
    }

    private fun triggerConversionCalculation() {
        val amountStr = binding.editTextAmountToConvert.text.toString()
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        val fromCurrency = binding.spinnerFromCurrency.selectedItem as? String

        if (fromCurrency != null) {
            viewModel.calculateConversions(amount, fromCurrency)
        } else {
            updateResultsUI(null)
        }
    }

    // Метод теперь принимает полный список и фильтрует его перед отображением
    private fun updateResultsUI(fullResults: List<ConversionResult>?) {
        binding.resultsContainer.removeAllViews()

        if (fullResults == null) {
            return
        }

        // Фильтруем список на основе currentSearchQuery
        val filteredResults = if (currentSearchQuery.isEmpty()) {
            fullResults // Если поиск пуст, показываем все
        } else {
            fullResults.filter {
                // Ищем совпадение в коде валюты (без учета регистра)
                it.currencyCode.contains(currentSearchQuery, ignoreCase = true)
            }
        }

        if (filteredResults.isEmpty() && binding.editTextAmountToConvert.text.toString().toDoubleOrNull() != 0.0) {
            // Сообщение, если ничего не найдено (или если список был пуст изначально)
            if(currentSearchQuery.isNotEmpty() && fullResults.isNotEmpty()){
                // Показываем, что не найдено по запросу
                // Можно добавить отдельный TextView для этого
                Toast.makeText(this, "Валюта '$currentSearchQuery' не найдена", Toast.LENGTH_SHORT).show()
            } else if (fullResults.isEmpty() && currentSearchQuery.isEmpty()){
                // Список изначально пуст
                Toast.makeText(this, "Не удалось получить курсы", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val inflater = LayoutInflater.from(this)
        filteredResults.forEach { result ->
            val itemBinding = ItemConvertedCurrencyBinding.inflate(inflater, binding.resultsContainer, false)
            itemBinding.textConvertedAmount.text = resultFormatter.format(result.amount)
            itemBinding.textCurrencyCode.text = result.currencyCode
            binding.resultsContainer.addView(itemBinding.root)
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, "Ошибка: $message", Toast.LENGTH_LONG).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}