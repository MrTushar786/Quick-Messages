package com.example.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class WhatsAppAutoSendService : AccessibilityService() {

    companion object {
        @Volatile
        var isServiceConnected: Boolean = false
            private set

        @Volatile
        var autoSendArmedTimestamp: Long = 0L

        /**
         * Arms the auto-send service for the next 8 seconds so it clicks Send
         * as soon as WhatsApp opens (for both text messages and photos/videos).
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
        // Only trigger if armed within the last 8 seconds
        if (elapsed > 8000) {
            return
        }

        val rootNode = rootInActiveWindow ?: return
        try {
            if (tryClickSendButton(rootNode)) {
                // Successfully triggered Send! Disarm and keep WhatsApp open
                autoSendArmedTimestamp = 0L
            }
        } finally {
            rootNode.recycle()
        }
    }

    private fun tryClickSendButton(root: AccessibilityNodeInfo): Boolean {
        // Strategy 1: Look for WhatsApp's official send button resource IDs (chat & media preview)
        val candidateIds = listOf(
            "com.whatsapp:id/send",
            "com.whatsapp:id/send_btn",
            "com.whatsapp.w4b:id/send",
            "com.whatsapp.w4b:id/send_btn"
        )
        for (id in candidateIds) {
            val nodes = root.findAccessibilityNodeInfosByViewId(id)
            if (!nodes.isNullOrEmpty()) {
                for (node in nodes) {
                    if (clickNodeOrParent(node)) {
                        return true
                    }
                }
            }
        }

        // Strategy 2: Search by content description (Send / Bhejein)
        return searchAndClickByContentDescription(root)
    }

    private fun clickNodeOrParent(node: AccessibilityNodeInfo?): Boolean {
        var current = node
        var depth = 0
        while (current != null && depth < 4) {
            if (current.isClickable && current.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                return true
            }
            current = current.parent
            depth++
        }
        return false
    }

    private fun searchAndClickByContentDescription(node: AccessibilityNodeInfo): Boolean {
        val desc = node.contentDescription?.toString()
        if (desc != null && (desc.equals("Send", ignoreCase = true) || desc.equals("Bhejein", ignoreCase = true))) {
            if (clickNodeOrParent(node)) {
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
    }
}
