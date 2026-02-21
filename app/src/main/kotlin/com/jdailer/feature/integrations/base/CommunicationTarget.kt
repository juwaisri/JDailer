package com.jdailer.feature.integrations.base

data class CommunicationTarget(
    val contactId: Long? = null,
    val contactName: String = "",
    val phoneNumber: String? = null,
    val emailAddress: String? = null,
    val mediaUris: List<String> = emptyList()
)
