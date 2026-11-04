package com.example.data.models

enum class DispatchMode(val displayName: String, val description: String) {
    WHATSAPP_AUTO(
        "WhatsApp Auto-Send",
        "Opens WhatsApp and automatically taps the Send button for you"
    ),
    WHATSAPP_MANUAL(
        "WhatsApp Manual",
        "Opens WhatsApp chat pre-filled for you to review and send"
    )
}
