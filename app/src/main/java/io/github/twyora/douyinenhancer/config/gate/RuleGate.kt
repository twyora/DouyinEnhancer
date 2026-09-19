package io.github.twyora.douyinenhancer.config.gate

import io.github.twyora.douyinenhancer.config.rule.IRule

data class RuleGate(val rule: IRule, val onMismatchValueMode: ConfigValueMode, val onMismatchStateMode: ConfigStateMode)
