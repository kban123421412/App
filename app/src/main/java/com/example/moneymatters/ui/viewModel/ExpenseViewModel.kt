package com.example.moneymatters.ui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.moneymatters.data.database.ExpenseDataBase
import com.example.moneymatters.data.model.ExpenseModel
import com.example.moneymatters.repository.ExpenseRepository
import com.example.moneymatters.data.dao.CategoryTotal
import com.example.moneymatters.data.model.GoalModel
import com.example.moneymatters.data.dao.GoalDao
import com.example.moneymatters.data.dao.ExpenseDao



class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository

    val allExpenses: LiveData<List<ExpenseModel>>
    val totalAmount: LiveData<Double>
    val categoryTotals: LiveData<List<CategoryTotal>>

    val allGoals: LiveData<List<GoalModel>>




    init {
        val dao = ExpenseDataBase.getDatabase(application).expenseDao()
        repository = ExpenseRepository(dao.expenseDao(), dao.goalDao())
        allExpenses = repository.allExpenses
        totalAmount = repository.totalAmount
        categoryTotals = repository.categoryTotals
        allGoals = repository.allGoals


    }
    //Expense Functions
    fun insertExpense(expense: ExpenseModel) = viewModelScope.launch(Dispatchers.IO){
        repository.insertExpense(expense)

    }

    fun deleteExpense(expense: ExpenseModel) = viewModelScope.launch(Dispatchers.IO){
        repository.deleteExpense(expense)

    }

    //Goal Functions
    fun insertGoal(goal: GoalModel) = viewModelScope.launch(Dispatchers.IO){
        repository.insertGoal(goal)
    }
    fun updateGoal(goal: GoalModel) = viewModelScope.launch(Dispatchers.IO){
        repository.updateGoal(goal)
    }
}

