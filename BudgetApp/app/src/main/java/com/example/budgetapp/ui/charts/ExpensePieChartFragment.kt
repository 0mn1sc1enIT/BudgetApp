package com.example.budgetapp.ui.charts

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels // Для доступа к ViewModel родительской Activity/Fragment
import com.example.budgetapp.R
import com.example.budgetapp.SharedPreferencesManager
import com.example.budgetapp.databinding.FragmentExpensePieChartBinding
import com.example.budgetapp.model.Category
import com.example.budgetapp.model.Transaction
import com.example.budgetapp.model.TransactionType
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import java.text.NumberFormat
import java.util.Locale

class ExpensePieChartFragment : Fragment() {

    private var _binding: FragmentExpensePieChartBinding? = null
    private val binding get() = _binding!!

    // Получаем общую ViewModel
    private val chartsViewModel: ChartsViewModel by activityViewModels()

    private fun getFormatter(): NumberFormat = SharedPreferencesManager.getCurrencyFormatter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpensePieChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChart()

        // Наблюдаем за всеми данными, необходимыми для графика
        chartsViewModel.selectedPeriod.observe(viewLifecycleOwner) { updateChart() }
        chartsViewModel.allTransactions.observe(viewLifecycleOwner) { updateChart() }
        chartsViewModel.allCategories.observe(viewLifecycleOwner) { updateChart() }
    }

    private fun setupChart() {
        binding.pieChartExpenses.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            setExtraOffsets(5f, 10f, 5f, 5f)
            dragDecelerationFrictionCoef = 0.95f
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 50f // Чуть меньше
            transparentCircleRadius = 55f
            setDrawCenterText(true)
            centerText = "Расходы"
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            animateY(1200, Easing.EaseInOutQuad) // Чуть быстрее анимация
            legend.isEnabled = true
            legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
            legend.orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
            legend.setDrawInside(false)
            legend.isWordWrapEnabled = true // Перенос слов в легенде
            legend.xEntrySpace = 7f
            legend.yEntrySpace = 0f
            legend.yOffset = 5f
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(10f) // Меньше текст на секторах
        }
    }

    private fun updateChart() {
        val period = chartsViewModel.selectedPeriod.value ?: return
        val allTransactions = chartsViewModel.allTransactions.value ?: return
        val categoriesMap = chartsViewModel.allCategories.value ?: return
        val formatter = getFormatter()
        Log.d("ExpensePieChart", "Updating chart for period: $period")

        val filteredExpenses = ChartsViewModel.filterTransactionsByPeriod(allTransactions, period)
            .filter { it.type == TransactionType.EXPENSE }

        val expensesByCategory = filteredExpenses
            .groupBy { it.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .filter { it.value > 0 }

        if (expensesByCategory.isNotEmpty()) {
            val entries = ArrayList<PieEntry>()
            expensesByCategory.forEach { (categoryId, sum) ->
                val categoryName = categoriesMap[categoryId]?.name ?: "Неизвестно"
                entries.add(PieEntry(sum.toFloat(), categoryName))
            }

            val dataSet = PieDataSet(entries, "")
            dataSet.sliceSpace = 2f
            dataSet.iconsOffset = MPPointF(0f, 30f)
            dataSet.selectionShift = 5f

            // Используем стандартные цвета Material
            dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
            // Или можно добавить больше цветов:
            // val colors = ArrayList(ColorTemplate.MATERIAL_COLORS.toList())
            // colors.addAll(ColorTemplate.VORDIPLOM_COLORS.toList())
            // dataSet.colors = colors

            val data = PieData(dataSet)
            data.setValueFormatter(PercentFormatter(binding.pieChartExpenses))
            data.setValueTextSize(11f)
            data.setValueTextColor(Color.BLACK)

            binding.pieChartExpenses.data = data
            binding.pieChartExpenses.highlightValues(null)
            binding.pieChartExpenses.invalidate()

            binding.pieChartExpenses.visibility = View.VISIBLE
            binding.textNoExpenseData.visibility = View.GONE

            // Используем актуальный форматтер для текста в центре
            val totalAmount = expensesByCategory.values.sum() // или incomeByCategory
            binding.pieChartExpenses.centerText = "Расходы\n${formatter.format(totalAmount)}" // или "Доходы..." для Income фрагмента

        } else {
            binding.pieChartExpenses.visibility = View.GONE
            binding.textNoExpenseData.visibility = View.VISIBLE
            binding.pieChartExpenses.data = null
            binding.pieChartExpenses.invalidate()
            Log.d("ExpensePieChart", "No expense data for period $period.")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}