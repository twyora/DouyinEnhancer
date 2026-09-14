package io.github.twyora.douyinenhancer.config

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

enum class FeatureGate {
    HIDDEN
}

// TODO: Rename it
abstract class ConfigProvider(
    val kvConfig: KVStorage,
    protected open val gates: Map<FeatureGate, () -> Boolean> = emptyMap()
) : KVStorage by kvConfig {
    // TODO: refactoring is required
    open val gatedKeys: Map<FeatureGate, Set<String>> = emptyMap()

    // TODO: ReadWriteConfig, GatedRead&WriteConfig
    protected fun <T : Any> property(key: String, defValue: T): ReadWriteProperty<Any, T> {
        return object : ReadWriteProperty<Any, T> {
            private val applicableGate by lazy {
                gatedKeys.entries.firstNotNullOfOrNull { (candidateGate, gatedKeySet) ->
                    if (key in gatedKeySet) {
                        candidateGate
                    } else {
                        null
                    }
                }
            }

            override fun getValue(thisRef: Any, property: KProperty<*>) =
                if (applicableGate != null && gates[applicableGate]?.invoke() != true) {
                    defValue
                } else {
                    get(key, defValue)
                }

            override fun setValue(thisRef: Any, property: KProperty<*>, value: T) = put(key, value)
        }
    }
}
