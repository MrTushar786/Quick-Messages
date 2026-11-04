package com.example.widget

import android.content.Context
import androidx.glance.appwidget.updateAll

object WidgetUpdater {

    suspend fun update(context: Context) {
        try {
            QuickMessageWidget().updateAll(context)
        } catch (_: Exception) {
            // Safe fallback if widget is not active
        }
    }
}
