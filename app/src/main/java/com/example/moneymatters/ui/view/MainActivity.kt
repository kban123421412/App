package com.example.moneymatters.ui.view

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.moneymatters.ui.viewModel.ExpenseViewModel
import com.example.moneymatters.ui.viewModel.ExpenseViewModelFactory
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager.getInstance
import com.example.moneymatters.util.DailyReminderWorker
import com.example.moneymatters.util.NotificationHelper.createNotificationChannel
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //grabs saved preferences and checks if notifications are enabled
        val prefs = getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean("notifications", true)

        //creates the notification channel
        createNotificationChannel(this)

        /* Old code for daily reminder
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            24, TimeUnit.HOURS
        ).build()*/

        // Check our saved settings before launching the background task

        if (notificationsEnabled) {
            val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
                24, TimeUnit.HOURS
            ).build()
            getInstance(this).enqueueUniquePeriodicWork(
                "DailyReminder",
                ExistingPeriodicWorkPolicy.KEEP,
                dailyWorkRequest
            )
        } else {
            getInstance(this).cancelUniqueWork("DailyReminder") //stops task if notifications are off in app settings
        }

        setContent {
            val context = LocalContext.current
            val expenseViewModel: ExpenseViewModel = viewModel(
                factory = ExpenseViewModelFactory(context.applicationContext as Application)
            )

            //swaps the colour scheme to dark or light mode
            val dynamicColorScheme = if (expenseViewModel.isDarkMode) {
                darkColorScheme()
            } else {
                lightColorScheme()
            }

            MaterialTheme(colorScheme = dynamicColorScheme) { //applies the theme to whole app
                MainScreenApp(expenseViewModel)
            }
        }
    }
}

@Composable
fun MainScreenApp(expenseViewModel: ExpenseViewModel) {
    val navController = rememberNavController()

    //ask user for notification permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract  = ActivityResultContracts.RequestPermission(),
    ){isGranted ->}

    //actually user asks for permission (the popup)
    LaunchedEffect(Unit){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    //scaffold for bottom bar
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        //new navhost
        NavHost(
            navController = navController,
            startDestination = "expense_list",
            modifier = Modifier.padding(innerPadding)
        ) {


            //screens
            composable("expense_list") {
                ExpenseListScreen(
                    viewModel = expenseViewModel,
                    expenseViewModel.currencySymbol
                )
            }
            composable("stats_screen") {
                StatsScreen(viewModel = expenseViewModel)
            }
            composable("settings_screen") {
                SettingsScreen(viewModel = expenseViewModel)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        //Expense tab
        NavigationBarItem(
            icon = { Icon(Icons.Filled.List, contentDescription = "List") },
            label = { Text("Expenses") },
            selected = currentRoute == "expense_list",
            onClick = {
                navController.navigate("expense_list") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        )

        //Stats tab
        NavigationBarItem(
            icon = { Icon(Icons.Filled.PieChart, contentDescription = "Stats") },
            label = { Text("Stats") },
            selected = currentRoute == "stats_screen",
            onClick = {
                navController.navigate("stats_screen") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        )

        //Settings tab
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = currentRoute == "settings_screen",
            onClick = {
                navController.navigate("settings_screen") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        )
    }
}