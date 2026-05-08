package com.example.moneymatters.ui.view

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter // Missing Import
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneymatters.data.model.ExpenseModel
import com.example.moneymatters.databinding.ActivityMainBinding
import com.example.moneymatters.databinding.DialogAddExpenseBinding
import com.example.moneymatters.ui.adapter.ExpenseAdapter
import com.example.moneymatters.ui.viewModel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: ExpenseViewModel
    private lateinit var adapter: ExpenseAdapter

    // Category list moved inside the class but outside functions
    private val categoryList = mutableListOf("Food", "Transport", "Entertainment", "Rent", "Shopping", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ExpenseAdapter { expense ->
            viewModel.deleteExpense(expense)
        }

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel = ViewModelProvider(this).get(ExpenseViewModel::class.java)

        viewModel.allExpenses.observe(this) {
            adapter.setExpenses(it)
        }

        viewModel.totalAmount.observe(this) { total ->
            val displayTotal = total ?: 0.0
            binding.tvTotal.text = String.format("Total: £%.2f", displayTotal)
        }

        binding.fabAdd.setOnClickListener {
            showAddDialog()
        }
    }

    private fun showAddDialog() {
        val dialogBinding = DialogAddExpenseBinding.inflate(layoutInflater)

        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categoryList)
        dialogBinding.etCategoryDropdown.setAdapter(categoryAdapter)

        dialogBinding.etCategoryDropdown.setOnItemClickListener { _, _, position, _ ->
            if (categoryList[position] == "Other") {
                dialogBinding.tilCustomCategory.visibility = android.view.View.VISIBLE
            } else {
                dialogBinding.tilCustomCategory.visibility = android.view.View.GONE
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Add Expense")
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { _, _ ->
                // FIX: Move these lines INSIDE the PositiveButton listener
                val title = dialogBinding.etTitle.text.toString()
                val amount = dialogBinding.etAmount.text.toString().toDoubleOrNull()
                val selectedCategory = dialogBinding.etCategoryDropdown.text.toString()

                val finalCategory = if (selectedCategory == "Other") {
                    val customCategory = dialogBinding.etCustomCategory.text.toString()
                    if (customCategory.isNotEmpty() && !categoryList.contains(customCategory)) {
                        // Add to list before "Other" to keep it at the bottom
                        categoryList.add(categoryList.size - 1, customCategory)
                    }
                    customCategory
                } else {
                    selectedCategory
                }

                if (title.isNotEmpty() && amount != null && finalCategory.isNotEmpty()) {
                    val date = SimpleDateFormat("dd MM yyyy", Locale.getDefault()).format(Date())
                    val expense = ExpenseModel(title = title, amount = amount, category = finalCategory, date = date)
                    viewModel.insertExpense(expense)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}