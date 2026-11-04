package com.example.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.MainActivity
import com.example.data.MessageRepository
import com.example.data.models.QuickMessage
import com.example.data.models.Recipient

class QuickMessageWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = MessageRepository(context)
        val recipient = repository.getRecipient()
        val messages = repository.getMessages()

        provideContent {
            NothingWidgetContent(
                recipient = recipient,
                messages = messages
            )
        }
    }

    @androidx.compose.runtime.Composable
    private fun NothingWidgetContent(
        recipient: Recipient,
        messages: List<QuickMessage>
    ) {
        val tileBgColor = Color(0xFF141414)
        val widgetBgColor = Color(0xFF000000)
        val textPrimaryColor = Color(0xFFFFFFFF)
        val textMutedColor = Color(0xFF888888)
        val dotAccentColor = Color(0xFFE0E0E0)

        // Outer container: Pitch black Nothing OS rounded shell
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(widgetBgColor)
                .cornerRadius(22.dp)
                .padding(10.dp)
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize()
            ) {
                // Header row: Nothing OS style dot matrix indicator & recipient info
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "• QUICK MSG",
                        modifier = GlanceModifier.clickable(actionStartActivity<MainActivity>()),
                        style = TextStyle(
                            color = androidx.glance.unit.ColorProvider(dotAccentColor),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    // Quick Action: Send Recent Photo directly from home screen widget
                    Box(
                        modifier = GlanceModifier
                            .background(Color(0xFF15A24D))
                            .cornerRadius(10.dp)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .clickable(
                                actionRunCallback<SendRecentPhotoActionCallback>(
                                    actionParametersOf(
                                        SendRecentPhotoActionCallback.KEY_RECIPIENT_PHONE to recipient.phoneNumber,
                                        SendRecentPhotoActionCallback.KEY_RECIPIENT_CODE to recipient.countryCode,
                                        SendRecentPhotoActionCallback.KEY_RECIPIENT_NAME to recipient.name
                                    )
                                )
                            )
                    ) {
                        Text(
                            text = "📷 SEND PHOTO",
                            style = TextStyle(
                                color = androidx.glance.unit.ColorProvider(Color.White),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    Text(
                        text = "→ ${recipient.name.uppercase()}",
                        maxLines = 1,
                        modifier = GlanceModifier.clickable(actionStartActivity<MainActivity>()),
                        style = TextStyle(
                            color = androidx.glance.unit.ColorProvider(textMutedColor),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(6.dp))

                // Adaptive Grid for 4 quick message tiles (2x2)
                val primaryMessages = messages.take(4)

                if (primaryMessages.isEmpty()) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .background(tileBgColor)
                            .cornerRadius(14.dp)
                            .clickable(actionStartActivity<MainActivity>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tap to configure messages",
                            style = TextStyle(
                                color = androidx.glance.unit.ColorProvider(textMutedColor),
                                fontSize = 12.sp
                            )
                        )
                    }
                } else {
                    // Row 1 (first 2 items)
                    val row1Items = primaryMessages.take(2)
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .defaultWeight(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (item in row1Items) {
                            MessageTile(
                                message = item,
                                defaultRecipient = recipient,
                                tileBgColor = tileBgColor,
                                primaryColor = textPrimaryColor,
                                mutedColor = textMutedColor,
                                modifier = GlanceModifier
                                    .defaultWeight()
                                    .fillMaxSize()
                                    .padding(3.dp)
                            )
                        }
                        if (row1Items.size == 1) {
                            Spacer(modifier = GlanceModifier.defaultWeight())
                        }
                    }

                    // Row 2 (items 3 and 4 if available)
                    val row2Items = primaryMessages.drop(2)
                    if (row2Items.isNotEmpty()) {
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .defaultWeight(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (item in row2Items) {
                                MessageTile(
                                    message = item,
                                    defaultRecipient = recipient,
                                    tileBgColor = tileBgColor,
                                    primaryColor = textPrimaryColor,
                                    mutedColor = textMutedColor,
                                    modifier = GlanceModifier
                                        .defaultWeight()
                                        .fillMaxSize()
                                        .padding(3.dp)
                                )
                            }
                            if (row2Items.size == 1) {
                                Spacer(modifier = GlanceModifier.defaultWeight())
                            }
                        }
                    }
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun MessageTile(
        message: QuickMessage,
        defaultRecipient: Recipient,
        tileBgColor: Color,
        primaryColor: Color,
        mutedColor: Color,
        modifier: GlanceModifier
    ) {
        val targetRecipient = message.customRecipient ?: defaultRecipient
        val titleText = if (message.iconEmoji.isNotBlank()) {
            "${message.iconEmoji} ${message.title}"
        } else {
            message.title
        }

        Box(
            modifier = modifier
                .background(tileBgColor)
                .cornerRadius(14.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .clickable(
                    actionRunCallback<LaunchMessageActionCallback>(
                        actionParametersOf(
                            LaunchMessageActionCallback.KEY_MESSAGE_TEXT to message.messageText,
                            LaunchMessageActionCallback.KEY_RECIPIENT_PHONE to targetRecipient.phoneNumber,
                            LaunchMessageActionCallback.KEY_RECIPIENT_CODE to targetRecipient.countryCode,
                            LaunchMessageActionCallback.KEY_RECIPIENT_NAME to targetRecipient.name
                        )
                    )
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titleText.uppercase(),
                    maxLines = 1,
                    style = TextStyle(
                        color = androidx.glance.unit.ColorProvider(primaryColor),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = message.messageText,
                    maxLines = 1,
                    style = TextStyle(
                        color = androidx.glance.unit.ColorProvider(mutedColor),
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}
