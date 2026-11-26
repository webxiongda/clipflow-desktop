package com.clipflow.secure

import com.clipflow.app.AppFileType
import com.clipflow.db.secure.SecureIO
import com.clipflow.path.AppPathProvider
import com.clipflow.platform.windows.WindowDapiHelper
import com.clipflow.presist.FilePersist
import com.clipflow.utils.CryptographyUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking

class WindowsSecureStoreFactory(
    appPathProvider: AppPathProvider,
    private val secureKeyPairSerializer: SecureKeyPairSerializer,
    private val secureIO: SecureIO,
) : SecureStoreFactory {
    private val logger = KotlinLogging.logger {}

    private val filePersist =
        FilePersist.createOneFilePersist(
            appPathProvider.resolve("secure.data", AppFileType.ENCRYPT),
        )

    override fun createSecureStore(): SecureStore =
        runBlocking {
            val file = filePersist.path.toFile()
            if (file.exists()) {
                logger.info { "Found secureKeyPair encrypt file" }
                filePersist.readBytes()?.let {
                    runCatching {
                        val decryptData = WindowDapiHelper.decryptData(it)
                        decryptData?.let { byteArray ->
                            val secureKeyPair = secureKeyPairSerializer.decodeSecureKeyPair(byteArray)
                            return@runBlocking GeneralSecureStore(
                                secureKeyPair,
                                secureKeyPairSerializer,
                                secureIO,
                            )
                        }
                    }.onFailure { e ->
                        logger.error(e) { "Failed to decrypt secureKeyPair" }
                    }
                }
                if (file.delete()) {
                    logger.info { "Delete secureKeyPair encrypt file" }
                }
            } else {
                logger.info { "Not found secureKeyPair encrypt file" }
            }

            logger.info { "Generate secureKeyPair" }
            val secureKeyPair = CryptographyUtils.generateSecureKeyPair()
            val data = secureKeyPairSerializer.encodeSecureKeyPair(secureKeyPair)
            val encryptData =
                WindowDapiHelper.encryptData(data)
                    ?: throw IllegalStateException("DPAPI encryption failed — cannot persist secure key pair")
            filePersist.saveBytes(encryptData)
            GeneralSecureStore(secureKeyPair, secureKeyPairSerializer, secureIO)
        }
}
