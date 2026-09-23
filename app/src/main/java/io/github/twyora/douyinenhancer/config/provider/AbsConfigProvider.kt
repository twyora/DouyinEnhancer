package io.github.twyora.douyinenhancer.config.provider

import io.github.twyora.douyinenhancer.config.gate.ConfigStateMode
import io.github.twyora.douyinenhancer.config.gate.ConfigValueMode
import io.github.twyora.douyinenhancer.config.gate.RuleGate
import io.github.twyora.douyinenhancer.config.kvstorage.IKVStorage
import io.github.twyora.douyinenhancer.config.rule.AlwaysTrueRule
import io.github.twyora.douyinenhancer.config.rule.IRule

abstract class AbsConfigProvider(
    val kvConfig: IKVStorage,
    val ruleContextProvider: () -> IRule.Context = {
        // not restricted by default
        IRule.Context(hiddenFeatureEnabled = true)
    }
) {
    protected val configItemsInternal = mutableListOf<ConfigItem<*>>()

    protected fun <T : Any> configItem(
        key: String,
        defValue: T,
        gate: RuleGate = RuleGate(
            AlwaysTrueRule,
            ConfigValueMode.FORCE_DEFAULT,
            ConfigStateMode.NORMAL
        )
    ) = ConfigItem(kvConfig, key, defValue, gate, ruleContextProvider).also {
        configItemsInternal.add(it)
    }

    val allConfigItems: List<ConfigItem<*>> get() = configItemsInternal
}
