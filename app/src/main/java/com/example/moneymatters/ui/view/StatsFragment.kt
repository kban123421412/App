package com.example.moneymatters.ui.view

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.moneymatters.databinding.FragmentStatsBinding
import com.example.moneymatters.ui.viewModel.ExpenseViewModel
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

// We don't need the default boilerplate, just a clean Fragment class
class StatsFragment : Fragment() {

    // Setup ViewBinding. Make sure your layout is named fragment_stats.xml
    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ExpenseViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Grab the ViewModel that belongs to the MainActivity
        viewModel = ViewModelProvider(requireActivity()).get(ExpenseViewModel::class.java)

        // 1. Savings Goal Progress Bar Logic
        viewModel.totalAmount.observe(viewLifecycleOwner) { totalSpent ->
            val total = totalSpent ?: 0.0
            val budget = 5000 // Coursework example: £5000 car goal
            binding.progressBarGoal.progress = total.toInt()
            binding.tvProgressLabel.text = String.format("£%.2f / £%d", total, budget)
        }

        // 2. 3rd Party Library: MPAndroidChart Logic (Mandatory to comment for coursework)
        viewModel.categoryTotals.observe(viewLifecycleOwner) { totals ->

            // Map our database totals into 'PieEntries' for the chart
            val entries = totals.map { PieEntry(it.total.toFloat(), it.category) }
            val dataSet = PieDataSet(entries, "Spending")

            // Set distinct colors for the categories
            dataSet.colors = mutableListOf(Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.GREEN, Color.LTGRAY)

            binding.spendingDonutChart.apply {
                data = PieData(dataSet)
                isDrawHoleEnabled = true // This turns the standard PieChart into a Donut Chart!
                centerText = "Expenses"
                description.isEnabled = false // Hides the default description text
                invalidate() // Tells the chart to refresh and draw itself
            }
        }
    }

    // Best practice to prevent memory leaks in Fragments
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}