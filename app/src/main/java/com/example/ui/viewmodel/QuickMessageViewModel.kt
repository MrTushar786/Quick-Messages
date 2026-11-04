package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.MessageRepository
import com.example.data.models.QuickMessage
import com.example.data.models.Recipient
import com.example.utils.PhoneNumberValidator
import com.example.utils.WhatsAppHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class QuickMessageUiState(
    val recipient: Recipient = Recipient(),
    val messages: List<QuickMessage> = emptyList(),
    val isLoading: Boolean = true,
    val selectedMessageForEdit: QuickMessage? = null,
    val isRecipientDialogOpen: Boolean = false,
    val snackbarMessage: String? = null
)

class QuickMessageViewModel(
    private val repository: MessageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuickMessageUiState())
    val uiState: StateFlow<QuickMessageUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.recipientFlow,
                repository.messagesFlow
            ) { recipient, messages ->
                Pair(recipient, messages)
            }.collect { (recipient, messages) ->
                _uiState.update { current ->
                    current.copy(
                        recipient = recipient,
                        messages = messages,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun openRecipientDialog() {
        _uiState.update { it.copy(isRecipientDialogOpen = true) }
    }

    fun closeRecipientDialog() {
        _uiState.update { it.copy(isRecipientDialogOpen = false) }
    }

    fun setEditTarget(message: QuickMessage?) {
        _uiState.update { it.copy(selectedMessageForEdit = message) }
    }

    fun updateRecipient(name: String, countryCode: String, phoneNumber: String): Boolean {
        val validation = PhoneNumberValidator.validate(countryCode, phoneNumber)
        if (validation is PhoneNumberValidator.ValidationResult.Invalid) {
            _uiState.update { it.copy(snackbarMessage = validation.errorMessage) }
            return false
        }

        viewModelScope.launch {
            val updated = Recipient(
                id = _uiState.value.recipient.id,
                name = name.trim().ifBlank { "Default Contact" },
                countryCode = if (countryCode.startsWith("+")) countryCode.trim() else "+${countryCode.trim()}",
                phoneNumber = phoneNumber.filter { it.isDigit() }
            )
            repository.updateRecipient(updated)
            _uiState.update {
                it.copy(
                    isRecipientDialogOpen = false,
                    snackbarMessage = "Recipient updated successfully"
                )
            }
        }
        return true
    }

    fun saveMessage(
        id: String?,
        title: String,
        messageText: String,
        iconEmoji: String,
        customRecipient: Recipient? = null
    ): Boolean {
        if (title.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "Message title cannot be empty") }
            return false
        }
        if (messageText.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "Message text cannot be empty") }
            return false
        }

        viewModelScope.launch {
            val existing = _uiState.value.selectedMessageForEdit
            val msgId = id ?: existing?.id ?: UUID.randomUUID().toString()
            val order = existing?.orderIndex ?: _uiState.value.messages.size

            val message = QuickMessage(
                id = msgId,
                title = title.trim(),
                messageText = messageText.trim(),
                iconEmoji = iconEmoji.trim(),
                customRecipient = customRecipient,
                orderIndex = order
            )
            repository.upsertMessage(message)
            _uiState.update {
                it.copy(
                    selectedMessageForEdit = null,
                    snackbarMessage = "Message saved"
                )
            }
        }
        return true
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
            _uiState.update { it.copy(snackbarMessage = "Message deleted") }
        }
    }

    fun testLaunchWhatsApp(context: Context, message: QuickMessage) {
        val targetRecipient = message.customRecipient ?: _uiState.value.recipient
        val result = WhatsAppHelper.launchWhatsApp(
            context = context,
            recipient = targetRecipient,
            messageText = message.messageText,
            showFeedbackToast = true
        )
        when (result) {
            is WhatsAppHelper.LaunchResult.Success -> {
                _uiState.update { it.copy(snackbarMessage = "Opened WhatsApp chat for '${message.title}'") }
            }
            is WhatsAppHelper.LaunchResult.RedirectedToPlayStore -> {
                _uiState.update { it.copy(snackbarMessage = "WhatsApp not installed. Opening Google Play...") }
            }
            is WhatsAppHelper.LaunchResult.Error -> {
                _uiState.update { it.copy(snackbarMessage = result.message) }
            }
        }
    }

    fun sendRecentMedia(context: Context) {
        val latestMedia = com.example.utils.GalleryHelper.getLatestMedia(context)
        if (latestMedia == null) {
            _uiState.update { it.copy(snackbarMessage = "No photos or videos found in gallery or permission needed") }
            return
        }

        val result = WhatsAppHelper.launchWhatsAppWithMedia(
            context = context,
            recipient = _uiState.value.recipient,
            mediaUri = latestMedia.uri,
            mimeType = latestMedia.mimeType,
            showFeedbackToast = true
        )
        when (result) {
            is WhatsAppHelper.LaunchResult.Success -> {
                val mediaLabel = if (latestMedia.isVideo) "video" else "photo"
                _uiState.update { it.copy(snackbarMessage = "Opening WhatsApp with recent $mediaLabel...") }
            }
            is WhatsAppHelper.LaunchResult.RedirectedToPlayStore -> {
                _uiState.update { it.copy(snackbarMessage = "WhatsApp is not installed. Opening Google Play...") }
            }
            is WhatsAppHelper.LaunchResult.Error -> {
                _uiState.update { it.copy(snackbarMessage = result.message) }
            }
        }
    }

    fun sendRecentPhoto(context: Context) = sendRecentMedia(context)

    fun resetToDefaults() {
        viewModelScope.launch {
            repository.resetToDefaults()
            _uiState.update { it.copy(snackbarMessage = "Reset to default presets") }
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    class Factory(private val repository: MessageRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(QuickMessageViewModel::class.java)) {
                return QuickMessageViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
