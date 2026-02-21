package com.jdailer.feature.messages.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jdailer.feature.messages.domain.model.MessageDeliveryDecision
import com.jdailer.feature.messages.domain.model.MessageTransportMode
import com.jdailer.feature.messages.domain.model.RcsCapability

@Composable
fun MessageTransportBanner(
    decision: MessageDeliveryDecision?,
    rcsCapability: RcsCapability?
) {
    if (decision == null && rcsCapability == null) return

    val modeText = when (decision?.mode) {
        MessageTransportMode.RCS -> "RCS"
        MessageTransportMode.MMS -> "MMS"
        MessageTransportMode.SMS -> "SMS"
        null -> "Unknown"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Routing: $modeText",
                style = MaterialTheme.typography.titleSmall
            )
            if (!decision?.reason.isNullOrBlank()) {
                Text(
                    text = "Reason: ${decision?.reason.orEmpty()}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (rcsCapability != null) {
                val rcsStatus = if (rcsCapability.isRcsEnabledByDefaultSmsApp) "enabled" else "disabled"
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "RCS: $rcsStatus", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "Carrier RCS: ${if (rcsCapability.isRcsAvailableAsCarrierService) "yes" else "no"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
