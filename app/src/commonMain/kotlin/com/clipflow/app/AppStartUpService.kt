package com.clipflow.app

interface AppStartUpService {

    fun followConfig()

    fun isAutoStartUp(): Boolean

    fun makeAutoStartUp()

    fun removeAutoStartUp()
}
