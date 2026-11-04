package com.example.widget

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.example.data.models.Recipient
import com.example.utils.GalleryHelper
import com.example.utils.WhatsAppHelper

class SendRecentPhotoActionCallback : ActionCallback {

    companion object {
        val KEY_RECIPIENT_PHONE = ActionParameters.Key<String>("key_photo_recipient_phone")
        val KEY_RECIPIENT_CODE = ActionParameters.Key<String>("key_photo_recipient_code")
        val KEY_RECIPIENT_NAME = ActionParameters.Key<String>("key_photo_recipient_name")
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val phone = parameters[KEY_RECIPIENT_PHONE] ?: "8864846795"
        val code = parameters[KEY_RECIPIENT_CODE] ?: "+91"
        val name = parameters[KEY_RECIPIENT_NAME] ?: "Recipient"

        val recipient = Recipient(
            id = "widget_photo_recipient",
            name = name,
            countryCode = code,
            phoneNumber = phone
        )

        val latestMedia = GalleryHelper.getLatestMedia(context)
        if (latestMedia != null) {
            WhatsAppHelper.launchWhatsAppWithMedia(
                context = context,
                recipient = recipient,
                mediaUri = latestMedia.uri,
                mimeType = latestMedia.mimeType,
                showFeedbackToast = true
            )
        } else {
            // If permission is not yet granted or no photo found, open app to prompt user cleanly
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent != null) {
                context.startActivity(intent)
            }
        }
    }
}
