package com.jdailer.core.common.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

data class DispatcherProvider(
    val io: CoroutineDispatcher = Dispatchers.IO,
    val default: CoroutineDispatcher = Dispatchers.Default,
    val main: CoroutineDispatcher = Dispatchers.Main,
    val mainImmediate: CoroutineDispatcher = Dispatchers.Main.immediate,
)
