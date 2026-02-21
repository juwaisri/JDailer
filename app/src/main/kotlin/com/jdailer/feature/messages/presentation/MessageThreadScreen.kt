package com.jdailer.feature.messages.presentation

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.jdailer.feature.messages.presentation.components.MessageAttachmentRow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun MessageThreadScreen(
    threadId: String,
    targetAddress: String,
    modifier: Modifier = Modifier
) {
    val viewModel: MessageThreadViewModel = koinViewModel()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val currentThreadId by viewModel.threadId.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val messages = viewModel.messages.collectAsLazyPagingItems()

    val attachmentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris.forEach { uri ->
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            viewModel.addAttachment(uri.toString())
        }
    }

    LaunchedEffect(threadId) {
        if (currentThreadId != threadId) {
            viewModel.openThread(threadId)
            viewModel.setDraftText("")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages.itemCount) { index ->
                val item = messages[index]
                if (item == null) return@items

                val hasMedia = item.mediaUri?.isNotBlank() == true
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "${item.address.orEmpty()} · ${item.direction.name}: ${item.body.orEmpty()}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                    if (hasMedia) {
                        MessageAttachmentRow(
                            attachmentUri = item.mediaUri.orEmpty(),
                            onOpenAttachment = { attachmentUri ->
                                runCatching {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW).apply {
                                            data = Uri.parse(attachmentUri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                    )
                                }
                            }
                        )
                    }
                }
            }

            item {
                when (messages.loadState.append) {
                    is LoadState.Loading -> CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    is LoadState.Error -> Text("Unable to load more messages")
                    else -> {}
                }
            }
            }

        if (uiState.attachmentUris.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                uiState.attachmentUris.forEach { uri ->
                    MessageAttachmentRow(
                        attachmentUri = uri,
                        onOpenAttachment = { viewModel.removeAttachment(uri) },
                        removable = true
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uiState.draftText,
                onValueChange = viewModel::setDraftText,
                label = { Text("Reply to ${targetAddress.ifBlank { "thread" }}") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            IconButton(onClick = { attachmentPicker.launch(arrayOf("*/*")) }) {
                Icon(Icons.Filled.Close, contentDescription = "Attach")
            }

            IconButton(
                onClick = {
                    scope.launch {
                        viewModel.sendReply(targetAddress, uiState.draftText, threadId)
                    }
                },
                enabled = !uiState.isSending && (uiState.draftText.isNotBlank() || uiState.attachmentUris.isNotEmpty())
            ) {
                if (uiState.isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Icon(Icons.Filled.Send, contentDescription = "Send")
                }
            }
        }

        uiState.lastError?.let { error ->
            Text(text = error)
        }
    }
}
