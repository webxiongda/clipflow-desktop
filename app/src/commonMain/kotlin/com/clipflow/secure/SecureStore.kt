package com.clipflow.secure

interface SecureStore {

    val secureKeyPair: SecureKeyPair

    suspend fun saveCryptPublicKey(
        appInstanceId: String,
        cryptPublicKey: ByteArray,
    )

    suspend fun existCryptPublicKey(appInstanceId: String): Boolean

    suspend fun deleteCryptPublicKey(appInstanceId: String)

    suspend fun getMessageProcessor(appInstanceId: String): SecureMessageProcessor
}
