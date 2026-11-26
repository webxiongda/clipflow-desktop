package com.clipflow.e2e.scenario

import com.clipflow.net.clientapi.SuccessResult
import com.clipflow.utils.HostAndPort
import com.clipflow.utils.buildUrl

class WrongTokenScenario : Scenario {
    override val name: String = "wrong-token"

    override suspend fun run(ctx: ScenarioContext): ScenarioResult {
        val target = resolveOrDiscover(ctx) ?: return ScenarioResult.Fail("Could not resolve target.")
        val targetAppId = target.appInstanceId ?: return ScenarioResult.Fail("Target has no appInstanceId.")

        val result =
            ctx.peer.syncClientApi.trust(
                targetAppInstanceId = targetAppId,
                host = target.host,
                token = WRONG_TOKEN,
                toUrl = { buildUrl(HostAndPort(target.host, target.port)) },
            )

        if (result is SuccessResult) {
            return ScenarioResult.Fail("Trust unexpectedly succeeded with wrong token $WRONG_TOKEN.")
        }
        if (ctx.peer.secureIO.existCryptPublicKey(targetAppId)) {
            return ScenarioResult.Fail("Target public key was stored despite trust failure.")
        }
        return ScenarioResult.Pass("Wrong token rejected; no key stored.")
    }

    companion object {
        const val WRONG_TOKEN: Int = 999999
    }
}
