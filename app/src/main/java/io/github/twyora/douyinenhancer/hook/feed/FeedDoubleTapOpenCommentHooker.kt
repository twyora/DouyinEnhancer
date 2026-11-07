package io.github.twyora.douyinenhancer.hook.feed

import com.highcapable.kavaref.extension.createInstance
import com.highcapable.yukihookapi.extension.kavaref.intercept
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.reflect.getFieldOrNull
import io.github.twyora.douyinenhancer.utils.reflect.invokeMethodOnlyQuietly
import io.github.twyora.douyinenhancer.utils.reflect.invokeMethodOrNull
import io.github.twyora.douyinenhancer.utils.reflect.resolveMethodOrNull

@HookOnMainProcess
object FeedDoubleTapOpenCommentHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !ConfigManager.module.verboseDisabled.value

    override fun onHook() {
        if (!ConfigManager.feed.doubleTapOpenComment.value) {
            YLog.info("$TAG: double-tap to open comment panel is disabled, skipping hook")
            return
        }

        packageInstance.baseListFragmentPanel.selfClass?.resolveMethodOrNull(
            packageInstance.baseListFragmentPanel.handleDoubleClick()
        )?.intercept {
            val result = proceed()

            val aweme = instance.invokeMethodOrNull(
                packageInstance.baseListFragmentPanel.getCurrentAweme()
            ) ?: run {
                YLog.error("$TAG: unable to get current aweme")
                return@intercept result
            }

            val openCommentPanelEvent = packageInstance.videoEvent.selfClass?.createInstance(
                DouyinPackage.VideoEventModule.EVENT_OPEN_COMMENT_PANEL,
                aweme
            ) ?: run {
                YLog.error("$TAG: unable to build open-comment-panel event")
                return@intercept result
            }

            instance.invokeMethodOnlyQuietly(
                packageInstance.baseListFragmentPanel.handleVideoEvent(),
                openCommentPanelEvent
            ).also {
                if (verbose) {
                    val awemeId = aweme.getFieldOrNull<String>(
                        packageInstance.aweme.aid()
                    )
                    YLog.debug("$TAG: dispatched open-comment-panel event for current aweme, aid: $awemeId")
                }
            }

            result
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to dispatch open-comment-panel event for current aweme", throwable)
            }
        }
    }
}
