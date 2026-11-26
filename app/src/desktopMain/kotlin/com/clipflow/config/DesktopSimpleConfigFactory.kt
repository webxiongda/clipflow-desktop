package com.clipflow.config

import com.clipflow.app.AppFileType
import com.clipflow.path.AppPathProvider
import com.clipflow.presist.FilePersist

class DesktopSimpleConfigFactory(
    private val appPathProvider: AppPathProvider,
) : SimpleConfigFactory {

    override fun createConfig(configName: String): SimpleConfig {
        val oneFilePersist =
            FilePersist.createOneFilePersist(
                appPathProvider.resolve(configName, AppFileType.USER),
            )
        return DesktopSimpleConfig(oneFilePersist)
    }
}
