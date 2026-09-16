package io.github.twyora.douyinenhancer.config

import kotlinx.coroutines.flow.Flow
import java.io.File

interface IKVStorage {
    fun <T : Any> get(key: String, defValue: T): T

    fun getAll(): Map<String, Any>

    fun <T : Any> put(key: String, value: T)

    fun putAll(values: Map<String, Any>)

    fun <T : Any> observe(key: String, defValue: T): Flow<T>

    fun clear()

    fun close()

    interface KVFactory {
        fun open(path: String, name: String): IKVStorage

        fun open(path: String): IKVStorage {
            val file = File(path)
            return open(file.parent ?: ".", file.name)
        }
    }
}
