package com.jdailer.feature.quickactions.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.jdailer.feature.contacts.domain.model.UnifiedContact
import com.jdailer.feature.quickactions.domain.model.SmartAction
import com.jdailer.feature.quickactions.domain.model.SmartActionType

@Composable
fun SmartContactCard(
    contact: UnifiedContact,
    actions: List<SmartAction>,
    tags: List<String> = emptyList(),
    onAction: (SmartActionType) -> Unit
) {
    var drag by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(drag) { }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, amount ->
                        drag += amount
                    },
                    onDragEnd = {
                        if (drag > 240f) {
                            actions.firstOrNull { it.type == SmartActionType.CALL }?.let {
                                onAction(it.type)
                            }
                        } else if (drag < -240f) {
                            actions.firstOrNull { it.type == SmartActionType.MESSAGE }?.let {
                                onAction(it.type)
                            }
                        }
                        drag = 0f
                    },
                    onDragCancel = { drag = 0f }
                )
            }
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = contact.displayName,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = contact.numbers.firstOrNull().orEmpty().ifBlank { "No phone number" },
                style = MaterialTheme.typography.bodyMedium
            )

            if (tags.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tags.forEach { tag ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text(tag) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                actions.forEach { action ->
                    val label = when (action.type) {
                        SmartActionType.CALL -> "Call"
                        SmartActionType.MESSAGE -> "Message"
                        SmartActionType.EMAIL -> "Email"
                        SmartActionType.APP -> "Open app"
                        SmartActionType.BLOCK -> "Block"
                        SmartActionType.NOTE -> "Note"
                        SmartActionType.SHARE -> "Share"
                    }
                    TextButton(onClick = { onAction(action.type) }) {
                        Text(label)
                    }
                }
            }
        }
    }
}
