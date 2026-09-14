package io.github.twyora.douyinenhancer.config

import kotlinx.coroutines.flow.Flow
import java.io.File

interface KVStorage {
    fun <T : Any> get(key: String, defValue: T): T

    fun getAll(): Map<String, Any>

    fun <T : Any> put(key: String, value: T)

    fun putAll(values: Map<String, Any>)

    fun <T : Any> observe(key: String, defValue: T): Flow<T>

    fun clear()

    fun close()

    interface KVFactory {
        fun open(path: String, name: String): KVStorage

        fun open(path: String): KVStorage {
            val file = File(path)
            return open(file.parent ?: ".", file.name)
        }
    }
}
