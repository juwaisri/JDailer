package com.jdailer.feature.contacts.domain.model

data class UnifiedContact(
    val contactId: Long,
    val displayName: String,
    val numbers: List<String>,
    val notes: String? = null,
    val tags: List<String> = emptyList(),
)
