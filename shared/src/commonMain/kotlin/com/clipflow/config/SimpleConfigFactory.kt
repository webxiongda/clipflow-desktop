package com.clipflow.config

interface SimpleConfigFactory {

    fun createConfig(configName: String): SimpleConfig
}
