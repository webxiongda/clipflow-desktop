package com.clipflow.sync

import com.clipflow.app.AppInfo
import com.clipflow.app.EndpointInfoFactory
import com.clipflow.image.DesktopQRCodeImage
import com.clipflow.image.PlatformImage
import com.clipflow.net.NetworkInterfaceService

class DesktopQRCodeGenerator(
    appInfo: AppInfo,
    endpointInfoFactory: EndpointInfoFactory,
    networkInterfaceService: NetworkInterfaceService,
) : QRCodeGenerator(appInfo, endpointInfoFactory, networkInterfaceService) {

    override suspend fun generateQRCode(token: CharArray): PlatformImage =
        DesktopQRCodeImage(
            data = buildQRCode(token).encodeToByteArray(),
        )
}
