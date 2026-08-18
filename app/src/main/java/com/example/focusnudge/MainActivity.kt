package com.example.focusnudge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class BlockedApp(val appName: String, val packageName: String, var isBlocked: Boolean)

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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreenContent()
                }
            }
        }
    }
}

@Composable
fun MainScreenContent() {
    var quote by remember { mutableStateOf(CoolSayings.getRandomQuote()) }
    val apps = remember {
        mutableStateListOf(
            BlockedApp("Instagram", "com.instagram.android", true),
            BlockedApp("YouTube", "com.google.android.youtube", true)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "FocusNudge Dashboard",
            style = MaterialTheme.typography.headlineMedium
        )

        // Motivation Quote Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "⚡ Live Nudge Quote", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "\"$quote\"", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { quote = CoolSayings.getRandomQuote() }) {
                    Text("New Motivation")
                }
            }
        }

        Text(
            text = "App Blocklist",
            style = MaterialTheme.typography.titleLarge
        )

        // App List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        Text(text = app.appName)
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
}
