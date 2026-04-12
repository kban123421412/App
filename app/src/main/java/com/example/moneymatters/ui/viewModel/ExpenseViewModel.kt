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


class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository

    val allExpenses: LiveData<List<ExpenseModel>>
    val totalAmount: LiveData<Double>


    init {
        val dao = ExpenseDataBase.getDatabase(application).expenseDao()
        repository = ExpenseRepository(dao)
        allExpenses = repository.allExpenses
        totalAmount = repository.totalAmount

    }
    fun insertExpense(expense: ExpenseModel) = viewModelScope.launch(Dispatchers.IO){
        repository.insertExpense(expense)

    }

    fun deleteExpense(expense: ExpenseModel) = viewModelScope.launch(Dispatchers.IO){
        repository.deleteExpense(expense)

    }
}

