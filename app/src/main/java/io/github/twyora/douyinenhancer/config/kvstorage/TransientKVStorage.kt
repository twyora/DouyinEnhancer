package io.github.twyora.douyinenhancer.config.kvstorage

import android.content.Context
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class TransientKVStorage(override val name: String) : IKVStorage {
    private val data = ConcurrentHashMap<String, Any>()
    private val keyFlows = ConcurrentHashMap<String, MutableStateFlow<Any?>>()

    private fun flowFor(key: String): MutableStateFlow<Any?> = keyFlows.getOrPut(key) {
        MutableStateFlow(data[key])
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(key: String, defValue: T): T = (data[key] as? T) ?: defValue

    override fun getAll(): Map<String, Any> = data.toMap()

    override fun <T : Any> put(key: String, value: T) {
        data[key] = value
        flowFor(key).tryEmit(value)
    }

    override fun putAll(values: Map<String, Any>) {
        data.putAll(values)
        values.forEach { (k, v) ->
            flowFor(k).tryEmit(v)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> observe(key: String, defValue: T): Flow<T> =
        flowFor(key).map {
            (it as? T) ?: defValue
        }

    override fun clear() {
        data.clear()
        keyFlows.values.forEach {
            it.tryEmit(null)
        }
    }

    override fun close() {
        keyFlows.clear()
    }

    companion object : IKVStorage.IKVFactory {
        override fun open(context: Context, name: String): IKVStorage = TransientKVStorage(name)

        override fun open(path: String, name: String): IKVStorage = TransientKVStorage(name)
    }
}
