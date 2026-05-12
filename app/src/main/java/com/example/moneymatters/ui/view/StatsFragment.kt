package com.example.moneymatters.ui.view

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneymatters.data.model.GoalModel
import com.example.moneymatters.databinding.DialogAddFundsBinding
import com.example.moneymatters.databinding.DialogAddGoalBinding
import com.example.moneymatters.databinding.FragmentStatsBinding
import com.example.moneymatters.ui.adapter.GoalAdapter
import com.example.moneymatters.ui.viewModel.ExpenseViewModel
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ExpenseViewModel
    private lateinit var goalAdapter: GoalAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(ExpenseViewModel::class.java)

        setupDonutChart()
        setupGoalsList()

        binding.fabAddGoal.setOnClickListener {
            showAddGoalDialog()
        }
    }

    private fun setupGoalsList() {
        // Initialize adapter and pass in the click listener for the "Add Funds" popup
        goalAdapter = GoalAdapter { clickedGoal ->
            showAddFundsDialog(clickedGoal)
        }

        binding.rvGoals.adapter = goalAdapter
        binding.rvGoals.layoutManager = LinearLayoutManager(requireContext())

        // Observe goals from the database
        viewModel.allGoals.observe(viewLifecycleOwner) { goals ->
            goalAdapter.setGoals(goals)
        }
    }

    private fun setupDonutChart() {
        viewModel.categoryTotals.observe(viewLifecycleOwner) { totals ->
            val entries = totals.map { PieEntry(it.total.toFloat(), it.category) }
            val dataSet = PieDataSet(entries, "Spending")
            dataSet.colors = mutableListOf(Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.GREEN, Color.LTGRAY)

            binding.spendingDonutChart.apply {
                data = PieData(dataSet)
                isDrawHoleEnabled = true
                centerText = "Expenses"
                description.isEnabled = false
                invalidate()
            }
        }
    }

    // Popup 1: Create a brand new goal
    private fun showAddGoalDialog() {
        val dialogBinding = DialogAddGoalBinding.inflate(layoutInflater)

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Goal")
            .setView(dialogBinding.root)
            .setPositiveButton("Create") { _, _ ->
                val title = dialogBinding.etGoalTitle.text.toString()
                val target = dialogBinding.etTargetAmount.text.toString().toDoubleOrNull()

                if (title.isNotEmpty() && target != null) {
                    val newGoal = GoalModel(title = title, targetAmount = target)
                    viewModel.insertGoal(newGoal)
                } else {
                    Toast.makeText(requireContext(), "Invalid input", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Popup 2: Add funds to an existing goal when tapped
    private fun showAddFundsDialog(goal: GoalModel) {
        val dialogBinding = DialogAddFundsBinding.inflate(layoutInflater)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Funds to ${goal.title}")
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { _, _ ->
                val amountToAdd = dialogBinding.etFundsAmount.text.toString().toDoubleOrNull()

                if (amountToAdd != null && amountToAdd > 0) {
                    // Update the amount and send back to ViewModel
                    goal.currentAmount += amountToAdd
                    viewModel.updateGoal(goal)
                } else {
                    Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}