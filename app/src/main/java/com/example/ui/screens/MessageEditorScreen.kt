package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.QuickMessage
import com.example.data.models.Recipient
import com.example.ui.components.WhatsAppPreviewCard
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkScreenBg
import com.example.ui.theme.GreenSendButton
import com.example.ui.theme.RedAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

private val PRESET_EMOJIS = listOf("🚗", "📍", "⏱️", "🏠", "💼", "📞", "👋", "❤️", "☕", "🛵", "✈️", "🚀")

@Composable
fun MessageEditorScreen(
    editingMessage: QuickMessage?,
    defaultRecipient: Recipient,
    onBack: () -> Unit,
    onSave: (id: String?, title: String, messageText: String, iconEmoji: String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var title by remember { mutableStateOf(editingMessage?.title.orEmpty()) }
    var messageText by remember { mutableStateOf(editingMessage?.messageText.orEmpty()) }
    var iconEmoji by remember { mutableStateOf(editingMessage?.iconEmoji.orEmpty()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkScreenBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(20.dp)
            .testTag("message_editor_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("editor_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextWhite
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (editingMessage == null) "New Quick Message" else "Edit Quick Message",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = TextWhite
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Message Title Input
        Text(
            text = "Title / Short Label",
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                errorMessage = null
            },
            placeholder = { Text("e.g. Leaving, Reached, On My Way", color = TextMuted) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedBorderColor = GreenSendButton,
                unfocusedBorderColor = DarkCardBorder,
                focusedContainerColor = DarkCardBg,
                unfocusedContainerColor = DarkCardBg
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("message_title_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Message Body Input
        Text(
            text = "Message Text (Prefilled in WhatsApp)",
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = messageText,
            onValueChange = {
                messageText = it
                errorMessage = null
            },
            placeholder = { Text("e.g. Nikal raha, Poch Gaya", color = TextMuted) },
            minLines = 3,
            maxLines = 5,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedBorderColor = GreenSendButton,
                unfocusedBorderColor = DarkCardBorder,
                focusedContainerColor = DarkCardBg,
                unfocusedContainerColor = DarkCardBg
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("message_body_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Emoji / Icon Selector
        Text(
            text = "Emoji Badge (Optional)",
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (iconEmoji.isEmpty()) GreenSendButton else DarkCardBg)
                        .border(1.dp, DarkCardBorder, CircleShape)
                        .clickable { iconEmoji = "" },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "None",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (iconEmoji.isEmpty()) TextWhite else TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
            items(PRESET_EMOJIS) { emoji ->
                val isSelected = iconEmoji == emoji
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) GreenSendButton else DarkCardBg)
                        .border(1.dp, if (isSelected) GreenSendButton else DarkCardBorder, CircleShape)
                        .clickable { iconEmoji = emoji },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 18.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // WhatsApp Live Deep Link Preview
        WhatsAppPreviewCard(
            phoneNumber = defaultRecipient.cleanFullNumber,
            messageText = messageText
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = errorMessage ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = RedAccent
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onBack,
                modifier = Modifier.testTag("cancel_message_button")
            ) {
                Text("Cancel", color = TextMuted)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = {
                    val finalTitle = title.ifBlank { messageText }
                    if (finalTitle.isBlank()) {
                        errorMessage = "Please enter message text or title"
                        return@Button
                    }
                    val finalText = messageText.ifBlank { title }
                    onSave(editingMessage?.id, finalTitle, finalText, iconEmoji)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenSendButton,
                    contentColor = TextWhite
                ),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.testTag("save_message_button")
            ) {
                Text(
                    text = "Save Message",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
