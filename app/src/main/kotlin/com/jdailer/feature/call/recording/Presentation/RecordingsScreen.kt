package com.jdailer.feature.call.recording.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@Composable
fun RecordingsScreen(modifier: Modifier = Modifier) {
    val viewModel: RecordingsListViewModel = koinViewModel()
    val recordings by viewModel.recordings.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(onClick = viewModel::clearRecordings) {
            Text("Clear recordings")
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(recordings) { recording ->
                if (recording != null) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(recording.recordingId, style = MaterialTheme.typography.labelSmall)
                            Text("Created: ${recording.timestamp}")
                            Text("Duration: ${recording.durationMs}ms")
                        }
                    }
                }
            }
        }
    }
}
