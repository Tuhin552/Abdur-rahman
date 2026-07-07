package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.DashboardScreen
import com.example.ui.GoalsScreen
import com.example.ui.LogBmiScreen
import com.example.ui.LoginScreen
import com.example.ui.MainViewModel
import com.example.ui.RegisterScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val isDark = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppMainContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppMainContent(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    var authScreen by remember { mutableStateOf("login") }

    if (currentUser == null) {
        // Authentication screens flow
        if (authScreen == "login") {
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = { authScreen = "register" },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            RegisterScreen(
                viewModel = viewModel,
                onNavigateToLogin = { authScreen = "login" },
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        // Primary authenticated application dashboard
        val activeTab by viewModel.activeTab.collectAsState()

        Scaffold(
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = activeTab == "dashboard",
                        onClick = { viewModel.selectTab("dashboard") },
                        icon = {
                            Icon(
                                imageVector = if (activeTab == "dashboard") Icons.Default.Dashboard else Icons.Outlined.Dashboard,
                                contentDescription = "Dashboard"
                            )
                        },
                        label = { Text("Dashboard") },
                        modifier = Modifier.testTag("nav_tab_dashboard")
                    )

                    NavigationBarItem(
                        selected = activeTab == "calculator",
                        onClick = { viewModel.selectTab("calculator") },
                        icon = {
                            Icon(
                                imageVector = if (activeTab == "calculator") Icons.Default.Calculate else Icons.Outlined.Calculate,
                                contentDescription = "Calculator"
                            )
                        },
                        label = { Text("Calculator") },
                        modifier = Modifier.testTag("nav_tab_calculator")
                    )

                    NavigationBarItem(
                        selected = activeTab == "goals",
                        onClick = { viewModel.selectTab("goals") },
                        icon = {
                            Icon(
                                imageVector = if (activeTab == "goals") Icons.Default.Flag else Icons.Outlined.Flag,
                                contentDescription = "Goals"
                            )
                        },
                        label = { Text("Goals") },
                        modifier = Modifier.testTag("nav_tab_goals")
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (activeTab) {
                    "dashboard" -> DashboardScreen(viewModel = viewModel)
                    "calculator" -> LogBmiScreen(viewModel = viewModel)
                    "goals" -> GoalsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
