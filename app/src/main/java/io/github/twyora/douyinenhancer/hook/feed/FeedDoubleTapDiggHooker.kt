package io.github.twyora.douyinenhancer.hook.feed

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.FeedKey
import io.github.twyora.douyinenhancer.config.key.ModuleKey
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.resolveMethod

@HookOnMainProcess
object FeedDoubleTapDiggHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !FastKVConfigManager.module.getBoolean(ModuleKey.DISABLE_VERBOSE_LOGS, false)

    override fun onHook() {
        if (!FastKVConfigManager.settings.getBoolean(FeedKey.FEED_DOUBLE_TAP_DIGG, false)) {
            if (verbose) {
                YLog.debug("$TAG: double-tap digg interception is disabled, skipping hook")
            }
            return
        }

        packageInstance.baseListFragmentPanel.selfClass?.resolveMethod(
            packageInstance.baseListFragmentPanel.handleBigDiggViewClick()
        )?.hook {
            before {
                if (verbose) {
                    YLog.debug("$TAG: intercepting double-tap digg")
                }
                resultNull()
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to intercept double-tap digg", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook for intercepting double-tap digg", throwable)
            }
        }
    }
}
