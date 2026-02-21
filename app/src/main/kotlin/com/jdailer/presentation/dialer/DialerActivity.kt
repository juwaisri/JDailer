package com.jdailer.presentation.dialer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.jdailer.feature.integrations.base.CommunicationAdapterRegistry
import com.jdailer.feature.integrations.router.CommunicationHubRouter
import org.koin.android.ext.android.inject

class DialerActivity : ComponentActivity() {
    private val adapterRegistry: CommunicationAdapterRegistry by inject()
    private val communicationHubRouter: CommunicationHubRouter by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    DialerScreen(
                        adapterRegistry = adapterRegistry,
                        communicationHubRouter = communicationHubRouter
                    )
                }
            }
        }
    }
}
