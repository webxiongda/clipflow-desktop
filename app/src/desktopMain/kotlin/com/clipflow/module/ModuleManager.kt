package com.clipflow.module

class ModuleManager(
    private val modules: Map<String, ServiceModule>,
) {

    fun getModuleById(moduleId: String): ServiceModule? = modules[moduleId]
}
