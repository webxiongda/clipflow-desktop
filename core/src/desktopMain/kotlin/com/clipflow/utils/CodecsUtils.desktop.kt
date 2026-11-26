package com.clipflow.utils

actual fun getCodecsUtils(): CodecsUtils = DesktopCoreCodecsUtils

object DesktopCoreCodecsUtils : CodecsUtils
