package com.example.moneymatters.repository

import androidx.lifecycle.LiveData
import com.example.moneymatters.data.dao.CategoryTotal
import com.example.moneymatters.data.dao.ExpenseDao
import com.example.moneymatters.data.model.ExpenseModel
import com.example.moneymatters.data.dao.GoalDao
import com.example.moneymatters.data.model.GoalModel

class ExpenseRepository(private val dao:ExpenseDao, private val goalDao: GoalDao) {

    // --Expense Logic--
    val allExpenses: LiveData<List<ExpenseModel>> = dao.getAllExpenses()
    val totalAmount: LiveData<Double> = dao.getTotalAmount()

    val categoryTotals: LiveData<List<CategoryTotal>> = dao.getCategoryTotal()


    suspend fun insertExpense(expense: ExpenseModel) {
        dao.insertExpense(expense)

    }

    suspend fun deleteExpense(expense: ExpenseModel) {
        dao.deleteExpense(expense)


    }

    // --GOAL LOGIC--

    val allGoals : LiveData<List<GoalModel>> = goalDao.getAllGoals()

    suspend fun insertGoal(goal: GoalModel) {
        goalDao.insertGoal(goal)
    }

    suspend fun updateGoal(goal: GoalModel) {
        goalDao.updateGoal(goal)
    }


}