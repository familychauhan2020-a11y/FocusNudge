package com.example.focusnudge

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("FocusNudgePrefs", Context.MODE_PRIVATE)

    fun saveUserProfile(role: String, goal: String, intervalMinutes: Int = 30) {
        prefs.edit().apply {
            putString("role", role)
            putString("goal", goal)
            putInt("interval", intervalMinutes)
            apply()
        }
    }

    fun getUserRole(): String = prefs.getString("role", "Student") ?: "Student"
    fun getUserGoal(): String = prefs.getString("goal", "Stay on task") ?: "Stay on task"
    fun getTimerIntervalMillis(): Long = (prefs.getInt("interval", 30) * 60 * 1000).toLong()
}

object PromptGenerator {
    fun generateNudgeMessage(context: Context): String {
        val prefs = AppPreferences(context)
        val role = prefs.getUserRole()
        val goal = prefs.getUserGoal()

        return when (role) {
            "Student" -> "You came here to study, not watch brainrot. Back to your books!"
            "Professional" -> "Is this scroll session moving your professional goals forward?"
            "Educator" -> "Did you find the lesson reference you were looking for?"
            else -> "Role: $role | Goal: \"$goal\"\n\nAre you still on task, or are you doomscrolling?"
        }
    }
}

class AppDetectorService : AccessibilityService() {

    private var currentApp: String = ""
    private var isTimerRunning = false
    private var handler = Handler(Looper.getMainLooper())
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    private val targetPackages = listOf(
        "com.google.android.youtube",
        "com.instagram.android",
        "com.zhiliaoapp.musically",
        "com.ss.android.ugc.trill"
    )

    private val timerRunnable = Runnable { triggerOverlayIntervention() }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            if (packageName != currentApp) {
                currentApp = packageName
                checkAndManageTimer(packageName)
            }
        }
    }

    private fun checkAndManageTimer(packageName: String) {
        if (targetPackages.contains(packageName)) {
            if (!isTimerRunning) startSessionTimer()
        } else {
            stopSessionTimer()
        }
    }

    private fun startSessionTimer() {
        isTimerRunning = true
        val interval = AppPreferences(this).getTimerIntervalMillis()
        handler.postDelayed(timerRunnable, interval)
    }

    private fun stopSessionTimer() {
        isTimerRunning = false
        handler.removeCallbacks(timerRunnable)
    }

    private fun triggerOverlayIntervention() {
        if (!Settings.canDrawOverlays(this)) return

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
            PixelFormat.TRANSLUCENT
        )

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setBackgroundColor(0xFF111111.toInt())
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }

        val messageText = TextView(this).apply {
            text = PromptGenerator.generateNudgeMessage(this@AppDetectorService)
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 20f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 32)
        }

        val mathChallengeText = TextView(this).apply {
            text = "Solve to unlock: 12 + 15 = ?"
            setTextColor(0xFFFFC107.toInt())
            textSize = 16f
            setPadding(0, 0, 0, 16)
        }

        val answerInput = EditText(this).apply {
            hint = "Enter answer"
            setHintTextColor(0x88FFFFFF.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        val dismissButton = Button(this).apply {
            text = "Wait 5s..."
            isEnabled = false
        }

        layout.addView(messageText)
        layout.addView(mathChallengeText)
        layout.addView(answerInput)
        layout.addView(dismissButton)

        overlayView = layout
        windowManager?.addView(overlayView, params)

        object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                dismissButton.text = "Wait ${millisUntilFinished / 1000}s..."
            }
            override fun onFinish() {
                dismissButton.text = "Submit & Return"
                dismissButton.isEnabled = true
            }
        }.start()

        dismissButton.setOnClickListener {
            val answer = answerInput.text.toString().trim()
            if (answer == "27") {
                removeOverlay()
                stopSessionTimer()
            } else {
                Toast.makeText(this@AppDetectorService, "Incorrect math answer!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun removeOverlay() {
        if (overlayView != null && windowManager != null) {
            windowManager?.removeView(overlayView)
            overlayView = null
        }
    }

    override fun onInterrupt() {}
}

class MainActivity : ComponentActivity() {

    private lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appPreferences = AppPreferences(this)

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
        }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainAppDashboard(
                        appPreferences = appPreferences,
                        onSave = { selectedRole, customGoal, interval ->
                            appPreferences.saveUserProfile(selectedRole, customGoal, interval)
                            Toast.makeText(this, "Profile Saved & Active!", Toast.LENGTH_SHORT).show()
                        },
                        onOpenAccessibility = {
                            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MainAppDashboard(
    appPreferences: AppPreferences,
    onSave: (String, String, Int) -> Unit,
    onOpenAccessibility: () -> Unit
) {
    var selectedRole by remember { mutableStateOf(appPreferences.getUserRole()) }
    var customRoleInput by remember { mutableStateOf("") }
    var customGoalInput by remember { mutableStateOf(appPreferences.getUserGoal()) }
    var timerMinutes by remember { mutableStateOf("30") }
    var showError by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "FocusNudge Dashboard", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onOpenAccessibility, modifier = Modifier.fillMaxWidth()) {
            Text("Enable Accessibility Service")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Choose Your Role", style = MaterialTheme.typography.titleMedium)

        val roles = listOf("Student", "Professional", "Educator", "Other")
        roles.forEach { role ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = (selectedRole == role), onClick = { selectedRole = role })
                Text(text = role, modifier = Modifier.padding(start = 8.dp))
            }
        }

        if (selectedRole == "Other") {
            OutlinedTextField(value = customRoleInput, onValueChange = { customRoleInput = it }, label = { Text("Custom Role") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = customGoalInput, onValueChange = { customGoalInput = it }, label = { Text("What are you checking?") }, modifier = Modifier.fillMaxWidth())
        }

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = timerMinutes, onValueChange = { timerMinutes = it }, label = { Text("Interval (Minutes)") }, modifier = Modifier.fillMaxWidth())

        if (showError) {
            Text("Please complete all inputs.", color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val interval = timerMinutes.toIntOrNull() ?: 30
                if (selectedRole == "Other") {
                    if (customRoleInput.isBlank() || customGoalInput.isBlank()) {
                        showError = true
                        return@Button
                    }
                    onSave(customRoleInput.trim(), customGoalInput.trim(), interval)
                } else {
                    onSave(selectedRole, "Stay focused on primary tasks", interval)
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Save & Activate")
        }
    }
}
