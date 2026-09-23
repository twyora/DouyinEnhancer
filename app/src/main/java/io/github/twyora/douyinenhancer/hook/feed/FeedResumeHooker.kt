package io.github.twyora.douyinenhancer.hook.feed

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.getField
import io.github.twyora.douyinenhancer.utils.resolveMethod

@HookOnMainProcess
object FeedResumeHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !ConfigManager.module.verboseDisabled.value

    override fun onHook() {
        if (!ConfigManager.feed.blockResumePlayback.value) {
            if (verbose) {
                YLog.debug("$TAG: block resume playback is disabled, skipping hook")
            }
            return
        }

        // I have another choice: intercept FeedPanelProxy.handleTextureAvailable,
        // (the name "FeedPanelProxy" is given by me; it is obfuscated by the host and owned by BaseListFragmentPanel.basePanelProxy)
        // instead of checking videoType using a magic number inside BaseListFragmentPanel.handleVideoEvent.
        // The reason I ultimately chose this strategy is that I cannot confirm whether FeedPanelProxy
        // will be removed or obfuscated into another name. If this functionality breaks,
        // maintaining extra hook points becomes an additional maintenance burden
        packageInstance.baseListFragmentPanel.selfClass?.resolveMethod(
            packageInstance.baseListFragmentPanel.handleVideoEvent()
        )?.hook {
            before {
                val videoType = args[0]?.getField<Int>(
                    packageInstance.videoEvent.type()
                ) ?: run {
                    YLog.error("$TAG: video type is null")
                    return@before
                }

                if (videoType != DouyinPackage.VideoEventModule.EVENT_TEXTURE_AVAILABLE) {
                    return@before
                } else if (verbose) {
                    YLog.debug("$TAG: intercepting resume of video playback on surface created")
                }
                resultNull()
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to intercept resume of video playback on surface created", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook for intercepting resume of video playback on surface created", throwable)
            }
        }
    }
}
