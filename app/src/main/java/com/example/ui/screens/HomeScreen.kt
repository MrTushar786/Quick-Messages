package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.DispatchMode
import com.example.data.models.QuickMessage
import com.example.ui.components.DispatchModeCard
import com.example.ui.components.MessageCard
import com.example.ui.components.RecentPhotoCard
import com.example.ui.components.RecipientCard
import com.example.ui.components.RecipientDialog
import com.example.ui.theme.DarkScreenBg
import com.example.ui.theme.GreenFab
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.QuickMessageUiState
import com.example.utils.GalleryHelper

@Composable
fun HomeScreen(
    uiState: QuickMessageUiState,
    onAddMessageClick: () -> Unit,
    onEditMessageClick: (QuickMessage) -> Unit,
    onDeleteMessageClick: (String) -> Unit,
    onTestLaunchClick: (QuickMessage) -> Unit,
    onSendRecentPhotoClick: () -> Unit,
    onEditRecipientClick: () -> Unit,
    onModeSelected: (DispatchMode) -> Unit,
    onCheckAccessibility: () -> Unit,
    onSaveRecipient: (name: String, countryCode: String, phoneNumber: String) -> Unit,
    onDismissRecipientDialog: () -> Unit,
    onResetPresets: () -> Unit,
    onClearSnackbar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var menuExpanded by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.any { it }) {
            onSendRecentPhotoClick()
        }
    }

    LaunchedEffect(Unit) {
        onCheckAccessibility()
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearSnackbar()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DarkScreenBg),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            // Bright green circular FAB with black plus icon matching screenshot
            FloatingActionButton(
                onClick = onAddMessageClick,
                containerColor = GreenFab,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier
                    .size(62.dp)
                    .navigationBarsPadding()
                    .testTag("add_message_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Message",
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkScreenBg)
                .statusBarsPadding()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Top App Bar matching reference UI: "Quick Messages" on left, 3 dots on right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Quick Messages",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = TextWhite
                )

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.testTag("overflow_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = TextWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(Color(0xFF1E1E1E))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Reset Presets", color = TextWhite) },
                            onClick = {
                                menuExpanded = false
                                onResetPresets()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = TextWhite)
                            }
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenFab)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("messages_list"),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Recipient Contact Header Card
                    item {
                        RecipientCard(
                            recipient = uiState.recipient,
                            onEditClick = onEditRecipientClick
                        )
                    }

                    // WhatsApp Auto-Send vs Manual Mode Switcher
                    item {
                        DispatchModeCard(
                            currentMode = uiState.dispatchMode,
                            isAccessibilityEnabled = uiState.isAccessibilityEnabled,
                            onModeSelected = onModeSelected
                        )
                    }

                    // 1-Click Send Recent Photo / Video Action Card
                    item {
                        RecentPhotoCard(
                            recipient = uiState.recipient,
                            onSendClick = {
                                if (GalleryHelper.hasPermission(context)) {
                                    onSendRecentPhotoClick()
                                } else {
                                    permissionLauncher.launch(GalleryHelper.getRequiredPermissions())
                                }
                            }
                        )
                    }

                    // Message Cards
                    items(
                        items = uiState.messages,
                        key = { it.id }
                    ) { msg ->
                        val buttonText = when (uiState.dispatchMode) {
                            DispatchMode.WHATSAPP_AUTO -> if (uiState.isAccessibilityEnabled) "Auto-Send" else "Send"
                            DispatchMode.WHATSAPP_MANUAL -> "Send"
                        }

                        MessageCard(
                            message = msg,
                            defaultRecipient = uiState.recipient,
                            sendButtonLabel = buttonText,
                            onTestLaunch = { onTestLaunchClick(msg) },
                            onEdit = { onEditMessageClick(msg) },
                            onDelete = { onDeleteMessageClick(msg.id) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }

    if (uiState.isRecipientDialogOpen) {
        RecipientDialog(
            currentRecipient = uiState.recipient,
            onDismiss = onDismissRecipientDialog,
            onConfirm = onSaveRecipient
        )
    }
}
