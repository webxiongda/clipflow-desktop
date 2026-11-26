package com.clipflow.utils

interface Loader<T, R> {

    suspend fun load(value: T): R?
}
