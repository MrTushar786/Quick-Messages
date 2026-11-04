package com.example.data

import android.content.Context
import com.example.data.models.QuickMessage
import com.example.data.models.Recipient
import com.example.widget.WidgetUpdater
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class MessageRepository(
    private val context: Context,
    private val dataStore: MessageDataStore = MessageDataStore(context)
) {

    val recipientFlow: Flow<Recipient> = dataStore.recipientFlow
    val messagesFlow: Flow<List<QuickMessage>> = dataStore.messagesFlow
    val dispatchModeFlow: Flow<com.example.data.models.DispatchMode> = dataStore.dispatchModeFlow

    suspend fun getRecipient(): Recipient {
        return recipientFlow.first()
    }

    suspend fun getDispatchMode(): com.example.data.models.DispatchMode {
        return dispatchModeFlow.first()
    }

    suspend fun updateDispatchMode(mode: com.example.data.models.DispatchMode) {
        dataStore.saveDispatchMode(mode)
        WidgetUpdater.update(context)
    }

    suspend fun getMessages(): List<QuickMessage> {
        return messagesFlow.first()
    }

    suspend fun updateRecipient(recipient: Recipient) {
        dataStore.saveRecipient(recipient)
        WidgetUpdater.update(context)
    }

    suspend fun upsertMessage(message: QuickMessage) {
        val current = messagesFlow.first().toMutableList()
        val index = current.indexOfFirst { it.id == message.id }
        if (index >= 0) {
            current[index] = message
        } else {
            current.add(message.copy(orderIndex = current.size))
        }
        dataStore.saveMessages(current)
        WidgetUpdater.update(context)
    }

    suspend fun deleteMessage(messageId: String) {
        val current = messagesFlow.first().filterNot { it.id == messageId }
        dataStore.saveMessages(current)
        WidgetUpdater.update(context)
    }

    suspend fun moveMessage(fromIndex: Int, toIndex: Int) {
        val current = messagesFlow.first().toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices && fromIndex != toIndex) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            dataStore.saveMessages(current)
            WidgetUpdater.update(context)
        }
    }

    suspend fun resetToDefaults() {
        dataStore.saveRecipient(MessageDataStore.DEFAULT_RECIPIENT)
        dataStore.saveMessages(MessageDataStore.DEFAULT_MESSAGES)
        WidgetUpdater.update(context)
    }
}
