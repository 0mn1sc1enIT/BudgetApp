package com.example.budgetapp.ui.charts

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.budgetapp.R
import com.example.budgetapp.databinding.FragmentBalanceTrendBinding // Используем соответствующий binding
import com.example.budgetapp.model.Transaction
import com.example.budgetapp.model.TransactionType
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TreeMap

class BalanceTrendFragment : Fragment() {

    private var _binding: FragmentBalanceTrendBinding? = null
    private val binding get() = _binding!!

    private val chartsViewModel: ChartsViewModel by activityViewModels()

    // Форматтер для оси X (месяцы)
    private val monthFormatter = SimpleDateFormat("MMM", Locale("ru")) // "янв", "фев" ...

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBalanceTrendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChart()

        chartsViewModel.selectedPeriod.observe(viewLifecycleOwner) { updateChart() }
        chartsViewModel.allTransactions.observe(viewLifecycleOwner) { updateChart() }
        // Нам не нужны категории для этого графика
    }

    private fun setupChart() {
        binding.lineChartBalance.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true) // Разрешить масштабирование
            setPinchZoom(true)    // Масштабирование двумя пальцами
            setDrawGridBackground(false)

            // Ось X (время)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f // Минимальный интервал - 1 (для наших индексов)
            // Форматтер для оси X установим при наличии данных

            // Левая ось Y (суммы)
            axisLeft.setDrawGridLines(true) // Можно включить сетку
            axisLeft.axisMinimum = 0f // Минимальное значение (или можно дать авто)

            // Правая ось Y (отключена)
            axisRight.isEnabled = false

            // Легенда
            legend.isEnabled = true // Включим для показа "Доходы", "Расходы", "Баланс"
            legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
            legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
            legend.orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
            legend.setDrawInside(false)

            animateX(1000) // Анимация по оси X
        }
    }

    private fun updateChart() {
        val period = chartsViewModel.selectedPeriod.value ?: return
        val allTransactions = chartsViewModel.allTransactions.value ?: return

        Log.d("BalanceTrend", "Updating chart for period: $period")

        val filteredTransactions = ChartsViewModel.filterTransactionsByPeriod(allTransactions, period)

        if (filteredTransactions.isEmpty()) {
            binding.lineChartBalance.visibility = View.GONE
            binding.textNoTrendData.visibility = View.VISIBLE
            binding.lineChartBalance.data = null // Очищаем данные
            binding.lineChartBalance.invalidate()
            Log.d("BalanceTrend", "No transaction data for period $period.")
            return
        }

        // Агрегируем данные по месяцам (или дням для месячного периода)
        // Будем использовать TreeMap для сортировки по ключу (индексу месяца/дня)
        val monthlyData = aggregateDataByMonth(filteredTransactions)

        if (monthlyData.isEmpty()) {
            binding.lineChartBalance.visibility = View.GONE
            binding.textNoTrendData.visibility = View.VISIBLE
            binding.lineChartBalance.data = null
            binding.lineChartBalance.invalidate()
            Log.d("BalanceTrend", "Aggregation resulted in empty data for period $period.")
            return
        }


        // Подготавливаем данные для LineChart
        val incomeEntries = ArrayList<Entry>()
        val expenseEntries = ArrayList<Entry>()
        val balanceEntries = ArrayList<Entry>()
        val monthLabels = ArrayList<String>() // Метки для оси X

        monthlyData.forEach { (monthIndex, values) ->
            val indexFloat = monthIndex.toFloat() // Индекс месяца/дня как позиция на оси X
            incomeEntries.add(Entry(indexFloat, values.income.toFloat()))
            expenseEntries.add(Entry(indexFloat, values.expense.toFloat()))
            balanceEntries.add(Entry(indexFloat, (values.income - values.expense).toFloat()))
            monthLabels.add(values.label) // Добавляем метку месяца/дня
        }

        // Создаем DataSet'ы
        val incomeDataSet = LineDataSet(incomeEntries, "Доходы").apply {
            color = ContextCompat.getColor(requireContext(), R.color.income_color)
            valueTextColor = color
            setCircleColor(color)
            circleRadius = 3f
            lineWidth = 2f
            setDrawValues(false) // Не рисовать значения на точках
        }
        val expenseDataSet = LineDataSet(expenseEntries, "Расходы").apply {
            color = ContextCompat.getColor(requireContext(), R.color.expense_color)
            valueTextColor = color
            setCircleColor(color)
            circleRadius = 3f
            lineWidth = 2f
            setDrawValues(false)
        }
        val balanceDataSet = LineDataSet(balanceEntries, "Баланс").apply {
            color = Color.GRAY
            valueTextColor = color
            setCircleColor(color)
            circleRadius = 3f
            lineWidth = 2f
            enableDashedLine(10f, 5f, 0f) // Пунктирная линия для баланса
            setDrawValues(false)
        }


        // Объединяем в LineData
        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(incomeDataSet)
        dataSets.add(expenseDataSet)
        dataSets.add(balanceDataSet)
        val lineData = LineData(dataSets)

        // Настраиваем форматтер оси X
        binding.lineChartBalance.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                // Используем метки, которые мы собрали
                return if (index >= 0 && index < monthLabels.size) monthLabels[index] else ""
            }
        }
        // Устанавливаем количество меток на оси X (опционально, можно оставить авто)
        // binding.lineChartBalance.xAxis.labelCount = monthLabels.size

        // Устанавливаем данные и обновляем график
        binding.lineChartBalance.data = lineData
        binding.lineChartBalance.invalidate() // Перерисовать

        binding.lineChartBalance.visibility = View.VISIBLE
        binding.textNoTrendData.visibility = View.GONE
    }


    // Структура для хранения агрегированных данных за период (месяц/день)
    data class MonthlyAggregatedData(
        val label: String, // Метка для оси X (название месяца/дня)
        var income: Double = 0.0,
        var expense: Double = 0.0
    )

    // Функция агрегации (упрощенная - по месяцам)
    private fun aggregateDataByMonth(transactions: List<Transaction>): Map<Int, MonthlyAggregatedData> {
        // TreeMap для автоматической сортировки по ключу (индексу месяца)
        val aggregatedData = TreeMap<Int, MonthlyAggregatedData>()
        val calendar = Calendar.getInstance()

        for (transaction in transactions) {
            calendar.time = transaction.date
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) // 0 - январь, 11 - декабрь
            // Создаем уникальный ключ для месяца: Год * 100 + Месяц
            // Это позволит правильно сортировать данные за разные годы
            val monthKey = year * 100 + month

            // Получаем или создаем запись для этого месяца
            val data = aggregatedData.getOrPut(monthKey) {
                // Создаем метку формата "Янв 24", "Фев 24" и т.д.
                val shortYear = SimpleDateFormat("yy", Locale.getDefault()).format(calendar.time)
                MonthlyAggregatedData(label = "${monthFormatter.format(calendar.time)} $shortYear")
            }

            // Добавляем сумму к доходу или расходу
            if (transaction.type == TransactionType.INCOME) {
                data.income += transaction.amount
            } else {
                data.expense += transaction.amount
            }
        }
        // Преобразуем ключи вида YYYYMM в простые индексы 0, 1, 2... для оси X
        return aggregatedData.values.mapIndexed { index, data -> index to data }.toMap()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}