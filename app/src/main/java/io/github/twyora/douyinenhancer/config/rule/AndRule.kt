package io.github.twyora.douyinenhancer.config.rule

class AndRule(private val left: IRule, private val right: IRule) : IRule {
    override fun evaluate(ruleContext: IRule.Context) = right.evaluate(ruleContext) && left.evaluate(ruleContext)
}

infix fun IRule.and(other: IRule) = AndRule(this, other)
