package com.jdailer.presentation.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.jdailer.feature.integrations.base.CommunicationAdapterRegistry
import com.jdailer.feature.integrations.router.CommunicationHubRouter
import com.jdailer.presentation.contacts.ContactsScreen
import com.jdailer.presentation.dialer.DialerScreen
import com.jdailer.presentation.history.UnifiedHistoryScreen
import com.jdailer.presentation.settings.SettingsScreen
import org.koin.android.ext.android.inject

private enum class HubTab(
    val label: String,
    val icon: ImageVector
) {
    Dialer("Dialer", Icons.Filled.Call),
    History("History", Icons.Filled.List),
    Contacts("Contacts", Icons.Filled.Person),
    Settings("Settings", Icons.Filled.Settings)
}

class MainActivity : ComponentActivity() {
    private val adapterRegistry: CommunicationAdapterRegistry by inject()
    private val communicationHubRouter: CommunicationHubRouter by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var selectedTab by rememberSaveable { mutableStateOf(HubTab.Dialer) }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            HubTab.values().forEach { tab ->
                                NavigationBarItem(
                                    selected = selectedTab == tab,
                                    onClick = { selectedTab = tab },
                                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                                    label = { Text(tab.label) }
                                )
                            }
                        }
                    }
                ) { contentPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
                        when (selectedTab) {
                            HubTab.Dialer -> DialerScreen(
                                adapterRegistry = adapterRegistry,
                                communicationHubRouter = communicationHubRouter
                            )
                            HubTab.History -> UnifiedHistoryScreen()
                            HubTab.Contacts -> ContactsScreen(
                                communicationHubRouter = communicationHubRouter
                            )
                            HubTab.Settings -> SettingsScreen()
                        }
                    }
                }
            }
        }
    }
}
