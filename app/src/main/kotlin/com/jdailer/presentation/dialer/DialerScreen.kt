package com.jdailer.presentation.dialer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.saveable.rememberSaveable
import com.jdailer.feature.dialer.domain.model.DialerSuggestion
import com.jdailer.feature.integrations.base.CommunicationAdapterRegistry
import com.jdailer.feature.integrations.base.CommunicationTarget
import com.jdailer.feature.integrations.base.IntentResult
import com.jdailer.feature.integrations.router.CommunicationHubRouter
import com.jdailer.feature.voip.domain.model.CallRouteResult
import com.jdailer.feature.voip.domain.model.CallRouteType
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun DialerScreen(
    modifier: Modifier = Modifier,
    adapterRegistry: CommunicationAdapterRegistry,
    communicationHubRouter: CommunicationHubRouter
) {
    val viewModel: DialerViewModel = koinViewModel()
    val query by viewModel.query.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val keypadRows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("*", "0", "#")
    )

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { contentPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search or dial") }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = {
                    if (query.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("No number entered")
                        }
                        return@Button
                    }
                    scope.launch { placeAndReportResult(viewModel, query, snackbarHostState) }
                }, enabled = query.isNotBlank()) {
                    Icon(Icons.Filled.Call, contentDescription = "Call")
                    Spacer(Modifier.width(8.dp))
                    Text("Call")
                }

                Button(onClick = { viewModel.clearQuery() }) {
                    Text("Clear")
                }
            }

            Divider()

            LazyColumn(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(suggestions, key = { item -> "${item.contactId}-${item.phoneNumber}" }) { suggestion ->
                    DialerSuggestionRow(
                        suggestion = suggestion,
                        onCall = {
                            if (suggestion.phoneNumber.isBlank()) return@DialerSuggestionRow
                            scope.launch { placeAndReportResult(viewModel, suggestion.phoneNumber, snackbarHostState) }
                        },
                        onMessage = {
                            scope.launch {
                                val result = communicationHubRouter.launchMessage(
                                    context = context,
                                    target = CommunicationTarget(
                                        contactId = suggestion.contactId,
                                        contactName = suggestion.displayName,
                                        phoneNumber = suggestion.phoneNumber
                                    )
                                )
                                when (result) {
                                    is IntentResult.Success -> Unit
                                    is IntentResult.Failure -> snackbarHostState.showSnackbar(
                                        result.message.ifBlank { "Message launch failed" }
                                    )
                                    is IntentResult.Unavailable -> snackbarHostState.showSnackbar(result.reason)
                                }
                            }
                        },
                        onPick = { viewModel.onSelectSuggestion(suggestion) }
                    )
                }
            }

            Surface(shape = RoundedCornerShape(18.dp)) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    keypadRows.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row.forEach { digit ->
                                Button(modifier = Modifier.weight(1f), onClick = {
                                    viewModel.appendDigit(digit)
                                }) {
                                    Text(digit)
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(modifier = Modifier.weight(1f), onClick = {
                            viewModel.appendDigit("⌫")
                        }) {
                            Text("⌫")
                        }
                        Button(modifier = Modifier.weight(2f), onClick = {
                            val firstSuggestion = suggestions.firstOrNull()
                            val target = firstSuggestion?.phoneNumber.orEmpty()
                            if (target.isBlank()) {
                                scope.launch { snackbarHostState.showSnackbar("No suggested number") }
                            } else {
                                scope.launch { placeAndReportResult(viewModel, target, snackbarHostState) }
                            }
                        }) {
                            Text("Call suggestion")
                        }
                    }
                }
            }
        }
    }
}

private suspend fun placeAndReportResult(
    viewModel: DialerViewModel,
    number: String,
    snackbarHostState: SnackbarHostState
) {
    val result: CallRouteResult = viewModel.placeCall(number)
    if (!result.isRouted) {
        snackbarHostState.showSnackbar(result.message.orEmpty().ifBlank { "Failed to place call" })
    } else if (result.routeType == CallRouteType.FALLBACK_DIAL) {
        snackbarHostState.showSnackbar("Opened system dialer")
    }
}

@Composable
private fun DialerSuggestionRow(
    suggestion: DialerSuggestion,
    onCall: () -> Unit,
    onMessage: () -> Unit,
    onPick: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                Text(suggestion.displayName, style = MaterialTheme.typography.titleMedium)
                Text(
                    suggestion.phoneNumber.ifBlank { "No number" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start
                )
            }
            Row {
                IconButton(onClick = onCall) {
                    Icon(Icons.Filled.Call, contentDescription = "Call")
                }
                IconButton(onClick = onMessage) {
                    Icon(Icons.Filled.Send, contentDescription = "Message")
                }
                Button(onClick = onPick) {
                    Text("Pick")
                }
            }
        }
    }
}
