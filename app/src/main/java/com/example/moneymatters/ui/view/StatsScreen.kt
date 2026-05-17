package com.example.moneymatters.ui.view

import android.graphics.Color as AndroidColor
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneymatters.R
import com.example.moneymatters.data.model.GoalModel
import com.example.moneymatters.data.model.ExpenseModel
import com.example.moneymatters.ui.viewModel.ExpenseViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.example.moneymatters.util.NotificationHelper.showGoalCompletedNotification
import com.example.moneymatters.util.ReceiptExporter.shareReceipt

// Tracks which time filter chip is currently selected
enum class TimeFilter { ALL, WEEK, MONTH, YEAR }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: ExpenseViewModel) {

    //observes the totals and saving goals
    val categoryTotals by viewModel.categoryTotals.observeAsState(emptyList())
    val goals by viewModel.allGoals.observeAsState(emptyList())

    // Observes all expenses to calculate the date filters locally
    val allExpenses by viewModel.allExpenses.observeAsState(emptyList())
    val context = androidx.compose.ui.platform.LocalContext.current //context so we can send notification

    var showAddGoalDialog by remember { mutableStateOf(false) }
    var selectedGoalForFunds by remember { mutableStateOf<GoalModel?>(null) }
    var selectedFilter by remember { mutableStateOf(TimeFilter.ALL) }

    //calculates the dates and filters the list automatically
    val filteredExpenses by remember(allExpenses, selectedFilter) {
        derivedStateOf {
            val format = SimpleDateFormat("dd MM yyyy", Locale.getDefault())
            val now = Calendar.getInstance().timeInMillis

            allExpenses.filter { expense ->
                if (selectedFilter == TimeFilter.ALL) return@filter true
                try {
                    val expenseDate = format.parse(expense.date)?.time ?: 0L
                    val daysDiff = (now - expenseDate) / (1000 * 60 * 60 * 24)
                    when (selectedFilter) {
                        TimeFilter.WEEK -> daysDiff <= 7
                        TimeFilter.MONTH -> daysDiff <= 30
                        TimeFilter.YEAR -> daysDiff <= 365
                        else -> true
                    }
                } catch (e: Exception) { true }
            }.sortedByDescending {
                try { format.parse(it.date)?.time ?: 0L } catch(e: Exception) { 0L }
            }
        }
    }

    //groups the filtered data on donut chart segments
    val pieEntries by remember(filteredExpenses) {
        derivedStateOf {
            filteredExpenses.groupBy { it.category }
                .map { PieEntry(it.value.sumOf { exp -> exp.amount }.toFloat(), it.key) }
        }
    }

