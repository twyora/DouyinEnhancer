package io.github.twyora.douyinenhancer.hook.feed

import com.highcapable.yukihookapi.extension.kavaref.intercept
import com.highcapable.yukihookapi.hook.core.YukiHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.reflect.resolveMethodOrNull

/**
 * This hooker functionality is part of #211,
 *  which provides inherited playback speed across feed switches.
 */
@HookOnMainProcess
object FeedKeepInheritedSpeedHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !ConfigManager.module.verboseDisabled.value

    override fun onHook() {
        if (!ConfigManager.feed.keepInheritedSpeed.value) {
            YLog.info("$TAG: keep inherited speed is disabled, skipping hook")
            return
        }

        installForceSpeedInheritHook()
    }

    private fun installForceSpeedInheritHook(): YukiHookCreator.MemberHooker<*>.Handle? {
        // Why does ktlint convert block returns to expression bodies sometimes, but not always?
        return packageInstance.compilationSpeedInheritComponent.selfClass?.resolveMethodOrNull(
            packageInstance.compilationSpeedInheritComponent.needInheritSpeed()
        )?.intercept {
            val enabled = proceed<Boolean>()

            if (enabled != true) {
                if (verbose) {
                    YLog.debug("$TAG: inherited playback speed kept across feed switch")
                }
                true
            } else {
                enabled
            }
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to keep inherited playback speed across feed switch", throwable)
            }
        }
    }
}
