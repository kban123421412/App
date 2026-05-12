package com.example.moneymatters.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.moneymatters.data.model.GoalModel
import com.example.moneymatters.databinding.ItemGoalBinding
import androidx.recyclerview.widget.RecyclerView

class GoalAdapter (private val onGoalClick: (GoalModel) -> Unit): RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {

    private var goals =  emptyList<GoalModel>()

    // binds goal data to recycler view
    inner class GoalViewHolder(val binding: ItemGoalBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(goal: GoalModel){
            binding.tvGoalTitle.text = goal.title
            binding.pbGoal.max = goal.targetAmount.toInt()
            binding.pbGoal.progress = goal.currentAmount.toInt()
            binding.tvGoalProgress.text = String.format("%.2f / %.2f", goal.currentAmount, goal.targetAmount)

            // Add fund popup when click on any goal
            binding.root.setOnClickListener {
                onGoalClick(goal)
            }
        }
    }

    // Creates layout for each item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val binding = ItemGoalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GoalViewHolder(binding)
    }

    // Updates recycler view
    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        holder.bind(goals[position])
    }

    //gets total number of goals in list
    override fun getItemCount() = goals.size

    // Updates list of goals
    fun setGoals (newGoals: List<GoalModel>){
        goals = newGoals
        notifyDataSetChanged()
    }
}









}