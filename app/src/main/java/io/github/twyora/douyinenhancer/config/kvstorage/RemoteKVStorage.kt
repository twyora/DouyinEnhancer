package io.github.twyora.douyinenhancer.config.kvstorage

import android.content.Context
import com.highcapable.yukihookapi.hook.factory.preferences
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.xposed.preference.YukiHookPreferences
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.all
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class RemoteKVStorage(override val name: String, val preference: YukiHookPreferences) : IKVStorage {
    private val keyFlows = ConcurrentHashMap<String, MutableStateFlow<Any?>>()

    private fun <T : Any> flowFor(key: String, defValue: T): MutableStateFlow<Any?> = keyFlows.getOrPut(key) {
        MutableStateFlow(
            if (preference.contains(key)) {
                get(key, defValue)
            } else {
                null
            }
        )
    }

    override fun <T : Any> get(key: String, defValue: T): T {
        @Suppress("UNCHECKED_CAST")
        return when (defValue) {
            is Int -> preference.getInt(key, defValue)

            is Long -> preference.getLong(key, defValue)

            is Boolean -> preference.getBoolean(key, defValue)

            is Float -> preference.getFloat(key, defValue)

            is String -> preference.getString(key, defValue)

            is Set<*> -> {
                require(
                    defValue.all { element ->
                        element is String
                    }
                ) {
                    "Set elements must all be String"
                }
                preference.getStringSet(key, defValue as Set<String>)
            }

            else -> throw IllegalArgumentException("unsupported type: ${defValue::class.qualifiedName}")
        } as T
    }

    @Suppress("UNCHECKED_CAST")
    override fun getAll(): Map<String, Any> = preference.all() as Map<String, Any>

    private fun <T : Any> doPut(editor: YukiHookPreferences.Editor, key: String, value: T) {
        @Suppress("UNCHECKED_CAST")
        when (value) {
            is Int -> editor.putInt(key, value)

            is Long -> editor.putLong(key, value)

            is Boolean -> editor.putBoolean(key, value)

            is Float -> editor.putFloat(key, value)

            is String -> editor.putString(key, value)

            is Set<*> -> {
                require(
                    value.all { element ->
                        element is String
                    }
                ) {
                    "Set elements must all be String"
                }
                editor.putStringSet(key, (java.util.HashSet(value)) as Set<String>)
            }

            else -> throw IllegalArgumentException("unsupported type: ${value::class.qualifiedName}")
        }
    }

    override fun <T : Any> put(key: String, value: T) {
        if (!preference.isWritable) {
            YLog.warn("$TAG: [$name] is not writable, write ignored (key=$key)")
            return
        }

        preference.edit().let {
            doPut(it, key, value)
            it.apply()
        }

        flowFor(key, value).tryEmit(value)
    }

    override fun putAll(values: Map<String, Any>) {
        if (!preference.isWritable) {
            YLog.warn("$TAG: [$name] is not writable, writes ignored (count=${values.size})")
            return
        }

        preference.edit().let {
            values.forEach { (key, value) ->
                doPut(it, key, value)
            }
            it.apply()
        }

        values.forEach { (key, value) ->
            flowFor(key, value).tryEmit(value)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> observe(key: String, defValue: T): Flow<T> = flow {
        emit(get(key, defValue))
        emitAll(
            flowFor(key, defValue).drop(1).map {
                (it as T?) ?: defValue
            }
        )
    }

    override fun clear() {
        if (!preference.isWritable) {
            YLog.warn("$TAG: [$name] is not writable, reset ignored")
            return
        }

        preference.edit {
            clear()
        }
        keyFlows.values.forEach {
            it.tryEmit(null)
        }
    }

    override fun close() {
        YLog.warn("$TAG: preference has no underlying closeable resource")
        keyFlows.clear()
    }

    companion object : IKVStorage.IKVFactory {
        private val TAG = this::class.simpleName

        override fun open(context: Context, name: String) = RemoteKVStorage(
            name,
            context.preferences(name)
        )

        override fun open(path: String, name: String) = throw IllegalStateException(
            "RemotePreference is managed by Xposed Framework and cannot be accessed directly" +
                " by the module itself. Use the Context-based overload"
        )
    }
}
