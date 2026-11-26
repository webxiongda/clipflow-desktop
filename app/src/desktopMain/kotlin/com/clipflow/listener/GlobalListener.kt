package com.clipflow.listener

interface GlobalListener {

    fun isRegistered(): Boolean

    fun start()

    fun stop()
}
