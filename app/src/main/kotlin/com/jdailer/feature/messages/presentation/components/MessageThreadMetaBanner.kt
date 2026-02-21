package com.jdailer.feature.messages.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jdailer.core.database.enums.HistorySource
import com.jdailer.feature.messages.domain.model.MessageThreadMetadata

@Composable
fun MessageThreadMetaBanner(
    source: HistorySource,
    metadata: MessageThreadMetadata,
    isPinned: Boolean,
    unreadCount: Int,
    modifier: Modifier = Modifier
) {
    val badgeStyle = MaterialTheme.typography.labelSmall

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FilterChip(
            selected = isPinned,
            onClick = {},
            label = { Text("Pinned", style = badgeStyle) }
        )
        FilterChip(
            selected = false,
            onClick = {},
            label = { Text(source.name, style = badgeStyle) }
        )

        if (metadata.isRcs) {
            FilterChip(
                selected = false,
                onClick = {},
                label = { Text("RCS", style = badgeStyle) }
            )
        }

        if (metadata.hasMedia) {
            FilterChip(
                selected = false,
                onClick = {},
                label = {
                    Text(
                        "${metadata.attachmentCount} media",
                        style = badgeStyle
                    )
                }
            )
        }

        if (unreadCount > 0) {
            ElevatedButton(onClick = {}) {
                Text("Unread $unreadCount")
            }
        } else {
            Spacer(Modifier.width(0.dp))
        }
    }
}
