package com.example.moneymatters.ui.view

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.moneymatters.data.model.GoalModel
import com.example.moneymatters.ui.viewModel.ExpenseViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import androidx.compose.ui.res.stringResource
import com.example.moneymatters.R

@Composable
fun StatsScreen(viewModel: ExpenseViewModel) {

    //observes the totals and saving goals
    val categoryTotals by viewModel.categoryTotals.observeAsState(emptyList())
    val goals by viewModel.allGoals.observeAsState(emptyList())

    var showAddGoalDialog by remember { mutableStateOf(false) }
    var selectedGoalForFunds by remember { mutableStateOf<GoalModel?>(null) }

    //Scaffold for screen structure and buttons
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddGoalDialog = true }) { //opens add goal dialog
                Icon(Icons.Filled.Add, contentDescription = "Add Goal")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            //title for donut chart
           // Text("Spending by Category", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Text(
                stringResource(id = R.string.spending_category),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            // 3rd party chart -> donut chart but within jetpack compose
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(250.dp)
                    .weight(1f)
                    .padding(vertical = 16.dp),

                //creates the donut chart and sets the title, removes the default description and enables the legend
                factory = { context ->
                    PieChart(context).apply {
                        isDrawHoleEnabled = true
                        centerText = context.getString(R.string.expenses_chart_center)
                        description.isEnabled = false
                        legend.isEnabled = true
                    }
                },

                //updates chart if categories change
                update = { chart ->
                    if (categoryTotals.isNotEmpty()) {
                        val entries = categoryTotals.map { PieEntry(it.total.toFloat(), it.category) }
                        val dataSet = PieDataSet(entries, "").apply {

                            colors = listOf(AndroidColor.CYAN,
                                            AndroidColor.MAGENTA,
                                            AndroidColor.YELLOW,
                                            AndroidColor.GREEN,
                                            AndroidColor.LTGRAY
                                        )

                            valueTextSize = 14f
                        }
                        chart.data = PieData(dataSet)
                        chart.invalidate() //refreshes chart
                    }
                }
            )

            //Text("My Savings Goals", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

            Text(
                stringResource(id = R.string.savings_goals),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // LazyColumn for Goals
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(goals) { goal ->
                    GoalItemCard(goal = goal, onClick = { selectedGoalForFunds = goal })
                }
            }
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
                viewModel.updateGoal(goal)
                selectedGoalForFunds = null
            }
        )
    }
}

@Composable
fun GoalItemCard(goal: GoalModel, onClick: () -> Unit) {

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
            LinearProgressIndicator(
                progress = { (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) }, //prevents exceeding or underflowing goal (stays between 1 and 0)
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = String.format("£%.2f / £%.2f", goal.currentAmount, goal.targetAmount),
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
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount to Add (£)") })
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