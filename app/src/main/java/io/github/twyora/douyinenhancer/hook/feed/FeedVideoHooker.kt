package io.github.twyora.douyinenhancer.hook.feed

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.invokeMethod
import io.github.twyora.douyinenhancer.utils.resolveMethod

@HookOnMainProcess
object FeedVideoHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !ConfigManager.module.verboseDisabled.value

    override fun onHook() {
        if (!ConfigManager.save.feedVideoRemoveWatermark.value) {
            if (verbose) {
                YLog.debug("$TAG: remove watermark is disabled, skipping hook")
            }
            return
        }

        packageInstance.miscDownloadAddrUtil.selfClass?.resolveMethod(
            packageInstance.miscDownloadAddrUtil.getSuffixSceneDownloadAddr()
        )?.hook {
            before {
                val aweme = args[0] ?: return@before

                val playAddr = aweme.invokeMethod<Any>(
                    packageInstance.aweme.getVideo()
                )?.invokeMethod<Any>(
                    packageInstance.video.getPlayAddr()
                )
                if (playAddr != null) {
                    if (verbose) {
                        YLog.debug("$TAG: play addr present, override result with play addr")
                    }
                    result = playAddr
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to override download addr with play addr", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook for video using play addr to download", throwable)
            }
        }
    }
}
