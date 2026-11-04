package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.models.Recipient
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkScreenBg
import com.example.ui.theme.GreenSendButton
import com.example.ui.theme.RedAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.utils.PhoneNumberValidator

@Composable
fun RecipientDialog(
    currentRecipient: Recipient,
    onDismiss: () -> Unit,
    onConfirm: (name: String, countryCode: String, phoneNumber: String) -> Unit
) {
    var name by remember { mutableStateOf(currentRecipient.name) }
    var countryCode by remember { mutableStateOf(currentRecipient.countryCode) }
    var phoneNumber by remember { mutableStateOf(currentRecipient.phoneNumber) }
    var validationError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(DarkCardBg)
                .border(1.dp, DarkCardBorder, RoundedCornerShape(22.dp))
                .padding(22.dp)
                .testTag("recipient_dialog")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Edit Recipient",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextWhite
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Enter the recipient name and WhatsApp phone number.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Recipient Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Contact Name", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = GreenSendButton,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedContainerColor = DarkScreenBg,
                        unfocusedContainerColor = DarkScreenBg
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recipient_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Country Code + Phone Number
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = countryCode,
                        onValueChange = {
                            countryCode = it
                            validationError = null
                        },
                        label = { Text("Code", color = TextMuted) },
                        placeholder = { Text("+91", color = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = GreenSendButton,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedContainerColor = DarkScreenBg,
                            unfocusedContainerColor = DarkScreenBg
                        ),
                        modifier = Modifier
                            .width(90.dp)
                            .testTag("country_code_input")
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = {
                            phoneNumber = it
                            validationError = null
                        },
                        label = { Text("Phone Number", color = TextMuted) },
                        placeholder = { Text("8864846795", color = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = GreenSendButton,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedContainerColor = DarkScreenBg,
                            unfocusedContainerColor = DarkScreenBg
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("phone_number_input")
                    )
                }

                if (validationError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = validationError ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RedAccent
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_recipient_button")
                    ) {
                        Text("Cancel", color = TextMuted)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val validation = PhoneNumberValidator.validate(countryCode, phoneNumber)
                            when (validation) {
                                is PhoneNumberValidator.ValidationResult.Valid -> {
                                    onConfirm(name, countryCode, phoneNumber)
                                }
                                is PhoneNumberValidator.ValidationResult.Invalid -> {
                                    validationError = validation.errorMessage
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenSendButton,
                            contentColor = TextWhite
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("save_recipient_button")
                    ) {
                        Text(
                            text = "Save",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
