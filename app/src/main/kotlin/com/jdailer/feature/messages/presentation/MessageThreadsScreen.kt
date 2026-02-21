package com.jdailer.feature.messages.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.jdailer.core.database.enums.HistorySource
import com.jdailer.feature.messages.domain.model.UnifiedMessageThread
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.DateFormat
import java.util.Date

@Composable
fun MessageThreadsScreen(modifier: Modifier = Modifier) {
    val viewModel: MessageThreadsViewModel = koinViewModel()
    val threads = viewModel.threads.collectAsLazyPagingItems()
    val query by viewModel.query.collectAsState()
    val sources by viewModel.sources.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val syncMessage by viewModel.syncMessage.collectAsState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val availableSources = listOf(HistorySource.SMS, HistorySource.MMS)
    var openThreadId by rememberSaveable { mutableStateOf<String?>(null) }
    var openThreadAddress by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(openThreadId) {
        if (openThreadId == null) {
            threads.refresh()
        }
    }

    LaunchedEffect(syncMessage) {
        syncMessage?.let { summary ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    "Messages sync: ${summary.syncedMessages} messages, ${summary.syncedThreads} threads"
                )
            }
        }
    }

    if (openThreadId != null && openThreadAddress != null) {
        MessageThreadScreen(
            threadId = openThreadId.orEmpty(),
            targetAddress = openThreadAddress.orEmpty(),
            modifier = modifier
        )
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { contentPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryUpdated,
                label = { Text("Search SMS / MMS") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableSources.forEach { source ->
                    val selected = sources.contains(source)
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.toggleSource(source) },
                        label = { Text(source.name) }
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(threads.itemCount) { index ->
                    val thread = threads[index]
                    if (thread == null) return@items
                    MessageThreadCard(
                        thread = thread,
                        onTap = {
                            openThreadId = thread.threadId
                            openThreadAddress = thread.address.orEmpty().ifBlank { "Thread ${thread.threadId}" }
                        },
                        onOpenExternal = {
                            val threadUri = Uri.parse("sms:${thread.address ?: thread.threadId}")
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, threadUri))
                            }.onFailure {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Unable to open message thread")
                                }
                            }
                        }
                    )
                }
                item {
                    when (threads.loadState.refresh) {
                        is LoadState.Loading -> Text("Loading…")
                        is LoadState.Error -> Text("Failed to load message threads")
                        else -> {}
                    }
                }
                item {
                    when (threads.loadState.append) {
                        is LoadState.Loading -> Text("Loading more…")
                        is LoadState.Error -> Text("Failed to load more items")
                        else -> Text("")
                    }
                }
            }

            TextButton(
                onClick = { viewModel.refresh() },
                enabled = threads.loadState.refresh != LoadState.Loading
            ) {
                Text("Refresh")
            }
        }
    }
}

@Composable
private fun MessageThreadCard(
    thread: UnifiedMessageThread,
    onTap: () -> Unit,
    onOpenExternal: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                thread.title.orEmpty().ifBlank { thread.address.orEmpty() },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                thread.snippet.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    DateFormat.getDateTimeInstance().format(Date(thread.lastMessageAt)),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    thread.source.name + if (thread.isRcs) " • RCS" else "",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Text(
                "Unread: ${thread.unreadCount} · ${if (thread.isPinned) "Pinned" else "Not pinned"}",
                style = MaterialTheme.typography.labelSmall
            )
            TextButton(onClick = onOpenExternal) {
                Text("Open external")
            }
        }
    }
}
