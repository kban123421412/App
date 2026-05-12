package com.example.moneymatters.ui.view

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.moneymatters.ui.viewModel.ExpenseViewModel
import com.example.moneymatters.ui.viewModel.ExpenseViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainScreenApp()
            }
        }
    }
}

@Composable
fun MainScreenApp() {
    val navController = rememberNavController()

    //Creates viewmodel so it can be shared
    val context = LocalContext.current
    val expenseViewModel: ExpenseViewModel = viewModel(
        factory = ExpenseViewModelFactory(context.applicationContext as Application)
    )

    // Scaffold for bottom bar
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        // new navhost
        NavHost(
            navController = navController,
            startDestination = "expense_list",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("expense_list") {
                ExpenseListScreen(viewModel = expenseViewModel)
            }
            composable("stats_screen") {
                StatsScreen(viewModel = expenseViewModel)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

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
    }
}