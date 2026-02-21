package com.jdailer.feature.voip.domain.model

data class SipProfile(
    val profileId: String = "default",
    val domain: String,
    val username: String,
    val password: String,
    val transport: String = "UDP",
    val port: Int = 5060,
    val outboundProxy: String? = null,
    val enabled: Boolean = false
)
