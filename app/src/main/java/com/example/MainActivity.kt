package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.data.models.QuickMessage
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MessageEditorScreen
import com.example.ui.theme.DarkScreenBg
import com.example.ui.theme.QuickMessageTheme
import com.example.ui.viewmodel.QuickMessageViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: QuickMessageViewModel by viewModels {
        val app = application as QuickMessageApplication
        QuickMessageViewModel.Factory(app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            QuickMessageTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkScreenBg
                ) {
                    QuickMessageApp(viewModel = viewModel)
                }
            }
        }
    }
}

private enum class Screen {
    HOME,
    EDITOR
}

@Composable
fun QuickMessageApp(
    viewModel: QuickMessageViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var currentScreen by remember { mutableStateOf(Screen.HOME) }

    when (currentScreen) {
        Screen.HOME -> {
            HomeScreen(
                uiState = uiState,
                onAddMessageClick = {
                    viewModel.setEditTarget(null)
                    currentScreen = Screen.EDITOR
                },
                onEditMessageClick = { message ->
                    viewModel.setEditTarget(message)
                    currentScreen = Screen.EDITOR
                },
                onDeleteMessageClick = { id ->
                    viewModel.deleteMessage(id)
                },
                onTestLaunchClick = { message ->
                    viewModel.testLaunchWhatsApp(context, message)
                },
                onSendRecentPhotoClick = {
                    viewModel.sendRecentPhoto(context)
                },
                onEditRecipientClick = {
                    viewModel.openRecipientDialog()
                },
                onModeSelected = { mode ->
                    viewModel.setDispatchMode(mode)
                },
                onCheckAccessibility = {
                    viewModel.checkAccessibilityStatus(context)
                },
                onSaveRecipient = { name, code, phone ->
                    viewModel.updateRecipient(name, code, phone)
                },
                onDismissRecipientDialog = {
                    viewModel.closeRecipientDialog()
                },
                onResetPresets = {
                    viewModel.resetToDefaults()
                },
                onClearSnackbar = {
                    viewModel.clearSnackbar()
                },
                modifier = modifier
            )
        }

        Screen.EDITOR -> {
            MessageEditorScreen(
                editingMessage = uiState.selectedMessageForEdit,
                defaultRecipient = uiState.recipient,
                onBack = {
                    viewModel.setEditTarget(null)
                    currentScreen = Screen.HOME
                },
                onSave = { id, title, text, emoji ->
                    val success = viewModel.saveMessage(
                        id = id,
                        title = title,
                        messageText = text,
                        iconEmoji = emoji
                    )
                    if (success) {
                        currentScreen = Screen.HOME
                    }
                },
                modifier = modifier
            )
        }
    }
}
