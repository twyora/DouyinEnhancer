package io.github.twyora.douyinenhancer.config.rule

object HiddenFeatureEnabledRule : IRule {
    override fun evaluate(ruleContext: IRule.Context) = ruleContext.hiddenFeatureEnabled
}