package io.github.twyora.douyinenhancer.config.provider

import io.github.twyora.douyinenhancer.config.gate.ConfigValueMode
import io.github.twyora.douyinenhancer.config.gate.ConfigStateMode
import io.github.twyora.douyinenhancer.config.gate.RuleGate
import io.github.twyora.douyinenhancer.config.kvstorage.IKVStorage
import io.github.twyora.douyinenhancer.config.rule.AlwaysTrueRule
import io.github.twyora.douyinenhancer.config.rule.IRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.reflect.KProperty

class ConfigItem<T : Any>(
    val storage: IKVStorage,
    val key: String,
    val defValue: T,
    val ruleGate: RuleGate = RuleGate(AlwaysTrueRule, ConfigValueMode.FORCE_DEFAULT, ConfigStateMode.NORMAL),
    private val ruleContextProvider: () -> IRule.Context = {
        IRule.Context(hiddenFeatureEnabled = true)
    },
) {
    private fun effectiveValue(raw: T) = if (ruleGate.rule.evaluate(ruleContextProvider())) {
        raw
    } else when (ruleGate.onMismatchValueMode) {
        ConfigValueMode.KEEP_STORED -> raw
        ConfigValueMode.FORCE_DEFAULT -> defValue
    }

    var value
        set(v) = storage.put(key, v)
        get() = effectiveValue(storage.get(key, defValue))

    val status
        get() = if (ruleGate.rule.evaluate(ruleContextProvider())) {
            ConfigStateMode.NORMAL
        } else {
            ruleGate.onMismatchStateMode
        }

    fun observe(): Flow<T> = storage.observe(key, defValue).map { raw ->
        effectiveValue(raw)
    }
}

operator fun <T : Any> ConfigItem<T>.getValue(thisRef: Any?, property: KProperty<*>) = value

operator fun <T : Any> ConfigItem<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    this.value = value
}