package io.github.twyora.douyinenhancer.config.rule

import io.github.twyora.douyinenhancer.BuildConfig

object DebugRule : IRule {
    override fun evaluate(ruleContext: IRule.Context) = BuildConfig.DEBUG
}
