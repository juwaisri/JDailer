package com.jdailer.feature.messages.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MessageAttachmentRow(
    attachmentUri: String,
    onOpenAttachment: (String) -> Unit,
    removable: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenAttachment(attachmentUri) }
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = attachmentUri.substringAfterLast('/'),
                modifier = Modifier.weight(1f)
            )
            if (removable) {
                ElevatedButton(
                    onClick = { onOpenAttachment(attachmentUri) },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Remove",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
