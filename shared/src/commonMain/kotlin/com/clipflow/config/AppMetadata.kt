package com.clipflow.config

import kotlinx.serialization.Serializable

@Serializable
data class AppMetadata(
    val appInstanceId: String,
)
