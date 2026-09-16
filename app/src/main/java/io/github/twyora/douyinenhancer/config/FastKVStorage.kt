package io.github.twyora.douyinenhancer.config

import android.content.SharedPreferences
import io.fastkv.FastKV
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FastKVStorage(val fastKV: FastKV) : IKVStorage {
    override fun <T : Any> get(key: String, defValue: T): T {
        @Suppress("UNCHECKED_CAST")
        return when (defValue) {
            is Int -> fastKV.getInt(key, defValue)
            is Long -> fastKV.getLong(key, defValue)
            is Boolean -> fastKV.getBoolean(key, defValue)
            is Float -> fastKV.getFloat(key, defValue)
            is Double -> fastKV.getDouble(key, defValue)
            is String -> fastKV.getString(key, defValue)
            is ByteArray -> fastKV.getArray(key, defValue)
            is Set<*> -> fastKV.getStringSet(key, defValue as Set<String>)
            else -> throw IllegalArgumentException("Unsupported type: ${defValue::class.qualifiedName}")
        } as T
    }

    override fun getAll(): Map<String, Any> = fastKV.all

    override fun <T : Any> put(key: String, value: T) {
        @Suppress("UNCHECKED_CAST")
        when (value) {
            is Int -> fastKV.putInt(key, value)
            is Long -> fastKV.putLong(key, value)
            is Boolean -> fastKV.putBoolean(key, value)
            is Float -> fastKV.putFloat(key, value)
            is Double -> fastKV.putDouble(key, value)
            is String -> fastKV.putString(key, value)
            is ByteArray -> fastKV.putArray(key, value)
            is Set<*> -> fastKV.putStringSet(key, value as Set<String>)
            else -> throw IllegalArgumentException("Unsupported type: ${value::class.qualifiedName}")
        }
    }

    override fun putAll(values: Map<String, Any>) {
        fastKV.putAll(values)
    }

    override fun <T : Any> observe(key: String, defValue: T): Flow<T> {
        return callbackFlow {
            trySend(get(key, defValue))

            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
                if (changedKey == null || changedKey == key) {
                    trySend(get(key, defValue))
                }
            }

            fastKV.registerOnSharedPreferenceChangeListener(listener)
            awaitClose {
                fastKV.unregisterOnSharedPreferenceChangeListener(listener)
            }
        }
    }

    override fun clear() {
        fastKV.clear()
    }

    override fun close() {
        fastKV.close()
    }

    companion object : IKVStorage.KVFactory {
        override fun open(path: String, name: String) = FastKVStorage(
            FastKV.Builder(path, name).build()
        )
    }
}
