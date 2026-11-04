package com.example.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import com.example.data.models.Recipient

object SmsHelper {

    sealed class SmsResult {
        object Success : SmsResult()
        object PermissionRequired : SmsResult()
        data class Error(val message: String) : SmsResult()
    }

    fun hasPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Directly dispatches an SMS message silently in the background using Android's SmsManager.
     */
    fun sendDirectSms(
        context: Context,
        recipient: Recipient,
        messageText: String
    ): SmsResult {
        if (!hasPermission(context)) {
            return SmsResult.PermissionRequired
        }

        val phone = recipient.cleanFullNumber
        if (phone.length < 7) {
            return SmsResult.Error("Invalid phone number for ${recipient.name}")
        }

        if (messageText.isBlank()) {
            return SmsResult.Error("Cannot send empty message")
        }

        return try {
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            if (messageText.length > 160) {
                val parts = smsManager.divideMessage(messageText)
                smsManager.sendMultipartTextMessage(phone, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(phone, null, messageText, null, null)
            }
            SmsResult.Success
        } catch (e: Exception) {
            SmsResult.Error(e.localizedMessage ?: "Failed to send SMS")
        }
    }
}
