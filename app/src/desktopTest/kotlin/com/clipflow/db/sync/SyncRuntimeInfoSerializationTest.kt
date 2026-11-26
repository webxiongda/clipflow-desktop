package com.clipflow.db.sync

import com.clipflow.sync.SyncTestFixtures.createSyncRuntimeInfo
import com.clipflow.utils.getJsonUtils
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals

class SyncRuntimeInfoSerializationTest {

    @Test
    fun `serialized field set remains compatible with the legacy runtime schema`() {
        val json = getJsonUtils().JSON
        val encoded = json.parseToJsonElement(json.encodeToString(createSyncRuntimeInfo())).jsonObject

        assertEquals(
            setOf(
                "appInstanceId",
                "appVersion",
                "userName",
                "deviceId",
                "deviceName",
                "platform",
                "hostInfoList",
                "port",
                "noteName",
                "connectNetworkPrefixLength",
                "connectHostAddress",
                "connectState",
                "allowSend",
                "allowReceive",
                "createTime",
                "modifyTime",
            ),
            encoded.keys,
        )
    }
}
