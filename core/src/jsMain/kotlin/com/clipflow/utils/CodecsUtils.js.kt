package com.clipflow.utils

actual fun getCodecsUtils(): CodecsUtils = JsCodecsUtils

object JsCodecsUtils : CodecsUtils
