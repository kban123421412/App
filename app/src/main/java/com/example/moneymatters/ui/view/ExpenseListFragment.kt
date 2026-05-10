package com.example.moneymatters.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneymatters.data.model.ExpenseModel
import com.example.moneymatters.databinding.DialogAddExpenseBinding
import com.example.moneymatters.databinding.FragmentExpenseListBinding
import com.example.moneymatters.ui.adapter.ExpenseAdapter
import com.example.moneymatters.ui.viewModel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpenseListFragment : Fragment() {

    //ViewBindings for new fragments instead of activities
    private var _binding: FragmentExpenseListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ExpenseViewModel
    private lateinit var adapter: ExpenseAdapter
    private val categoryList = mutableListOf("Food", "Transport", "Entertainment", "Rent", "Shopping", "Other")

    //onCreateView is called when the fragment is first created
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentExpenseListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ExpenseAdapter{expense->
            viewModel.deleteExpense(expense)
        }

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        //grab view model from main activity
        viewModel = ViewModelProvider(requireActivity()).get(ExpenseViewModel::class.java)

        //observes changes to budget list
        viewModel.allExpenses.observe(viewLifecycleOwner){
            adapter.setExpenses(it)
        }

        viewModel.totalAmount.observe(viewLifecycleOwner) { total ->
            val displayTotal = total ?: 0.0
            binding.tvTotal.text = String.format("Total: £%.2f", displayTotal)
        }

        binding.fabAdd.setOnClickListener{
            showAddDialog()
        }
    }

    //Dialog function from main activity (before use of fragments)
    private fun showAddDialog() {
        val dialogBinding = DialogAddExpenseBinding.inflate(layoutInflater)
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, categoryList)
        dialogBinding.etCategoryDropdown.setAdapter(categoryAdapter)

        dialogBinding.etCategoryDropdown.setOnItemClickListener { _, _, position, _ ->
            dialogBinding.tilCustomCategory.visibility = if (categoryList[position] == "Other") View.VISIBLE else View.GONE
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Add Expense")
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { _, _ ->
                val title = dialogBinding.etTitle.text.toString()
                val amountText = dialogBinding.etAmount.text.toString()
                val amount = amountText.toDoubleOrNull()
                val selectedCategory = dialogBinding.etCategoryDropdown.text.toString()

                val finalCategory = if (selectedCategory == "Other") dialogBinding.etCustomCategory.text.toString() else selectedCategory

                if (title.isNotEmpty() && amount != null && finalCategory.isNotEmpty()) {
                    val date = SimpleDateFormat("dd MM yyyy", Locale.getDefault()).format(Date())
                    val expense = ExpenseModel(title = title, amount = amount, category = finalCategory, date = date)
                    viewModel.insertExpense(expense)
                } else {
                    Toast.makeText(requireContext(), "Invalid data", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    //close binding
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

