package com.example.focusnudge

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                FocusNudgeDashboard()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusNudgeDashboard() {
    val context = LocalContext.current
    var currentSaying by remember { mutableStateOf(CoolSayings.getRandomSaying()) }

    // State for App Blocking List
    val appList = remember {
        mutableStateListOf(
            BlockedApp("Instagram", "com.instagram.android"),
            BlockedApp("YouTube", "com.google.android.youtube"),
            BlockedApp("TikTok", "com.zhiliaoapp.musically"),
            BlockedApp("X (Twitter)", "com.twitter.android")
        )
    }

    // State for Habit Tracker
    val habitList = remember {
        mutableStateListOf(
            FocusHabit("Deep Work Session", 120, 45),
            FocusHabit("No Social Media", 180, 180),
            FocusHabit("Reading / Learning", 30, 15)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FocusNudge Control Center") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cool Nudge / Reminder Card
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("⚡ Live Nudge Quote", fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"$currentSaying\"",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { currentSaying = CoolSayings.getRandomSaying() },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("New Motivation")
                        }
                    }
                }
            }

            // Custom App Blocker Section
            item {
                Text("🚫 Customizable App Blocklist", style = MaterialTheme.typography.titleLarge)
            }

            items(appList) { app ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(app.name, style = MaterialTheme.typography.bodyLarge)
                            Text(app.packageName, style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = app.isBlocked,
                            onCheckedChange = { isChecked ->
                                val index = appList.indexOf(app)
                                appList[index] = app.copy(isBlocked = isChecked)
                                val status = if (isChecked) "Blocked" else "Unblocked"
                                Toast.makeText(context, "${app.name} $status", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // Habit Tracker Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("🎯 Daily Focus Habits", style = MaterialTheme.typography.titleLarge)
            }

            items(habitList) { habit ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(habit.title, style = MaterialTheme.typography.bodyLarge)
                            Text("${habit.completedMinutes} / ${habit.targetMinutes} mins")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = (habit.completedMinutes.toFloat() / habit.targetMinutes).coerceIn(0f, 1f),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
