package com.jdailer.feature.quickactions.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jdailer.feature.quickactions.domain.model.QuickActionUiModel

@Composable
fun AdvancedQuickActionStrip(
    actions: List<QuickActionUiModel>,
    onAction: (QuickActionUiModel) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        actions
            .filter { it.enabled }
            .forEach { action ->
                FilterChip(
                    selected = false,
                    onClick = { onAction(action) },
                    label = { Text(action.label) }
                )
            }
    }
}
