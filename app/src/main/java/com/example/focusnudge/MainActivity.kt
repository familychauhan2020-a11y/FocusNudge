package com.example.focusnudge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

data class BlockedApp(val appName: String, val packageName: String, val isBlocked: Boolean)
data class FocusHabit(val title: String, val currentMinutes: Int, val targetMinutes: Int)

object CoolSayings {
    private val quotes = listOf(
        "Focus on being productive instead of busy.",
        "Your future is created by what you do today, not tomorrow.",
        "Small steps every day lead to big results.",
        "Stay foolish, stay hungry."
    )
    fun getRandomQuote(): String = quotes.random()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainAppStructure()
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Home", Icons.Default.Home)
    object Blocklist : Screen("blocklist", "Blocklist", Icons.Default.Lock)
    object Habits : Screen("habits", "Habits", Icons.Default.CheckCircle)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppStructure() {
    val navController = rememberNavController()
    val screens = listOf(Screen.Dashboard, Screen.Blocklist, Screen.Habits, Screen.Settings)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FocusNudge") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen() }
            composable(Screen.Blocklist.route) { AppBlocklistScreen() }
            composable(Screen.Habits.route) { HabitsScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}

@Composable
fun DashboardScreen() {
    var quote by remember { mutableStateOf(CoolSayings.getRandomQuote()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("⚡ Live Nudge Quote", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("\"$quote\"", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { quote = CoolSayings.getRandomQuote() }) {
                    Text("New Motivation")
                }
            }
        }
    }
}

@Composable
fun AppBlocklistScreen() {
    val apps = remember {
        mutableStateListOf(
            BlockedApp("Instagram", "com.instagram.android", true),
            BlockedApp("YouTube", "com.google.android.youtube", true)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(apps) { app ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(app.appName)
                    Switch(
                        checked = app.isBlocked,
                        onCheckedChange = { isChecked ->
                            val index = apps.indexOf(app)
                            apps[index] = app.copy(isBlocked = isChecked)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HabitsScreen() {
    val habits = remember {
        listOf(
            FocusHabit("Deep Work Session", 45, 120),
            FocusHabit("No Social Media", 180, 180)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(habits) { habit ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(habit.title)
                    Spacer(modifier = Modifier.height(8.dp))
                    val currentProgress = habit.currentMinutes.toFloat() / habit.targetMinutes.toFloat()
                    LinearProgressIndicator(
                        progress = currentProgress,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    var notificationsEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("⚙️ App Settings", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable Notifications")
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )
        }
    }
}
