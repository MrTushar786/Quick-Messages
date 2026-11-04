package com.example.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WhatsAppAutoSendService : AccessibilityService() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    companion object {
        @Volatile
        var isServiceConnected: Boolean = false
            private set

        @Volatile
        var autoSendArmedTimestamp: Long = 0L

        /**
         * Arms the auto-send service for the next 6 seconds so it only clicks
         * when triggered intentionally from the Quick Messages app or widget.
         */
        fun armAutoSend() {
            autoSendArmedTimestamp = System.currentTimeMillis()
        }

        /**
         * Checks whether this Accessibility Service is currently enabled in Android Settings.
         */
        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            val expectedServiceName = "${context.packageName}/${WhatsAppAutoSendService::class.java.canonicalName}"
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServices)
            while (colonSplitter.hasNext()) {
                val componentName = colonSplitter.next()
                if (componentName.equals(expectedServiceName, ignoreCase = true) ||
                    componentName.endsWith("/.accessibility.WhatsAppAutoSendService") ||
                    componentName.contains("WhatsAppAutoSendService")
                ) {
                    return true
                }
            }
            return false
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceConnected = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val pkg = event.packageName?.toString() ?: ""
        if (pkg != "com.whatsapp" && pkg != "com.whatsapp.w4b") {
            return
        }

        val elapsed = System.currentTimeMillis() - autoSendArmedTimestamp
        // Only trigger if armed within the last 6 seconds
        if (elapsed > 6000) {
            return
        }

        val rootNode = rootInActiveWindow ?: return
        try {
            if (tryClickSendButton(rootNode)) {
                // Successfully triggered Send! Disarm to prevent duplicate clicks
                autoSendArmedTimestamp = 0L

                // Instant Auto-Close: Automatically dismiss WhatsApp and return to Home Screen
                serviceScope.launch {
                    delay(300) // Brief 300ms pause allowing WhatsApp to process send payload
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    delay(150)
                    performGlobalAction(GLOBAL_ACTION_HOME)
                }
            }
        } finally {
            rootNode.recycle()
        }
    }

    private fun tryClickSendButton(root: AccessibilityNodeInfo): Boolean {
        // Strategy 1: Look for WhatsApp's official send button resource id (both text & media)
        val sendById = root.findAccessibilityNodeInfosByViewId("com.whatsapp:id/send")
        if (sendById != null && sendById.isNotEmpty()) {
            for (node in sendById) {
                if (node.isClickable && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    return true
                }
            }
        }

        // Strategy 2: Look for WhatsApp Business send button id
        val businessSendById = root.findAccessibilityNodeInfosByViewId("com.whatsapp.w4b:id/send")
        if (businessSendById != null && businessSendById.isNotEmpty()) {
            for (node in businessSendById) {
                if (node.isClickable && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    return true
                }
            }
        }

        // Strategy 3: Search by content description (supports international / localized WhatsApp)
        return searchAndClickByContentDescription(root)
    }

    private fun searchAndClickByContentDescription(node: AccessibilityNodeInfo): Boolean {
        val desc = node.contentDescription?.toString()
        if (desc != null && (desc.equals("Send", ignoreCase = true) || desc.equals("Bhejein", ignoreCase = true))) {
            if (node.isClickable && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                return true
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val clicked = searchAndClickByContentDescription(child)
            child.recycle()
            if (clicked) return true
        }
        return false
    }

    override fun onInterrupt() {
        // No-op
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceConnected = false
        serviceJob.cancel()
    }
}
