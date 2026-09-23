package io.github.twyora.douyinenhancer.config.rule

interface IRule {
    data class Context(val hiddenFeatureEnabled: Boolean)

    fun evaluate(ruleContext: Context): Boolean
}
