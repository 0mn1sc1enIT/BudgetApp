package com.example.budgetapp.ui.charts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // Делегат для ViewModel
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.budgetapp.R
import com.example.budgetapp.databinding.FragmentChartsBinding
import com.example.budgetapp.model.ChartPeriod
import com.google.android.material.tabs.TabLayoutMediator

class ChartsFragment : Fragment() {

    private var _binding: FragmentChartsBinding? = null
    private val binding get() = _binding!!

    // Получаем ViewModel, привязанный к Activity (чтобы дочерние фрагменты тоже его видели)
    private val chartsViewModel: ChartsViewModel by viewModels({ requireActivity() }) // Привязка к Activity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChartsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPeriodSpinner()
        setupViewPagerAndTabs()
    }

    private fun setupPeriodSpinner() {
        val periods = ChartPeriod.values().map { it.displayName }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, periods) // Используем стандартный layout
        binding.spinnerPeriodSelector.setAdapter(adapter)

        // Установка текущего значения при запуске
        val currentPeriodName = chartsViewModel.selectedPeriod.value?.displayName ?: ChartPeriod.CURRENT_MONTH.displayName
        binding.spinnerPeriodSelector.setText(currentPeriodName, false)

        // Слушатель выбора
        binding.spinnerPeriodSelector.setOnItemClickListener { parent, _, position, _ ->
            val selectedPeriodName = parent.adapter.getItem(position) as String
            ChartPeriod.values().find { it.displayName == selectedPeriodName }?.let {
                chartsViewModel.selectPeriod(it) // Обновляем ViewModel
            }
        }
    }

    private fun setupViewPagerAndTabs() {
        val viewPagerAdapter = ChartsPagerAdapter(this)
        binding.viewPagerCharts.adapter = viewPagerAdapter

        TabLayoutMediator(binding.tabLayoutCharts, binding.viewPagerCharts) { tab, position ->
            tab.text = when (position) {
                0 -> "Расходы"
                1 -> "Доходы"
                2 -> "Динамика"
                else -> null
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Адаптер для ViewPager2
    private inner class ChartsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ExpensePieChartFragment()
                1 -> IncomePieChartFragment()
                2 -> BalanceTrendFragment()
                else -> throw IllegalStateException("Invalid position: $position")
            }
        }
    }
}