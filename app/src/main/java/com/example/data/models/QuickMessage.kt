package com.example.data.models

import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
data class QuickMessage(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val messageText: String,
    val iconEmoji: String = "",
    val customRecipient: Recipient? = null,
    val orderIndex: Int = 0
)
