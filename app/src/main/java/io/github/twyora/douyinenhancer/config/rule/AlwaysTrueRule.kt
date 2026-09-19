package io.github.twyora.douyinenhancer.config.rule

object AlwaysTrueRule : IRule {
    override fun evaluate(ruleContext: IRule.Context) = true
}