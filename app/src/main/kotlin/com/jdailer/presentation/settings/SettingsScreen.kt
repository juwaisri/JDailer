package com.jdailer.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val viewModel: SettingsViewModel = koinViewModel()
    val policy by viewModel.uiState.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState(initial = null)

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeActionMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { contentPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Recording & Privacy",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            PreferenceToggleRow(
                title = "Enable call recordings",
                detail = "Store recordings only when explicitly enabled.",
                checked = policy.recordingEnabled,
                onCheckedChange = viewModel::setRecordingEnabled
            )
            PreferenceToggleRow(
                title = "Require explicit consent",
                detail = "Ask per-call consent before start.",
                checked = policy.requireExplicitConsent,
                onCheckedChange = viewModel::setRequireExplicitConsent
            )
            PreferenceToggleRow(
                title = "Allow cloud backup",
                detail = "Keep recordings and metadata available to cloud backups.",
                checked = policy.allowCloudBackup,
                onCheckedChange = viewModel::setAllowCloudBackup
            )
            PreferenceToggleRow(
                title = "Redact metadata",
                detail = "Hash caller number in local call recording metadata.",
                checked = policy.redactMetadata,
                onCheckedChange = viewModel::setRedactMetadata
            )
            PreferenceToggleRow(
                title = "Notify when recording starts",
                detail = "Show visible recording state while active.",
                checked = policy.notifyOnRecordingStart,
                onCheckedChange = viewModel::setNotifyOnRecording
            )

            Card {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Retention", style = MaterialTheme.typography.titleSmall)
                        Text("${policy.autoDeleteAfterDays} days")
                    }
                    Text(
                        "Automatic deletion removes recordings older than the configured period.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { viewModel.setAutoDeleteAfterDays((policy.autoDeleteAfterDays - 1).coerceAtLeast(1)) }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Decrease retention"
                            )
                        }
                        Spacer(Modifier.size(8.dp))
                        Button(
                            onClick = { viewModel.setAutoDeleteAfterDays((policy.autoDeleteAfterDays + 1).coerceAtMost(365)) }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowUp,
                                contentDescription = "Increase retention"
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Button(onClick = { viewModel.clearRecordings() }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Clear recordings"
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Clear recordings")
                        }
                    }
                }
            }

            Divider()
            Text(
                "Privacy policy defaults",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                "Cloud and message settings are applied on every recording attempt and storage write.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun PreferenceToggleRow(
    title: String,
    detail: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(detail, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
