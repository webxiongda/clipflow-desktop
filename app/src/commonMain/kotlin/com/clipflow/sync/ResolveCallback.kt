package com.clipflow.sync

import com.clipflow.net.VersionRelation

data class ResolveCallback(
    val updateVersionRelation: (VersionRelation) -> Unit,
    val markPollFailure: suspend () -> Unit = {},
    val onComplete: () -> Unit = {},
)