    //scaffold for screen structure and buttons
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddGoalDialog = true }) { //opens add goal dialog
                Icon(Icons.Filled.Add, contentDescription = "Add Goal")
            }
        }
    ) { paddingValues ->

        //lazy column for the list of expenses
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                //title for donut chart
                // Text("Spending by Category", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                Text(
                    stringResource(id = R.string.spending_category),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                // Time Filter Selection Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FilterChip(selected = selectedFilter == TimeFilter.ALL, onClick = { selectedFilter = TimeFilter.ALL }, label = { Text("All") })
                    FilterChip(selected = selectedFilter == TimeFilter.WEEK, onClick = { selectedFilter = TimeFilter.WEEK }, label = { Text("Week") })
                    FilterChip(selected = selectedFilter == TimeFilter.MONTH, onClick = { selectedFilter = TimeFilter.MONTH }, label = { Text("Month") })
                    FilterChip(selected = selectedFilter == TimeFilter.YEAR, onClick = { selectedFilter = TimeFilter.YEAR }, label = { Text("Year") })
                }

                //3rd party chart -> donut chart but within jetpack compose
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        //.heightIn(250.dp)
                        .height(250.dp)
                        //.weight(1f) <-- attempted to cofigure for tablet, but this gives 0 pixels of space to charts
                        .padding(vertical = 16.dp),

                    //creates the donut chart and sets the title, removes the default description and enables the legend
                    factory = { factoryContext ->
                        PieChart(factoryContext).apply {
                            isDrawHoleEnabled = true
                            centerText = factoryContext.getString(R.string.expenses_chart_center)
                            description.isEnabled = false
                            legend.isEnabled = true
                        }
                    },

                    //updates chart if categories change
                    update = { chart ->
                        if (pieEntries.isNotEmpty()) {
                            val dataSet = PieDataSet(pieEntries, "").apply {
                                colors = listOf(
                                    AndroidColor.CYAN,
                                    AndroidColor.MAGENTA,
                                    AndroidColor.YELLOW,
                                    AndroidColor.GREEN,
                                    AndroidColor.LTGRAY
                                )
                                valueTextSize = 14f
                            }
                            chart.data = PieData(dataSet)
                            chart.notifyDataSetChanged()
                            chart.invalidate() //refreshes chart
                        } else {

                            //if db is empty clear chart
                            chart.clear()
                            chart.invalidate()
                        }
                    }
                )
            }

            //listof expenses based on filter
            if (filteredExpenses.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filtered Expenses",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                        )

                        IconButton(
                            onClick = {
                                shareReceipt(
                                    context = context,
                                    filterName = selectedFilter.name.lowercase().replaceFirstChar { it.uppercase() },
                                    expenses = filteredExpenses
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Share Receipt Image",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                items(filteredExpenses) { expense ->
                    SmallExpenseItem(expense = expense, currencySymbol = viewModel.currencySymbol)
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

                //Old hardcoded texts
                //Text("My Savings Goals", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                Text(
                    stringResource(id = R.string.savings_goals),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(goals) { goal ->
                GoalItemCard(
                    goal = goal,
                    onClick = { selectedGoalForFunds = goal },
                    currencySymbol = viewModel.currencySymbol
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp)) //removed .weight() from lazy colum so
            }                                                     //it takes the left over space correctly
        }
    }

    //displays add goal dialog
    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onSave = { goal ->
                viewModel.insertGoal(goal)
                showAddGoalDialog = false
            }
        )
    }

    //displays add funds dialog for specific goal
    selectedGoalForFunds?.let { goal ->
        AddFundsDialog(
            goal = goal,
            onDismiss = { selectedGoalForFunds = null },
            onSave = { amountToAdd ->
                //goal.currentAmount += amountToAdd <-- changed this as goals would not update after you add a fund to it
                val updatedGoal = goal.copy(currentAmount = goal.currentAmount + amountToAdd)
                viewModel.updateGoal(updatedGoal)

                //checks if goal is completed
                if(updatedGoal.currentAmount >= updatedGoal.targetAmount && goal.currentAmount < goal.targetAmount){
                    // NEW: Check if notifications are enabled before showing the popup!
                    if (viewModel.isNotificationsEnabled) {
                        showGoalCompletedNotification(context, updatedGoal.title)
                    }
                }
                selectedGoalForFunds = null
            }
        )
    }
}

//custom layout for filtered expenses below donut chart
@Composable
fun SmallExpenseItem(expense: ExpenseModel, currencySymbol: String) {
    val icon = when (expense.category) {
        "Food" -> Icons.Filled.Fastfood
        "Transport" -> Icons.Filled.DirectionsCar
        "Entertainment" -> Icons.Filled.Movie
        "Rent" -> Icons.Filled.Home
        "Shopping" -> Icons.Filled.ShoppingCart
        else -> Icons.Filled.AttachMoney
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = expense.category,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = expense.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = expense.date, fontSize = 12.sp, color = Color.Gray)
        }

        Text(text = String.format("${currencySymbol}%.2f", expense.amount), fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun GoalItemCard(goal: GoalModel, onClick: () -> Unit, currencySymbol: String) {

    //card that displays goal information
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }, //makes card tappable
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = goal.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))

            val safeProgress = if (goal.targetAmount > 0){
                (goal.currentAmount / goal.targetAmount).toFloat()
            }else{
                0f
            }
            LinearProgressIndicator(
                progress = { safeProgress.coerceIn(0f,1f) }, //prevents exceeding or underflowing goal (stays between 1 and 0)

                //created a divide by 0 causing stats screen to crash
                //progress = { (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) },

                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = String.format("${currencySymbol}%.2f / ${currencySymbol}%.2f", goal.currentAmount, goal.targetAmount),
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }
    }
}

@Composable
fun AddGoalDialog(onDismiss: () -> Unit, onSave: (GoalModel) -> Unit) {
    var title by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Goal") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Goal Title") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = target, onValueChange = { target = it }, label = { Text("Target Amount (£)") })
            }
        },
        confirmButton = {
            Button(onClick = {
                val parsedTarget = target.toDoubleOrNull()

                //only creates goal on valid input
                if (title.isNotBlank() && parsedTarget != null) {
                    onSave(GoalModel(title = title, targetAmount = parsedTarget))
                }
            }) { Text("Create") }
        },

        //closes without saving
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddFundsDialog(goal: GoalModel, onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Funds to ${goal.title}") },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount to Add (£)") })
        },
        confirmButton = {
            Button(onClick = {
                val parsedAmount = amount.toDoubleOrNull()
                if (parsedAmount != null && parsedAmount > 0) {
                    onSave(parsedAmount)
                }
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
  }
