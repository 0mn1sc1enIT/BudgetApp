package com.example.budgetapp.ui.charts

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.budgetapp.SharedPreferencesManager
import com.example.budgetapp.databinding.FragmentIncomePieChartBinding
import com.example.budgetapp.model.TransactionType
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import java.text.NumberFormat

class IncomePieChartFragment : Fragment() {

    private var _binding: FragmentIncomePieChartBinding? = null
    private val binding get() = _binding!!

    private val chartsViewModel: ChartsViewModel by activityViewModels()
    private fun getFormatter(): NumberFormat = SharedPreferencesManager.getCurrencyFormatter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIncomePieChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChart()

        chartsViewModel.selectedPeriod.observe(viewLifecycleOwner) { updateChart() }
        chartsViewModel.allTransactions.observe(viewLifecycleOwner) { updateChart() }
        chartsViewModel.allCategories.observe(viewLifecycleOwner) { updateChart() }
    }

    private fun setupChart() {
        // Настройки в основном такие же, как у расходов, можно кастомизировать цвета/текст
        binding.pieChartIncome.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            setExtraOffsets(5f, 10f, 5f, 5f)
            dragDecelerationFrictionCoef = 0.95f
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 50f
            transparentCircleRadius = 55f
            setDrawCenterText(true)
            centerText = "Доходы" // Изменяем текст
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            animateY(1200, Easing.EaseInOutQuad)
            legend.isEnabled = true
            legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
            legend.orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
            legend.setDrawInside(false)
            legend.isWordWrapEnabled = true
            legend.xEntrySpace = 7f
            legend.yEntrySpace = 0f
            legend.yOffset = 5f
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(10f)
        }
    }

    private fun updateChart() {
        val period = chartsViewModel.selectedPeriod.value ?: return
        val allTransactions = chartsViewModel.allTransactions.value ?: return
        val categoriesMap = chartsViewModel.allCategories.value ?: return
        val formatter = getFormatter()
        Log.d("IncomePieChart", "Updating chart for period: $period")

        // Фильтруем ДОХОДЫ
        val filteredIncome = ChartsViewModel.filterTransactionsByPeriod(allTransactions, period)
            .filter { it.type == TransactionType.INCOME }

        val incomeByCategory = filteredIncome
            .groupBy { it.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .filter { it.value > 0 }

        if (incomeByCategory.isNotEmpty()) {
            val entries = ArrayList<PieEntry>()
            incomeByCategory.forEach { (categoryId, sum) ->
                val categoryName = categoriesMap[categoryId]?.name ?: "Неизвестно"
                entries.add(PieEntry(sum.toFloat(), categoryName))
            }

            val dataSet = PieDataSet(entries, "")
            dataSet.sliceSpace = 2f
            dataSet.iconsOffset = MPPointF(0f, 30f)
            dataSet.selectionShift = 5f

            // Используем другие цвета для доходов, например JOYFUL
            dataSet.colors = ColorTemplate.JOYFUL_COLORS.toList()

            val data = PieData(dataSet)
            data.setValueFormatter(PercentFormatter(binding.pieChartIncome))
            data.setValueTextSize(11f)
            data.setValueTextColor(Color.BLACK)

            binding.pieChartIncome.data = data
            binding.pieChartIncome.highlightValues(null)
            binding.pieChartIncome.invalidate()

            binding.pieChartIncome.visibility = View.VISIBLE
            binding.textNoIncomeData.visibility = View.GONE

            val totalAmount = incomeByCategory.values.sum() // или incomeByCategory
            binding.pieChartIncome.centerText = "Расходы\n${formatter.format(totalAmount)}" // или "Доходы..." для Income фрагмента

        } else {
            binding.pieChartIncome.visibility = View.GONE
            binding.textNoIncomeData.visibility = View.VISIBLE
            binding.pieChartIncome.data = null
            binding.pieChartIncome.invalidate()
            Log.d("IncomePieChart", "No income data for period $period.")
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}