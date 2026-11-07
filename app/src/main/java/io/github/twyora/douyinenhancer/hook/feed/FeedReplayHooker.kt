package io.github.twyora.douyinenhancer.hook.feed

import com.highcapable.yukihookapi.extension.kavaref.intercept
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.reflect.getFieldOrNull
import io.github.twyora.douyinenhancer.utils.reflect.invokeMethodOnlyQuietly
import io.github.twyora.douyinenhancer.utils.reflect.resolveMethodOrNull

@HookOnMainProcess
object FeedReplayHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !ConfigManager.module.verboseDisabled.value

    override fun onHook() {
        if (!ConfigManager.feed.blockAutoReplay.value) {
            YLog.info("$TAG: block auto replay is disabled, skipping hook")
            return
        }

        // BaseListFragmentPanel has no member function onPlayCompleted,
        // but onVideoPlayerEvent will be called when video playback completes with a specific video event code.
        // I have no other good idea; pause the video manually here
        packageInstance.baseListFragmentPanel.selfClass?.resolveMethodOrNull(
            packageInstance.baseListFragmentPanel.onVideoPlayerEvent()
        )?.intercept {
            val result = proceed()

            val status = args.firstOrNull {
                packageInstance.videoPlayerStatus.selfClass?.isInstance(it) == true
            } ?: run {
                YLog.error("$TAG: video player status not found in argument list")
                return@intercept result
            }
            val code = status.getFieldOrNull<Int>(
                packageInstance.videoPlayerStatus.code()
            ) ?: run {
                YLog.error("$TAG: video player event code is null")
                return@intercept result
            }

            if (code != DouyinPackage.VideoPlayerStatusModule.EVENT_PLAY_COMPLETED) {
                return@intercept result
            } else if (verbose) {
                YLog.debug("$TAG: pause when feed playback completes")
            }
            instance.invokeMethodOnlyQuietly(
                packageInstance.baseListFragmentPanel.handlePause(),
                true
            )

            result
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to intercept feed auto-loop on playback completion", throwable)
            }
        }
    }
}
