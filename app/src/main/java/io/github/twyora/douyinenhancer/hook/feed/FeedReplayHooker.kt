package io.github.twyora.douyinenhancer.hook.feed

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.getField
import io.github.twyora.douyinenhancer.utils.invokeMethodOnly
import io.github.twyora.douyinenhancer.utils.resolveMethod

@HookOnMainProcess
object FeedReplayHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !ConfigManager.module.verboseDisabled.value

    override fun onHook() {
        if (!ConfigManager.feed.blockAutoReplay.value) {
            if (verbose) {
                YLog.debug("$TAG: block auto replay is disabled, skipping hook")
            }
            return
        }

        // BaseListFragmentPanel has no member function onPlayCompleted,
        // but onVideoPlayerEvent will be called when video playback completes with a specific video event code.
        // I have no other good idea; pause the video manually here
        packageInstance.baseListFragmentPanel.selfClass?.resolveMethod(
            packageInstance.baseListFragmentPanel.onVideoPlayerEvent()
        )?.hook {
            after {
                val status = args[0] ?: run {
                    YLog.error("$TAG: video player event is null")
                    return@after
                }
                val code = status.getField<Int>(
                    packageInstance.videoPlayerStatus.code()
                ) ?: run {
                    YLog.error("$TAG: video player event code is null")
                    return@after
                }

                if (code != DouyinPackage.VideoPlayerStatusModule.EVENT_PLAY_COMPLETED) {
                    return@after
                } else if (verbose) {
                    YLog.debug("$TAG: pause when feed playback completes")
                }
                instance.invokeMethodOnly(
                    packageInstance.baseListFragmentPanel.handlePause(),
                    true
                )
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to intercept feed auto-loop on playback completion", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook feed auto-loop on playback completion", throwable)
            }
        }
    }
}
