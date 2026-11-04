package com.example.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.models.Recipient

object WhatsAppHelper {

    private const val WHATSAPP_PACKAGE = "com.whatsapp"
    private const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"
    private const val PLAY_STORE_URI = "market://details?id=com.whatsapp"
    private const val PLAY_STORE_WEB_URL = "https://play.google.com/store/apps/details?id=com.whatsapp"

    sealed class LaunchResult {
        object Success : LaunchResult()
        object RedirectedToPlayStore : LaunchResult()
        data class Error(val message: String) : LaunchResult()
    }

    /**
     * Builds the official WhatsApp click-to-chat URI using Uri.Builder.
     * Uri.Builder handles proper URL encoding for spaces, special characters, and unicode text.
     * Example: https://wa.me/918864846795?text=Nikal%20raha
     */
    fun buildWhatsAppUri(cleanPhoneNumber: String, messageText: String): Uri {
        val sanitizedPhone = cleanPhoneNumber.filter { it.isDigit() }
        return Uri.Builder()
            .scheme("https")
            .authority("wa.me")
            .path(sanitizedPhone)
            .appendQueryParameter("text", messageText)
            .build()
    }

    /**
     * Returns the string representation of the deep link for live UI preview.
     */
    fun getPreviewUrl(cleanPhoneNumber: String, messageText: String): String {
        return buildWhatsAppUri(cleanPhoneNumber, messageText).toString()
    }

    /**
     * Directly opens the recipient's WhatsApp chat with pre-filled message text.
     * The user must manually press Send inside WhatsApp.
     * If WhatsApp is not installed, redirects to the Google Play Store to install it.
     */
    fun launchWhatsApp(
        context: Context,
        recipient: Recipient,
        messageText: String,
        showFeedbackToast: Boolean = true
    ): LaunchResult {
        val cleanNumber = recipient.cleanFullNumber
        if (cleanNumber.length < 7) {
            val errorMsg = "Invalid phone number for ${recipient.name}"
            if (showFeedbackToast) {
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            }
            return LaunchResult.Error(errorMsg)
        }

        val uri = buildWhatsAppUri(cleanNumber, messageText)
        com.example.accessibility.WhatsAppAutoSendService.armAutoSend()

        // Attempt 1: Target standard WhatsApp application directly
        val waIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage(WHATSAPP_PACKAGE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(waIntent)
            return LaunchResult.Success
        } catch (_: ActivityNotFoundException) {
            // Attempt 2: Target WhatsApp Business
            val businessIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage(WHATSAPP_BUSINESS_PACKAGE)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(businessIntent)
                return LaunchResult.Success
            } catch (_: ActivityNotFoundException) {
                // Attempt 3: Redirect to Google Play Store to install WhatsApp
                if (showFeedbackToast) {
                    Toast.makeText(
                        context,
                        "WhatsApp not found. Opening Google Play Store...",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                redirectToPlayStore(context)
                return LaunchResult.RedirectedToPlayStore
            } catch (e: Exception) {
                return LaunchResult.Error(e.localizedMessage ?: "Failed to open WhatsApp")
            }
        } catch (e: Exception) {
            return LaunchResult.Error(e.localizedMessage ?: "Failed to open WhatsApp")
        }
    }

    /**
     * Directly opens the recipient's WhatsApp chat with the specified photo or video attached.
     * Uses WhatsApp's standard JID recipient mapping (e.g. 918864846795@s.whatsapp.net).
     */
    fun launchWhatsAppWithMedia(
        context: Context,
        recipient: Recipient,
        mediaUri: Uri,
        mimeType: String = "image/*",
        showFeedbackToast: Boolean = true
    ): LaunchResult {
        val cleanNumber = recipient.cleanFullNumber
        if (cleanNumber.length < 7) {
            val errorMsg = "Invalid phone number for ${recipient.name}"
            if (showFeedbackToast) {
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            }
            return LaunchResult.Error(errorMsg)
        }

        val whatsappJid = "${cleanNumber}@s.whatsapp.net"
        com.example.accessibility.WhatsAppAutoSendService.armAutoSend()

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            setPackage(WHATSAPP_PACKAGE)
            putExtra(Intent.EXTRA_STREAM, mediaUri)
            putExtra("jid", whatsappJid)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(sendIntent)
            return LaunchResult.Success
        } catch (_: ActivityNotFoundException) {
            val businessIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                setPackage(WHATSAPP_BUSINESS_PACKAGE)
                putExtra(Intent.EXTRA_STREAM, mediaUri)
                putExtra("jid", whatsappJid)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(businessIntent)
                return LaunchResult.Success
            } catch (_: ActivityNotFoundException) {
                if (showFeedbackToast) {
                    Toast.makeText(
                        context,
                        "WhatsApp not found. Opening Google Play Store...",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                redirectToPlayStore(context)
                return LaunchResult.RedirectedToPlayStore
            } catch (e: Exception) {
                return LaunchResult.Error(e.localizedMessage ?: "Failed to open WhatsApp")
            }
        } catch (e: Exception) {
            return LaunchResult.Error(e.localizedMessage ?: "Failed to open WhatsApp")
        }
    }

    fun launchWhatsAppWithPhoto(
        context: Context,
        recipient: Recipient,
        photoUri: Uri,
        showFeedbackToast: Boolean = true
    ): LaunchResult = launchWhatsAppWithMedia(context, recipient, photoUri, "image/*", showFeedbackToast)

    private fun redirectToPlayStore(context: Context) {
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URI)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(marketIntent)
        } catch (_: ActivityNotFoundException) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_WEB_URL)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(webIntent)
            } catch (_: Exception) {
                // Ignore fallback failure
            }
        }
    }
}
