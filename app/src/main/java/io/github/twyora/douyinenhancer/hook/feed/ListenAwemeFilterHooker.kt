package io.github.twyora.douyinenhancer.hook.feed

import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.getField
import io.github.twyora.douyinenhancer.utils.resolveMethod

@HookOnMainProcess
object ListenAwemeFilterHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !ConfigManager.moduleConfig.verboseDisabled

    override fun onHook() {
        if (!ConfigManager.feedConfig.bypassListenAwemeRestriction) {
            if (verbose) {
                YLog.debug("$TAG: bypass listen aweme restriction is disabled, skipping hook")
            }
            return
        }
        installBypassListenAwemeFilterHook()
    }

    private fun installBypassListenAwemeFilterHook(): YukiMemberHookCreator.MemberHookCreator.Result? =
        packageInstance.listenAwemeFilter.selfClass?.resolveMethod(
            packageInstance.listenAwemeFilter.accept()
        )?.hook {
            after {
                if (result == true) {
                    return@after
                }

                if (verbose) {
                    val aweme = args[0]
                    val awemeId = aweme?.getField<String>(
                        packageInstance.aweme.aid()
                    )
                    YLog.debug("$TAG: bypassing listen aweme filter for aweme id: $awemeId")
                }
                resultTrue()
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to bypass aweme in filter", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook for bypassing listen aweme filter", throwable)
            }
        }
}
