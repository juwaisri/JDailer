package com.jdailer.presentation.history

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.jdailer.core.database.enums.HistorySource
import com.jdailer.feature.history.domain.model.UnifiedHistoryItem
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun UnifiedHistoryScreen(modifier: Modifier = Modifier) {
    val viewModel: HistoryViewModel = koinViewModel()
    val sources by viewModel.selectedSources.collectAsState()
    val pagedItems = viewModel.pagedHistory.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val availableSources = HistorySource.values().toList()
    val visibleSources = availableSources.filter { it != HistorySource.UNKNOWN }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { contentPadding ->
        Column(modifier = modifier.fillMaxSize().padding(contentPadding).padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = viewModel::clearFilters) {
                    Text("All Sources")
                }

                visibleSources.forEach { source ->
                    val selected = sources.contains(source)
                    FilterChip(
                        selected = selected,
                        onClick = {
                            val next = if (selected) {
                                sources - source
                            } else {
                                sources + source
                            }
                            viewModel.setSources(next)
                        },
                        label = { Text(source.name) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pagedItems.itemCount) { index ->
                    val item = pagedItems[index]
                    if (item == null) return@items
                    HistoryItemCard(
                        item = item,
                        onAction = {
                            scope.launch {
                                if (item.threadId.isBlank()) {
                                    snackbarHostState.showSnackbar("No actionable thread id")
                                    return@launch
                                }
                                val intent = when (item.source) {
                                    HistorySource.CALL -> Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${item.threadId}")
                                    }
                                    HistorySource.EMAIL -> Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:${item.threadId}")
                                    }
                                    else -> Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("sms:${item.threadId}")
                                    }
                                }
                                runCatching { context.startActivity(intent) }
                                    .onFailure { error ->
                                        snackbarHostState.showSnackbar(error.message.orEmpty())
                                    }
                            }
                        }
                    )
                }
                item {
                    when (pagedItems.loadState.refresh) {
                        is LoadState.Loading -> {
                            Text("Loading history…", modifier = Modifier.padding(8.dp))
                        }
                        is LoadState.Error -> {
                            Text("Failed to load history", modifier = Modifier.padding(8.dp))
                        }
                        is LoadState.NotLoading -> {
                            if (pagedItems.itemCount == 0) {
                                Text("No history found", modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                }
                item {
                    when (pagedItems.loadState.append) {
                        is LoadState.Loading -> Text("Loading more…", modifier = Modifier.padding(8.dp))
                        is LoadState.Error -> Text("Load failed", modifier = Modifier.padding(8.dp))
                        else -> Spacer(Modifier.height(1.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(item: UnifiedHistoryItem, onAction: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "${item.source.name} · ${item.direction.name}",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    item.snippet.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onAction) {
                Text("Open")
            }
        }
    }
}
