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
        // 若“双击打开评论区”已开启：评论 hooker 会自己作废双击点赞并打开评论区，
        // 这里再挂 resultNull 会先吞掉事件导致评论区打不开（两个 before 冲突），故跳过。
        if (FastKVConfigManager.settings.getBoolean(FeedKey.FEED_DOUBLE_TAP_OPEN_COMMENT, false)) {
            if (verbose) {
                YLog.debug("$TAG: double-tap open comment is on, digg-disable merged into it; skip separate hook")
            }
            return
        }
        if (!FastKVConfigManager.settings.getBoolean(FeedKey.FEED_DOUBLE_TAP_DIGG, false)) {
            if (verbose) {
                YLog.debug("$TAG: double-tap digg interception is disabled, skipping hook")
            }
            return
        }

        packageInstance.baseListFragmentPanel.selfClass?.resolveMethod(
            packageInstance.baseListFragmentPanel.handleDoubleClick()
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
