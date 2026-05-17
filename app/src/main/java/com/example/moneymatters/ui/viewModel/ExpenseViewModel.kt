package com.example.moneymatters.ui.viewModel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository

    val allExpenses: LiveData<List<ExpenseModel>>
    val totalAmount: LiveData<Double>
    val categoryTotals: LiveData<List<CategoryTotal>>

    val allGoals: LiveData<List<GoalModel>>

    //State trackers for the settings panel
    private val prefs = application.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
    var isNotificationsEnabled by mutableStateOf(prefs.getBoolean("notifications", true))
    var isDarkMode by mutableStateOf(prefs.getBoolean("dark_mode", false))
    var currencySymbol by mutableStateOf(prefs.getString("currency","£")?:"£")

    init {
        val db = ExpenseDataBase.getDatabase(application)
        repository = ExpenseRepository(db.expenseDao(), db.goalDao())
        allExpenses = repository.allExpenses
        totalAmount = repository.totalAmount
        categoryTotals = repository.categoryTotals
        allGoals = repository.allGoals
    }

    //preference functions
    fun toggleNotification(enabled: Boolean) {
        isNotificationsEnabled = enabled
        prefs.edit().putBoolean("notifications", enabled).apply()
    }

    fun toggleDarkMode(enabled: Boolean) {
        isDarkMode = enabled
        prefs.edit().putBoolean("dark_mode", enabled).apply()
    }

    fun updateCurrency(symbol: String) {
        currencySymbol = symbol
        prefs.edit().putString("currency", symbol).apply()
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

    //Adds automatic expense to db
    fun logAutomaticExpense(title: String, amount: Double, category: String) = viewModelScope.launch(Dispatchers.IO) {
        val currentDate = SimpleDateFormat("dd MM yyyy", Locale.getDefault()).format(Date())
        val automaticExpense = ExpenseModel(
            title = title,
            amount = amount,
            category = category,
            date = currentDate
        )
        repository.insertExpense(automaticExpense)
    }
}