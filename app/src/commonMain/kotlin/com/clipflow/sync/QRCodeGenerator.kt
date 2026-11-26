package com.clipflow.sync

import com.clipflow.app.AppInfo
import com.clipflow.app.EndpointInfoFactory
import com.clipflow.dto.sync.SyncInfo
import com.clipflow.image.PlatformImage
import com.clipflow.net.NetworkInterfaceService
import com.clipflow.utils.getCodecsUtils
import com.clipflow.utils.getJsonUtils

abstract class QRCodeGenerator(
    private val appInfo: AppInfo,
    private val endpointInfoFactory: EndpointInfoFactory,
    private val networkInterfaceService: NetworkInterfaceService,
) {

    private val codecsUtils = getCodecsUtils()
    protected val jsonUtils = getJsonUtils()

    abstract suspend fun generateQRCode(token: CharArray): PlatformImage

    protected suspend fun buildQRCode(token: CharArray): String {
        val hostInfoList =
            networkInterfaceService
                .getCurrentUseNetworkInterfaces()
                .map { it.toHostInfo() }
        val endpointInfo = endpointInfoFactory.createEndpointInfo(hostInfoList)
        val syncInfo = SyncInfo(appInfo, endpointInfo)
        return buildQRCode(syncInfo, token)
    }

    private fun buildQRCode(
        syncInfo: SyncInfo,
        token: CharArray,
    ): String {
        val syncInfoJson = jsonUtils.JSON.encodeToString(syncInfo)
        val syncInfoBytes = syncInfoJson.encodeToByteArray()
        return encodeSyncInfo(syncInfoBytes, token.concatToString().toInt())
    }

    private fun encodeSyncInfo(
        syncInfoBytes: ByteArray,
        token: Int,
    ): String {
        val size = syncInfoBytes.size
        val offset = token % size
        val rotatedBytes = syncInfoBytes.rotate(offset)
        val saltedBytes = rotatedBytes + token.toByteArray()
        return codecsUtils.base64Encode(saltedBytes)
    }

    @Suppress("unused")
    protected fun decodeSyncInfo(encodedString: String): Pair<SyncInfo, Int> {
        val decodedBytes = codecsUtils.base64Decode(encodedString)

        if (decodedBytes.size < 4) {
            throw IllegalArgumentException("Decoded bytes too short to contain token")
        }

        val token =
            decodedBytes.takeLast(4).let { bytes ->
                (bytes[0].toInt() and 0xFF shl 24) or
                    (bytes[1].toInt() and 0xFF shl 16) or
                    (bytes[2].toInt() and 0xFF shl 8) or
                    (bytes[3].toInt() and 0xFF)
            }

        val syncInfoWithRotation = decodedBytes.dropLast(4).toByteArray()

        val size = syncInfoWithRotation.size
        val offset = token % size

        val originalBytes = syncInfoWithRotation.rotate((size - offset) % size)

        val syncInfo = jsonUtils.JSON.decodeFromString<SyncInfo>(originalBytes.decodeToString())

        return Pair(syncInfo, token)
    }

    private fun ByteArray.rotate(offset: Int): ByteArray {
        val effectiveOffset = offset % size
        return if (effectiveOffset == 0 || isEmpty()) {
            copyOf()
        } else {
            ByteArray(size) { i ->
                this[(i - effectiveOffset + size) % size]
            }
        }
    }

    private fun Int.toByteArray(): ByteArray =
        byteArrayOf(
            (this shr 24).toByte(),
            (this shr 16).toByte(),
            (this shr 8).toByte(),
            this.toByte(),
        )
}
