package com.example.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.example.accessibility.WhatsAppAutoSendService
import com.example.data.MessageRepository
import com.example.data.models.DispatchMode
import com.example.data.models.Recipient
import com.example.utils.WhatsAppHelper

class LaunchMessageActionCallback : ActionCallback {

    companion object {
        val KEY_MESSAGE_TEXT = ActionParameters.Key<String>("key_message_text")
        val KEY_RECIPIENT_PHONE = ActionParameters.Key<String>("key_recipient_phone")
        val KEY_RECIPIENT_CODE = ActionParameters.Key<String>("key_recipient_code")
        val KEY_RECIPIENT_NAME = ActionParameters.Key<String>("key_recipient_name")
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val messageText = parameters[KEY_MESSAGE_TEXT].orEmpty()
        val phone = parameters[KEY_RECIPIENT_PHONE] ?: "8864846795"
        val code = parameters[KEY_RECIPIENT_CODE] ?: "+91"
        val name = parameters[KEY_RECIPIENT_NAME] ?: "Recipient"

        val recipient = Recipient(
            id = "widget_recipient",
            name = name,
            countryCode = code,
            phoneNumber = phone
        )

        val repository = MessageRepository(context)
        val mode = repository.getDispatchMode()

        if (mode == DispatchMode.WHATSAPP_AUTO) {
            WhatsAppAutoSendService.armAutoSend()
        }

        WhatsAppHelper.launchWhatsApp(
            context = context,
            recipient = recipient,
            messageText = messageText,
            showFeedbackToast = true
        )
    }
}
