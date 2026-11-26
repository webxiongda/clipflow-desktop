package com.clipflow.dto.pull

import kotlinx.serialization.Serializable

@Serializable
data class PullFileRequest(
    val id: Long,
    val chunkIndex: Int,
) {

    override fun toString(): String = "PullFileRequest(id=$id, chunkIndex=$chunkIndex)"
}
