package com.example.focusnudge

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class NudgeDetectorService : AccessibilityService() {

    private var swipeCount = 0
    private var lastSwipeTime: Long = 0

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val rootNode = rootInActiveWindow ?: return

        // 1. Full-screen educational video active hone par swipe reset karein
        if (isFullScreenPlayer(rootNode)) {
            swipeCount = 0
            return
        }

        // 2. Continuous Reels/Shorts vertical swipes detect karein
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            val currentTime = System.currentTimeMillis()

            // Agar 3 seconds ke andar doosra swipe hua hai
            if (currentTime - lastSwipeTime < 3000) {
                swipeCount++
            } else {
                swipeCount = 1
            }
            lastSwipeTime = currentTime

            // 3. Continuous 5 swipes ke baad FocusNudge overlay trigger karein
            if (swipeCount >= 5) {
                triggerNudgeOverlay()
                swipeCount = 0
            }
        }
    }

    private fun isFullScreenPlayer(node: AccessibilityNodeInfo): Boolean {
        val bounds = Rect()
        node.getBoundsInScreen(bounds)

        val displayMetrics = resources.displayMetrics
        
        // Full screen bounds check (Educational lecture detection)
        return bounds.width() == displayMetrics.widthPixels && 
               bounds.height() == displayMetrics.heightPixels
    }

    private fun triggerNudgeOverlay() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {}
}
