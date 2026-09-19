package io.github.twyora.douyinenhancer.config.rule

class OrRule(private val left: IRule, private val right: IRule) : IRule {
    override fun evaluate(ruleContext: IRule.Context) = left.evaluate(ruleContext) || right.evaluate(ruleContext)
}

infix fun IRule.or(other: IRule) = OrRule(this, other)