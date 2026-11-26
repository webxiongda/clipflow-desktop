package com.clipflow.utils

import kotlinx.coroutines.CoroutineDispatcher

expect val ioDispatcher: CoroutineDispatcher

expect val mainDispatcher: CoroutineDispatcher

expect val cpuDispatcher: CoroutineDispatcher
