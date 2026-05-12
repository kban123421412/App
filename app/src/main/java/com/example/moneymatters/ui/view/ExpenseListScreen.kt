package com.example.moneymatters.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymatters.data.model.ExpenseModel
import com.example.moneymatters.ui.viewModel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.exp

@Composable
fun ExpenseListScreen(viewModel: ExpenseViewModel) {
    //observes the room db
    val expense by viewModel.allExpenses.observeAsState(emptyList())
    val totalAmount by viewModel.totalAmount.observeAsState(0.0)
    var showDialog by remember { mutableStateOf(false) }

    //scafold to show basic screen structure
    Scaffold(

        //Add expense button
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Expense")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            //Displays total expense
            Text(
                text = String.format("Total: £%.2f", totalAmount),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )


            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {

                //Creates the cards for each expense
                items(expense) {expense ->
                    ExpenseItemCard(expense = expense, onDelete = { viewModel.deleteExpense(it)} )
                }
            }
        }
    }

    //Shows the actual popup when showDialog is true
    if (showDialog){
        AddExpenseDialog(
            onDismiss = { showDialog = false },
            onSave = { expense ->

                //adds new expense to db
                viewModel.insertExpense(expense)
                showDialog = false
                }
            )
        }

    }

@Composable
fun ExpenseItemCard(expense: ExpenseModel, onDelete: (ExpenseModel) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            //Shows expense information (title, date, category, ect)
            Column {
                Text(text = expense.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "${expense.category} • ${expense.date}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = String.format("£%.2f", expense.amount), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                IconButton(onClick = { onDelete(expense) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(onDismiss: () -> Unit, onSave: (ExpenseModel) -> Unit) {

    //stores input
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    var category by remember { mutableStateOf("Food") }
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("Food", "Transport", "Entertainment", "Rent", "Shopping", "Other")


    //popup for adding new expense
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Expense") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (£)") })
                Spacer(modifier = Modifier.height(8.dp))

                // Dropdown Menu
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { selection ->
                            DropdownMenuItem(
                                text = { Text(selection) },
                                onClick = {
                                    category = selection
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },

        //save button
        confirmButton = {
            Button(onClick = {
                val parsedAmount = amount.toDoubleOrNull()
                if (title.isNotBlank() && parsedAmount != null) {
                    val date = SimpleDateFormat("dd MM yyyy", Locale.getDefault()).format(Date())
                    onSave(ExpenseModel(title = title, amount = parsedAmount, category = category, date = date))
                }
            }) { Text("Save") }
        },

        //cancel button
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
