package com.jdailer.feature.voip.domain.model

data class TelecomRoleState(
    val isDefaultDialer: Boolean,
    val hasRoleManager: Boolean,
    val hasSelfManagedConnectionService: Boolean
)
