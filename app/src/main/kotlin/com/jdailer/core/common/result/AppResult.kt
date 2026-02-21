package com.jdailer.core.common.result

sealed class AppResult<out T> {
    data class Success<T>(
        val value: T
    ) : AppResult<T>()

    data class Error(
        val throwable: Throwable,
        val message: String = throwable.message.orEmpty()
    ) : AppResult<Nothing>()

    data object Loading : AppResult<Nothing>()

    data object Idle : AppResult<Nothing>()
}
