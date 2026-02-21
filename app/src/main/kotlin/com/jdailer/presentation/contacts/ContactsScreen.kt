package com.jdailer.presentation.contacts

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.jdailer.feature.contacts.domain.model.UnifiedContact
import com.jdailer.feature.integrations.base.CommunicationTarget
import com.jdailer.feature.integrations.base.IntentResult
import com.jdailer.feature.integrations.router.CommunicationHubRouter
import com.jdailer.feature.quickactions.domain.model.SmartAction
import com.jdailer.feature.quickactions.domain.model.SmartActionType
import com.jdailer.feature.quickactions.presentation.SmartContactCard
import com.jdailer.feature.voip.domain.model.CallRouteType
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.DateFormat
import java.util.Date

@Composable
fun ContactsScreen(
    modifier: Modifier = Modifier,
    communicationHubRouter: CommunicationHubRouter
) {
    val context = LocalContext.current
    val viewModel: ContactsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val contacts by viewModel.contacts.collectAsState(emptyList())
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result[Manifest.permission.READ_CONTACTS] == true) {
            viewModel.syncFromDevice()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(message = "Contacts permission denied")
            }
        }
    }

    LaunchedEffect(uiState.syncMessage) {
        uiState.syncMessage?.let { message ->
            snackbarHostState.showSnackbar(message = message)
            viewModel.clearSyncMessage()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { contentPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = viewModel::onSearchChanged,
                    label = { Text("Search local contacts") },
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        val permissionGranted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.READ_CONTACTS
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        if (permissionGranted) {
                            viewModel.syncFromDevice()
                        } else {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.READ_CONTACTS,
                                    Manifest.permission.WRITE_CONTACTS
                                )
                            )
                        }
                    },
                    enabled = !uiState.isSyncing
                ) {
                    Text("Sync")
                }
            }

            if (uiState.isSyncing) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    Text("Syncing from device…")
                }
            }

            uiState.lastSyncedAtMs?.let { syncedAt ->
                Text(
                    "Last synced: ${DateFormat.getDateTimeInstance().format(Date(syncedAt))}",
                    style = MaterialTheme.typography.bodySmall
                )
            } ?: run {
                Text("Last synced: never", style = MaterialTheme.typography.bodySmall)
            }

            HorizontalDivider()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(contacts, key = { it.contactId }) { contact ->
                    ContactCardRow(
                        contact = contact,
                        viewModel = viewModel,
                        communicationHubRouter = communicationHubRouter,
                        snackbarHostState = snackbarHostState,
                        scope = scope,
                        context = context
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactCardRow(
    contact: UnifiedContact,
    viewModel: ContactsViewModel,
    communicationHubRouter: CommunicationHubRouter,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope,
    context: Context
) {
    var quickActions by rememberSaveable(contact.contactId.toString()) {
        mutableStateOf<List<SmartAction>>(emptyList())
    }
    var tags by rememberSaveable(contact.contactId.toString()) {
        mutableStateOf<List<String>>(emptyList())
    }
    var latestNote by rememberSaveable(contact.contactId.toString()) {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(contact.contactId) {
        val resolvedActions = viewModel.resolveQuickActions(contact)
        quickActions = if (resolvedActions.isNotEmpty()) {
            resolvedActions
        } else {
            listOf(
                SmartAction(
                    type = SmartActionType.CALL,
                    actionId = "CALL",
                    label = "Call",
                    sortOrder = 0
                ),
                SmartAction(
                    type = SmartActionType.MESSAGE,
                    actionId = "MESSAGE",
                    label = "Message",
                    sortOrder = 1
                )
            )
        }
        tags = viewModel.resolveQuickTags(contact.contactId)
        latestNote = viewModel.resolveLatestNote(contact.contactId)
    }

    val latestNoteValue = latestNote

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                contact.displayName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                contact.numbers.firstOrNull().orEmpty().ifBlank { "No phone number" },
                style = MaterialTheme.typography.bodyMedium
            )

            if (!latestNoteValue.isNullOrBlank()) {
                Text(
                    latestNoteValue,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            SmartContactCard(
                contact = contact,
                actions = quickActions,
                tags = tags,
                onAction = { type ->
                    when (type) {
                        SmartActionType.CALL -> {
                            val number = contact.numbers.firstOrNull().orEmpty()
                            if (number.isBlank()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(message = "No phone number")
                                }
                            } else {
                                scope.launch {
                                    val result = viewModel.placeCall(number)
                                    if (!result.isRouted) {
                                        snackbarHostState.showSnackbar(
                                            message = result.message.orEmpty().ifBlank { "Call not routed" }
                                        )
                                    }
                                }
                            }
                        }

                        SmartActionType.MESSAGE -> {
                            val number = contact.numbers.firstOrNull().orEmpty()
                            if (number.isBlank()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(message = "No phone number")
                                }
                            } else {
                                scope.launch {
                                    val result = communicationHubRouter.launchMessage(
                                        context = context,
                                        target = CommunicationTarget(
                                            contactId = contact.contactId,
                                            contactName = contact.displayName,
                                            phoneNumber = number
                                        )
                                    )
                                    when (result) {
                                        is IntentResult.Success -> Unit
                                        is IntentResult.Failure -> snackbarHostState.showSnackbar(
                                            message = result.message.ifBlank { "Message launch failed" }
                                        )
                                        is IntentResult.Unavailable -> snackbarHostState.showSnackbar(
                                            message = result.reason
                                        )
                                    }
                                }
                            }
                        }

                        SmartActionType.EMAIL -> {
                            scope.launch {
                                val email = viewModel.resolvePrimaryEmail(contact.contactId)
                                if (email.isNullOrBlank()) {
                                    snackbarHostState.showSnackbar(message = "No email address for this contact")
                                    return@launch
                                }
                                val result = communicationHubRouter.launchEmail(
                                    context = context,
                                    target = CommunicationTarget(
                                        contactId = contact.contactId,
                                        contactName = contact.displayName,
                                        emailAddress = email
                                    )
                                )
                                when (result) {
                                    is IntentResult.Success -> Unit
                                    is IntentResult.Failure -> snackbarHostState.showSnackbar(
                                        message = result.message.ifBlank { "Unable to compose email" }
                                    )
                                    is IntentResult.Unavailable -> snackbarHostState.showSnackbar(message = result.reason)
                                }
                            }
                        }

                        SmartActionType.APP -> {
                            val detailsIntent = Intent(
                                context,
                                com.jdailer.presentation.contacts.ContactDetailActivity::class.java
                            )
                            context.startActivity(detailsIntent)
                        }

                        SmartActionType.BLOCK -> {
                            scope.launch {
                                val number = contact.numbers.firstOrNull().orEmpty()
                                if (number.isBlank()) {
                                    snackbarHostState.showSnackbar(message = "No phone number to block")
                                    return@launch
                                }
                                viewModel.blockNumber(number)
                                snackbarHostState.showSnackbar(message = "Call block applied")
                            }
                        }

                        SmartActionType.NOTE -> {
                            val noteText = latestNoteValue.orEmpty().ifBlank { "No notes available" }
                            scope.launch {
                                snackbarHostState.showSnackbar(message = noteText)
                            }
                        }

                        SmartActionType.SHARE -> {
                            val payload = "${contact.displayName}: ${
                                contact.numbers.joinToString(", ") { number -> number }
                            }"
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                setType("text/plain")
                                putExtra(Intent.EXTRA_TEXT, payload)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share contact"))
                        }
                    }
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = {
                    scope.launch {
                        val number = contact.numbers.firstOrNull().orEmpty()
                        if (number.isBlank()) {
                            snackbarHostState.showSnackbar(message = "No phone number")
                        } else {
                            val result = viewModel.placeCall(number)
                            if (!result.isRouted || result.routeType == CallRouteType.FALLBACK_DIAL) {
                                snackbarHostState.showSnackbar(
                                    message = if (result.routeType == CallRouteType.FAILED) {
                                        result.message.orEmpty().ifBlank { "Unable to place call" }
                                    } else {
                                        "Opened system dialer"
                                    }
                                )
                            }
                        }
                    }
                }) {
                    Icon(Icons.Filled.Call, contentDescription = "Call")
                }
                IconButton(onClick = {
                    launchMessageFromContact(
                        context = context,
                        contact = contact,
                        communicationHubRouter = communicationHubRouter,
                        snackbarHostState = snackbarHostState,
                        scope = scope
                    )
                }) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Message")
                }
                IconButton(onClick = {
                    scope.launch {
                        val email = viewModel.resolvePrimaryEmail(contact.contactId)
                        if (email.isNullOrBlank()) {
                            snackbarHostState.showSnackbar(message = "No email address")
                            return@launch
                        }
                        val result = communicationHubRouter.launchEmail(
                            context = context,
                            target = CommunicationTarget(
                                contactId = contact.contactId,
                                contactName = contact.displayName,
                                emailAddress = email
                            )
                        )
                        when (result) {
                            is IntentResult.Success -> Unit
                            is IntentResult.Failure -> snackbarHostState.showSnackbar(
                                message = result.message.ifBlank { "Unable to compose email" }
                            )
                            is IntentResult.Unavailable -> snackbarHostState.showSnackbar(message = result.reason)
                        }
                    }
                }) {
                    Icon(Icons.Filled.Email, contentDescription = "Email")
                }
                IconButton(onClick = {
                    val payload = "${contact.displayName}: ${contact.numbers.joinToString(", ")}"
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        setType("text/plain")
                        putExtra(Intent.EXTRA_TEXT, payload)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share contact"))
                }) {
                    Icon(Icons.Filled.Share, contentDescription = "Share")
                }
                IconButton(onClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar(message = "Spam decision is evaluated in call screening")
                    }
                }) {
                    Icon(Icons.Filled.Warning, contentDescription = "Spam/Block")
                }
            }
        }
    }
}

private fun launchMessageFromContact(
    context: Context,
    contact: UnifiedContact,
    communicationHubRouter: CommunicationHubRouter,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val number = contact.numbers.firstOrNull().orEmpty()
    if (number.isBlank()) {
        scope.launch {
            snackbarHostState.showSnackbar(message = "No phone number")
        }
        return
    }
    scope.launch {
        val result = communicationHubRouter.launchMessage(
            context = context,
            target = CommunicationTarget(
                contactId = contact.contactId,
                contactName = contact.displayName,
                phoneNumber = number
            )
        )
        when (result) {
            is IntentResult.Success -> Unit
            is IntentResult.Failure -> snackbarHostState.showSnackbar(
                message = result.message.ifBlank { "Unable to open messaging app" }
            )
            is IntentResult.Unavailable -> snackbarHostState.showSnackbar(message = result.reason)
        }
    }
}
