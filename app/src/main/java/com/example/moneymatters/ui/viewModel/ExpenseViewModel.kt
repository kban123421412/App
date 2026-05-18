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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class RecurringTemplate(val id: String, val title: String, val amount: Double, val category: String)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository

    val allExpenses: LiveData<List<ExpenseModel>>
    val totalAmount: LiveData<Double>
    val categoryTotals: LiveData<List<CategoryTotal>>
    val allGoals: LiveData<List<GoalModel>>

    //saves preferences
    private val prefs = application.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)

    var isNotificationsEnabled by mutableStateOf(prefs.getBoolean("notifications", true))
    var isDarkMode by mutableStateOf(prefs.getBoolean("dark_mode", false))
    var currencySymbol by mutableStateOf(prefs.getString("currency", "£") ?: "£")

    //state list for recurring templates
    var recurringTemplates by mutableStateOf<List<RecurringTemplate>>(emptyList())

    init {
        val db = ExpenseDataBase.getDatabase(application)
        repository = ExpenseRepository(db.expenseDao(), db.goalDao())
        allExpenses = repository.allExpenses
        totalAmount = repository.totalAmount
        categoryTotals = repository.categoryTotals
        allGoals = repository.allGoals

        loadTemplates() //loads saved template on startup
    }

    //reccuring expenses
    private fun loadTemplates() {
        val savedSet = prefs.getStringSet("recurring_templates", emptySet()) ?: emptySet()
        recurringTemplates = savedSet.mapNotNull {
            val parts = it.split("|")

            //builds the template from the saved text string
            if (parts.size == 4) RecurringTemplate(parts[0], parts[1], parts[2].toDoubleOrNull() ?: 0.0, parts[3]) else null
        }
    }

    fun addRecurringExpenses(title: String, amount: Double, category: String) {
        val newTemplate = RecurringTemplate(UUID.randomUUID().toString(), title, amount, category)
        val updatedList = recurringTemplates + newTemplate
        recurringTemplates = updatedList
        saveTemplates(updatedList)
    }

    fun deleteRecurringExpense(template: RecurringTemplate) {
        val updatedList = recurringTemplates.filter { it.id != template.id }
        recurringTemplates = updatedList
        saveTemplates(updatedList)
    }

    private fun saveTemplates(list: List<RecurringTemplate>) {
        val stringSet = list.map { "${it.id}|${it.title}|${it.amount}|${it.category}" }.toSet()
        prefs.edit().putStringSet("recurring_templates", stringSet).apply()
    }

    //prefernces functions
    fun toggleNotifications(enabled: Boolean) {
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
    fun insertExpense(expense: ExpenseModel) = viewModelScope.launch(Dispatchers.IO){ repository.insertExpense(expense) }
    fun deleteExpense(expense: ExpenseModel) = viewModelScope.launch(Dispatchers.IO){ repository.deleteExpense(expense) }

    //Goal Functions
    fun insertGoal(goal: GoalModel) = viewModelScope.launch(Dispatchers.IO){ repository.insertGoal(goal) }
    fun updateGoal(goal: GoalModel) = viewModelScope.launch(Dispatchers.IO){ repository.updateGoal(goal) }

    // Automation function to instantaneously inject recurring subscriptions into the DB
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