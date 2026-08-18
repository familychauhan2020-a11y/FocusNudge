package com.example.focusnudge

import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

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

// 1. Navigation Routes
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Home", Icons.Default.Home)
    object Blocklist : Screen("blocklist", "Blocklist", Icons.Default.Lock)
    object Habits : Screen("habits", "Habits", Icons.Default.CheckCircle)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

// 2. Main Scaffold with Bottom Navigation
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

// -----------------------------------------------------------------------------
// PAGE 1: HOME / DASHBOARD
// -----------------------------------------------------------------------------
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

        Text("Quick Summary", style = MaterialTheme.typography.titleMedium)
        Text("Active focus mode status and quick stats will appear here.")
    }
}

// -----------------------------------------------------------------------------
// PAGE 2: APP BLOCKLIST
// -----------------------------------------------------------------------------
@Composable
fun AppBlocklistScreen() {
    val context = LocalContext.current
    val apps = remember {
        mutableStateListOf(
            BlockedApp("Instagram", "com.instagram.android", true),
            BlockedApp("YouTube", "com.google.android.youtube", true),
            BlockedApp("TikTok", "com.zhiliaoapp.musically", true),
            BlockedApp("X (Twitter)", "com.twitter.android", true)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("🚫 Customizable App Blocklist", style = MaterialTheme.typography.titleMedium)
        }
        items(apps) { app ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(app.appName, style = MaterialTheme.typography.titleMedium)
                        Text(app.packageName, style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = app.isBlocked,
                        onCheckedChange = { isChecked ->
                            val index = apps.indexOf(app)
                            apps[index] = app.copy(isBlocked = isChecked)
                            val status = if (isChecked) "blocked" else "unblocked"
                            Toast.makeText(context, "${app.appName} $status", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// PAGE 3: DAILY FOCUS HABITS
// -----------------------------------------------------------------------------
@Composable
fun HabitsScreen() {
    val habits = remember {
        listOf(
            FocusHabit("Deep Work Session", 45, 120),
            FocusHabit("No Social Media", 180, 180),
            FocusHabit("Reading / Learning", 15, 30)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("🎯 Daily Focus Habits", style = MaterialTheme.typography.titleMedium)
        }
        items(habits) { habit ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(habit.title, style = MaterialTheme.typography.bodyLarge)
                        Text("${habit.currentMinutes} / ${habit.targetMinutes} mins")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = habit.currentMinutes.toFloat() / habit.targetMinutes.toFloat(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// PAGE 4: INTERIOR SETTINGS SCREEN
// -----------------------------------------------------------------------------
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var notificationsEnabled by remember { mutableStateOf(true) }
    var strictModeEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("⚙️ App Settings", style = MaterialTheme.typography.titleLarge)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Nudge Notifications", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Strict Mode (No Unblocking)", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = strictModeEnabled,
                        onCheckedChange = { strictModeEnabled = it }
                    )
                }
            }
        }

        OutlinedButton(
            onClick = { Toast.makeText(context, "Permissions refreshed", Toast.LENGTH_SHORT).show() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Re-check System Permissions")
        }
    }
}
