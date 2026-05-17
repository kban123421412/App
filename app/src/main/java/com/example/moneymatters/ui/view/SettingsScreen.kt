package com.example.moneymatters.ui.view

import android.content.Intent
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import com.example.moneymatters.ui.viewModel.ExpenseViewModel
import com.example.moneymatters.util.DailyReminderWorker
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: ExpenseViewModel) {
    val context = LocalContext.current
    var showCurrencyMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()) //accessibility on smaller screens
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

                            //saves setting permanently
                            viewModel.toggleNotifications(isEnabled)

                            val workManager = androidx.work.WorkManager.getInstance(context)
                            if (isEnabled) {
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

                // Dark Theme Row
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
                        // NEW: Save the setting permanently
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

                    Box {
                        Button(onClick = { showCurrencyMenu = true }) {
                            Text(text = viewModel.currencySymbol)
                            Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = "Open")
                        }

                        DropdownMenu(expanded = showCurrencyMenu, onDismissRequest = { showCurrencyMenu = false }) {

                            //save the settings
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

                //Adds a mock monthly Netflix charge straight into Room Database on tap
                Row(
                    modifier = Modifier.fillMaxWidth().clickable {
                        viewModel.logAutomaticExpense("Netflix Subscription", 10.99, "Entertainment")
                    }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Filled.Autorenew, contentDescription = "Auto Log")
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Log Recurring Netflix Charge", fontWeight = FontWeight.SemiBold)
                        Text(text = "Instantly inputs monthly £10.99 payment", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
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
                                android.widget.Toast.makeText(context, "Could not open a calculator.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Filled.Calculate, contentDescription = "Calculator Intent")
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Launch System Calculator", fontWeight = FontWeight.SemiBold)
                        Text(text = "Uses Implicit System Intents to leave app", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}