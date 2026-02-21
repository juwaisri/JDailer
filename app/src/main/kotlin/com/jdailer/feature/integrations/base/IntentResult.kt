package com.jdailer.feature.integrations.base

sealed class IntentResult {
    data object Success : IntentResult()
    data class Unavailable(val reason: String) : IntentResult()
    data class Failure(val message: String, val throwable: Throwable? = null) : IntentResult()
}
