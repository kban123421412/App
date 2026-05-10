package com.example.moneymatters.repository

import androidx.lifecycle.LiveData
import com.example.moneymatters.data.dao.CategoryTotal
import com.example.moneymatters.data.dao.ExpenseDao
import com.example.moneymatters.data.model.ExpenseModel

class ExpenseRepository(private val dao:ExpenseDao) {

    val allExpenses: LiveData<List<ExpenseModel>> = dao.getAllExpenses()
    val totalAmount: LiveData<Double> = dao.getTotalAmount()

    val categoryTotals: LiveData<List<CategoryTotal>> = dao.getCategoryTotal()


    suspend fun insertExpense(expense: ExpenseModel) {
        dao.insertExpense(expense)

    }

    suspend fun deleteExpense(expense: ExpenseModel) {
        dao.deleteExpense(expense)


    }
}