package com.example.moneymatters.ui.view

import android.content.Intent
import android.widget.Toast
import android.widget.Toast.*
import androidx.compose.foundation.ExperimentalFoundationApi //for the holding functionality
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable //also for holding functionality
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import com.example.moneymatters.ui.viewModel.ExpenseViewModel
import com.example.moneymatters.util.DailyReminderWorker
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(viewModel: ExpenseViewModel) {
    val context = LocalContext.current
    var showCurrencyMenu by remember { mutableStateOf(false) }
    var showAddTemplateDialog by remember { mutableStateOf(false) }

    //stacks all UI vertically
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()) //accessibility on smaller screens, prevents cutoff
    ) {
        Text(text = "Settings", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        //Sytem Controls Section
        Text(text = "Preferences", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {

                //Notifications Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Notifications, contentDescription = "Notifications")
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Enable Notifications")
                    }
                    Switch(
                        checked = viewModel.isNotificationsEnabled,
                        onCheckedChange = { isEnabled ->

                            //permanently saves notification preference
                            viewModel.toggleNotifications(isEnabled)

                            //workmanager for daily notifications even if app is closed
                            val workManager = androidx.work.WorkManager.getInstance(context)
                            if (isEnabled) {

                                //daily log reminder
                                val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
                                    24, TimeUnit.HOURS
                                ).build()
                                workManager.enqueueUniquePeriodicWork(
                                    "DailyReminder",
                                    ExistingPeriodicWorkPolicy.KEEP,
                                    dailyWorkRequest
                                )
                            } else {
                                workManager.cancelUniqueWork("DailyReminder")
                            }
                        }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                //dark Theme Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.DarkMode, contentDescription = "Dark Mode")
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Dark Theme Layout")
                    }
                    Switch(
                        checked = viewModel.isDarkMode,

                        //permanently saves the dark mode preference
                        onCheckedChange = { viewModel.toggleDarkMode(it) }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                //currency Swap Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.AttachMoney, contentDescription = "Currency")
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Active Currency Symbol")
                    }

                    //currency dropdown
                    Box {
                        Button(onClick = { showCurrencyMenu = true }) {
                            Text(text = viewModel.currencySymbol)
                            Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = "Open")
                        }

                        DropdownMenu(expanded = showCurrencyMenu, onDismissRequest = { showCurrencyMenu = false }) {

                            //save the currency preference (add more maybe)
                            DropdownMenuItem(text = { Text("Pound (£)") }, onClick = { viewModel.updateCurrency("£"); showCurrencyMenu = false })
                            DropdownMenuItem(text = { Text("Dollar ($)") }, onClick = { viewModel.updateCurrency("$"); showCurrencyMenu = false })
                            DropdownMenuItem(text = { Text("Euro (€)") }, onClick = { viewModel.updateCurrency("€"); showCurrencyMenu = false })
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        //Utility section
        Text(text = "Quick Tools", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {

                //recurring logging
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Recurring Expenses", fontWeight = FontWeight.SemiBold)
                        Text(text = "Tap to log instantly. Hold to delete.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    IconButton(onClick = { showAddTemplateDialog = true }) {
                        Icon(imageVector = Icons.Filled.AddCircle, contentDescription = "Add Expense", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                //checks if there are any reccuring logs and displays them
                if (viewModel.recurringTemplates.isEmpty()) {
                    Text("No recurring expenses saved.", modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray, fontSize = 14.sp)
                } else {

                    //loops through the reccurring logs
                    viewModel.recurringTemplates.forEach { template ->
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()

                                //detects normal taps and holding taps using Expermimental Foundation Api
                                .combinedClickable(
                                    onClick = {

                                        //pushed to db
                                        viewModel.logAutomaticExpense(template.title, template.amount, template.category)
                                        makeText(context, "Logged: ${template.title}", LENGTH_SHORT).show()
                                    },
                                    onLongClick = {

                                        //removes it
                                        viewModel.deleteRecurringExpense(template)
                                        makeText(context, "Deleted: ${template.title}",
                                            LENGTH_SHORT
                                        ).show()
                                    }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Filled.Autorenew, contentDescription = "Auto Log", tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = template.title, fontWeight = FontWeight.SemiBold)
                                Text(text = template.category, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                            }
                            Text(text = String.format("${viewModel.currencySymbol}%.2f", template.amount), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                //goes to calc app (calc is slang for calculator)
                Row(
                    modifier = Modifier.fillMaxWidth().clickable {
                        try {

                            //1.tries to open calculator app
                            val launchIntent = Intent.makeMainSelectorActivity(
                                Intent.ACTION_MAIN,
                                Intent.CATEGORY_APP_CALCULATOR
                            ).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(launchIntent)

                        } catch (e: Exception) {
                            try {

                                //2.if 1 doesnt work target samsung calculator (all testing done on samsung phone)
                                val samsungIntent = context.packageManager.getLaunchIntentForPackage("com.sec.android.app.popupcalculator")
                                if (samsungIntent != null) {
                                    context.startActivity(samsungIntent)
                                } else {

                                    //3.if 2 doesnt work opens google calculator
                                    val webIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        android.net.Uri.parse("https://www.google.com/search?q=calculator")
                                    )
                                    context.startActivity(webIntent)
                                }
                            } catch (e2: Exception) {

                                //if 1, 2 and 3 fail displays error
                                makeText(context, "Could not open a calculator.", LENGTH_SHORT).show()
                            }
                        }
                    }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Filled.Calculate, contentDescription = "Calculator Intent")
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Launch Calculator", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    //reccurring log popup
    if (showAddTemplateDialog) {
        AddTemplateDialog(
            currencySymbol = viewModel.currencySymbol,
            onDismiss = { showAddTemplateDialog = false },
            onSave = { title, amount, category ->

                viewModel.addRecurringExpense(title, amount, category)
                showAddTemplateDialog = false
            }
        )
    }
}

//creates the template for the popup
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTemplateDialog(currencySymbol: String, onDismiss: () -> Unit, onSave: (String, Double, String) -> Unit) {

    //stores input
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food") }
    var customCategory by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("Food", "Transport", "Entertainment", "Rent", "Shopping", "Other")

    AlertDialog(
        onDismissRequest = onDismiss, //closes the dialog
        title = { Text("New Recurring Expense") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title (e.g. Netflix)") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount ($currencySymbol)") })
                Spacer(modifier = Modifier.height(8.dp))

                //dropdown logic
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
                            DropdownMenuItem(text = { Text(selection) }, onClick = { category = selection; expanded = false })
                        }
                    }
                }

                //shows custom categroy if selected
                if (category == "Other") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = customCategory, onValueChange = { customCategory = it }, label = { Text("Custom Category") })
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val parsedAmount = amount.toDoubleOrNull()
                val finalCategory = if (category == "Other") customCategory else category

                //checks for valid input
                if (title.isNotBlank() && parsedAmount != null) {
                    onSave(title, parsedAmount, finalCategory)
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}