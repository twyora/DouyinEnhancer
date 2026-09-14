package io.github.twyora.douyinenhancer.config

import kotlinx.coroutines.flow.Flow

interface KVStorage {
    fun <T : Any> get(key: String, defValue: T): T

    fun getAll(): Map<String, Any>

    fun <T : Any> put(key: String, value: T)

    fun putAll(values: Map<String, Any>)

    fun <T : Any> observe(key: String, defValue: T): Flow<T>

    fun clear()
}
