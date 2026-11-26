package com.clipflow.app

interface AppRestartService {

    fun restart(exitApplication: () -> Unit)
}
