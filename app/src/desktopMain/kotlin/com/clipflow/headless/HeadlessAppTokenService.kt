package com.clipflow.headless

import com.clipflow.app.AppTokenService
import io.github.oshai.kotlinlogging.KotlinLogging

class HeadlessAppTokenService : AppTokenService() {

    private val logger = KotlinLogging.logger {}

    override fun preShowToken() {
        logger.info { "Pairing token requested; run 'clipflow token' to read it" }
    }

    override fun preShowPairingCode() {
        logger.info { "Show pairing code requested" }
    }
}
