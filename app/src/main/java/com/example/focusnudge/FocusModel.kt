package com.example.focusnudge

data class BlockedApp(
    val name: String,
    val packageName: String,
    var isBlocked: Boolean = true
)

data class FocusHabit(
    val title: String,
    val targetMinutes: Int,
    var completedMinutes: Int = 0
)

object CoolSayings {
    val reminders = listOf(
        "Bro, put the phone down! Your future self is watching. 👀",
        "Scrolling won't pay the bills. Get back to the grind! 🚀",
        "Distraction detected! Stay locked in, champ. 🔥",
        "Close the app! Your goals are waiting for you. 🎯",
        "Focus now, flex later. Back to work! 💪"
    )

    fun getRandomSaying(): String = reminders.random()
}
