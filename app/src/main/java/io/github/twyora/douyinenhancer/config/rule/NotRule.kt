package io.github.twyora.douyinenhancer.config.rule

class NotRule(private val rule: IRule) : IRule {
    override fun evaluate(ruleContext: IRule.Context) = !rule.evaluate(ruleContext)
}

fun IRule.not() = NotRule(this)