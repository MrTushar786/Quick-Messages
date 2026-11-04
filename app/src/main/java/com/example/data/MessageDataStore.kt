package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.models.QuickMessage
import com.example.data.models.Recipient
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "quick_messages_prefs")

class MessageDataStore(private val context: Context) {

    companion object {
        private val KEY_RECIPIENT = stringPreferencesKey("key_recipient")
        private val KEY_MESSAGES = stringPreferencesKey("key_messages")

        val DEFAULT_RECIPIENT = Recipient(
            id = "default_recipient",
            name = "Default",
            countryCode = "+91",
            phoneNumber = "8864846795"
        )

        val DEFAULT_MESSAGES = listOf(
            QuickMessage(
                id = "default_msg_1",
                title = "Leaving",
                messageText = "Nikal raha",
                iconEmoji = "🚗",
                orderIndex = 0
            ),
            QuickMessage(
                id = "default_msg_2",
                title = "Reached",
                messageText = "Poch Gaya",
                iconEmoji = "📍",
                orderIndex = 1
            ),
            QuickMessage(
                id = "default_msg_3",
                title = "On My Way",
                messageText = "Raste me hu",
                iconEmoji = "⏱️",
                orderIndex = 2
            ),
            QuickMessage(
                id = "default_msg_4",
                title = "Call Later",
                messageText = "Abhi busy hu, thodi der me call karta hu",
                iconEmoji = "📞",
                orderIndex = 3
            )
        )
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val recipientAdapter = moshi.adapter(Recipient::class.java)
    private val messagesListType = Types.newParameterizedType(List::class.java, QuickMessage::class.java)
    private val messagesAdapter = moshi.adapter<List<QuickMessage>>(messagesListType)

    val recipientFlow: Flow<Recipient> = context.dataStore.data.map { preferences ->
        val json = preferences[KEY_RECIPIENT]
        if (json.isNullOrBlank()) {
            DEFAULT_RECIPIENT
        } else {
            try {
                recipientAdapter.fromJson(json) ?: DEFAULT_RECIPIENT
            } catch (_: Exception) {
                DEFAULT_RECIPIENT
            }
        }
    }

    val messagesFlow: Flow<List<QuickMessage>> = context.dataStore.data.map { preferences ->
        val json = preferences[KEY_MESSAGES]
        if (json.isNullOrBlank()) {
            DEFAULT_MESSAGES
        } else {
            try {
                val list = messagesAdapter.fromJson(json)
                if (list.isNullOrEmpty()) DEFAULT_MESSAGES else list.sortedBy { it.orderIndex }
            } catch (_: Exception) {
                DEFAULT_MESSAGES
            }
        }
    }

    suspend fun saveRecipient(recipient: Recipient) {
        val json = recipientAdapter.toJson(recipient)
        context.dataStore.edit { preferences ->
            preferences[KEY_RECIPIENT] = json
        }
    }

    suspend fun saveMessages(messages: List<QuickMessage>) {
        val ordered = messages.mapIndexed { index, msg -> msg.copy(orderIndex = index) }
        val json = messagesAdapter.toJson(ordered)
        context.dataStore.edit { preferences ->
            preferences[KEY_MESSAGES] = json
        }
    }
}
