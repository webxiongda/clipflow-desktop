package com.clipflow.utils

actual fun getCodecsUtils(): CodecsUtils = NativeCoreCodecsUtils

object NativeCoreCodecsUtils : CodecsUtils
