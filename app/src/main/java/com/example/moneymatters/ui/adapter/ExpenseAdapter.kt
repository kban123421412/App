package com.example.moneymatters.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymatters.data.model.ExpenseModel
import com.example.moneymatters.databinding.ExpenseItemBinding
import android.view.LayoutInflater


class ExpenseAdapter(private val onDeleteClick: (ExpenseModel) -> Unit): RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    private var expenses = listOf<ExpenseModel>()

    inner class ExpenseViewHolder(val binding: ExpenseItemBinding): RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ExpenseViewHolder {
        val binding = ExpenseItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ExpenseViewHolder,
        position: Int
    ) {
        val expense = expenses[position]
        holder.binding.tvTitle.text = expense.title
        holder.binding.tvAmount.text = expense.amount.toString()
        holder.binding.tvCategory.text = expense.category
        holder.binding.tvDate.text = expense.date

        holder.itemView.setOnClickListener {
            onDeleteClick(expense)
            true
        }
    }

    override fun getItemCount() = expenses.size

    fun setExpenses(list: List<ExpenseModel>){
        expenses = list
        notifyDataSetChanged()
    }
}