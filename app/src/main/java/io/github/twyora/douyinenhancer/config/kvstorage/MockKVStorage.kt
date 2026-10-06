package io.github.twyora.douyinenhancer.config.kvstorage

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow

class MockKVStorage : IKVStorage {
    private val store = mutableMapOf<String, Any>()
    private val changeFlow =
        MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun <T : Any> get(key: String, defValue: T): T {
        @Suppress("UNCHECKED_CAST")
        return (store[key] as? T) ?: defValue
    }

    override fun getAll(): Map<String, Any> = store

    override fun <T : Any> put(key: String, value: T) {
        store[key] = value
        changeFlow.tryEmit(Unit)
    }

    override fun putAll(values: Map<String, Any>) {
        store.putAll(values)
        changeFlow.tryEmit(Unit)
    }

    override fun <T : Any> observe(key: String, defValue: T): Flow<T> = flow {
        emit(get(key, defValue))
        changeFlow.collect {
            emit(get(key, defValue))
        }
    }

    override fun clear() {
        store.clear()
        changeFlow.tryEmit(Unit)
    }

    override fun close() {
        // nothing to release
    }

    companion object : IKVStorage.IKVFactory {
        override fun open(path: String, name: String): IKVStorage = MockKVStorage()
    }
}
