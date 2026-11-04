package com.example.data.models

enum class DispatchMode(val displayName: String, val description: String) {
    WHATSAPP_AUTO(
        "WhatsApp Auto-Send",
        "Auto-sends in WhatsApp and instantly closes back to your screen in 0.3s"
    ),
    DIRECT_SMS(
        "Direct SMS",
        "Sends SMS text silently in background with zero screens opening"
    ),
    WHATSAPP_MANUAL(
        "WhatsApp Manual",
        "Opens WhatsApp chat pre-filled for you to review and send"
    )
}
