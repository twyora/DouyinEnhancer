package io.github.twyora.douyinenhancer.hook.feed

import com.highcapable.kavaref.extension.createInstance
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.getField
import io.github.twyora.douyinenhancer.utils.invokeMethod
import io.github.twyora.douyinenhancer.utils.invokeMethodOnly
import io.github.twyora.douyinenhancer.utils.resolveMethod

@HookOnMainProcess
object FeedDoubleTapOpenCommentHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !ConfigManager.moduleConfig.verboseDisabled

    override fun onHook() {
        if (!ConfigManager.feedConfig.doubleTapOpenComment) {
            if (verbose) {
                YLog.debug("$TAG: double-tap to open comment panel is disabled, skipping hook")
            }
            return
        }

        packageInstance.baseListFragmentPanel.selfClass?.resolveMethod(
            packageInstance.baseListFragmentPanel.handleDoubleClick()
        )?.hook {
            after {
                val aweme = instance.invokeMethod<Any>(
                    packageInstance.baseListFragmentPanel.getCurrentAweme()
                ) ?: run {
                    YLog.error("$TAG: unable to get current aweme")
                    return@after
                }

                val openCommentPanelEvent = packageInstance.videoEvent.selfClass?.createInstance(
                    DouyinPackage.VideoEventModule.EVENT_OPEN_COMMENT_PANEL,
                    aweme
                ) ?: run {
                    YLog.error("$TAG: unable to build open-comment-panel event")
                    return@after
                }

                if (verbose) {
                    val awemeId = aweme.getField<String>(
                        packageInstance.aweme.aid()
                    )
                    YLog.debug("$TAG: dispatching open-comment-panel event for current aweme, aid: $awemeId")
                }

                instance.invokeMethodOnly(
                    packageInstance.baseListFragmentPanel.handleVideoEvent(),
                    openCommentPanelEvent
                )
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to dispatch open-comment-panel event for current aweme", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook double-tap handle for opening comment panel", throwable)
            }
        }
    }
}
