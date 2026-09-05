package io.github.twyora.douyinenhancer.hook.feed

import com.highcapable.kavaref.extension.createInstance
import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.FeedKey
import io.github.twyora.douyinenhancer.config.key.ModuleKey
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.Method
import io.github.twyora.douyinenhancer.utils.getField
import io.github.twyora.douyinenhancer.utils.invokeMethod
import io.github.twyora.douyinenhancer.utils.invokeMethodOnly
import io.github.twyora.douyinenhancer.utils.resolveMethod

/**
 * 双击打开评论区（38.8.0 重做版）。
 *
 * 思路（与用户确认）：抖音默认双击=点赞。把“双击点赞”的动作点拦截下来：
 * - 点赞作废（resultNull，等同原版“禁用双击点赞”）；
 * - 改为派发“打开评论区”视频事件（type=7，与评论图标同一路径：handleVideoEvent 里
 *   i==7 即 onVideoCommentListEvent）。
 *
 * 38.8.0 注意：BaseListFragmentPanel.handleDoubleClick(MotionEvent) 在日志中从未触发
 * （新版双击点赞疑似不走该入口），故同时挂探针：
 * - handleDoubleClick(MotionEvent,MotionEvent,MotionEvent)（3 参，内部会调 1 参）
 * - handleBigDiggViewClick(MotionEvent, IFeedViewHolder, Aweme)（双击点赞的实际动作方法）
 * 哪个探针在真机双击时触发，下一版就把“作废+开评论”落到那个入口上。
 */
@HookOnMainProcess
object FeedDoubleTapOpenCommentHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !FastKVConfigManager.module.getBoolean(ModuleKey.DISABLE_VERBOSE_LOGS, false)

    override fun onHook() {
        if (!FastKVConfigManager.settings.getBoolean(FeedKey.FEED_DOUBLE_TAP_OPEN_COMMENT, false)) {
            if (verbose) {
                YLog.debug("$TAG: double-tap to open comment panel is disabled, skipping hook")
            }
            return
        }

        val panelClass = packageInstance.baseListFragmentPanel.selfClass
        if (panelClass == null) {
            YLog.error("$TAG: unable to resolve BaseListFragmentPanel class")
            return
        }

        // 主入口：抖音双击（若仍走 handleDoubleClick）→ 作废点赞 + 打开评论
        val handleDoubleClick = packageInstance.baseListFragmentPanel.handleDoubleClick()
        if (handleDoubleClick.name != null) {
            panelClass.resolveMethod(handleDoubleClick)?.hook {
                before {
                    interceptAndOpenComment(instance)
                    resultNull()
                }
            }?.result {
                onConductFailure { _, throwable -> YLog.error("$TAG: double-tap intercept failed", throwable) }
                onHookingFailure { throwable -> YLog.error("$TAG: failed to hook handleDoubleClick for open comment", throwable) }
            }
        } else if (verbose) {
            YLog.debug("$TAG: handleDoubleClick(MotionEvent) not found in config, skipped main intercept")
        }

        // 探针 1：3 参 handleDoubleClick
        installProbe(
            panelClass,
            Method("handleDoubleClick", listOf("android.view.MotionEvent", "android.view.MotionEvent", "android.view.MotionEvent")),
            "handleDoubleClick(3-arg)"
        )
        // 探针 2：双击点赞实际动作 handleBigDiggViewClick
        installProbe(
            panelClass,
            Method(
                "handleBigDiggViewClick",
                listOf(
                    "android.view.MotionEvent",
                    "com.ss.android.ugc.aweme.feed.adapter.IFeedViewHolder",
                    "com.ss.android.ugc.aweme.feed.model.Aweme"
                )
            ),
            "handleBigDiggViewClick"
        )
    }

    /** 作废本次双击点赞，并改为打开评论区 */
    private fun interceptAndOpenComment(panel: Any) {
        if (verbose) {
            YLog.debug("$TAG: double-tap intercepted, digg voided -> opening comment panel")
        }
        val aweme = panel.invokeMethod<Any>(
            packageInstance.baseListFragmentPanel.getCurrentAweme()
        ) ?: run {
            YLog.error("$TAG: unable to get current aweme for open comment")
            return
        }
        dispatchOpenComment(panel, aweme)
    }

    private fun dispatchOpenComment(panel: Any, aweme: Any) {
        val openCommentPanelEvent = packageInstance.videoEvent.selfClass?.createInstance(
            DouyinPackage.VideoEventModule.EVENT_OPEN_COMMENT_PANEL,
            aweme
        ) ?: run {
            YLog.error("$TAG: unable to build open-comment-panel event")
            return
        }
        if (verbose) {
            val awemeId = aweme.getField<String>(packageInstance.aweme.aid())
            YLog.debug("$TAG: dispatching open-comment-panel event for current aweme, aid: $awemeId")
        }
        panel.invokeMethodOnly(
            packageInstance.baseListFragmentPanel.handleVideoEvent(),
            openCommentPanelEvent
        )
    }

    private fun installProbe(panelClass: Class<*>, method: Method, label: String): YukiMemberHookCreator.MemberHookCreator.Result? {
        if (method.name == null) {
            return null
        }
        return panelClass.resolveMethod(method)?.hook {
            before {
                if (verbose) {
                    val aid = runCatching {
                        val aweme = args.lastOrNull() ?: instance.invokeMethod<Any>(
                            packageInstance.baseListFragmentPanel.getCurrentAweme()
                        )
                        aweme?.getField<String>(packageInstance.aweme.aid())
                    }.getOrNull()
                    YLog.debug("$TAG: [probe] $label fired, aid=$aid")
                }
            }
        }?.result {
            onConductFailure { _, throwable -> YLog.error("$TAG: probe $label failed", throwable) }
            onHookingFailure { throwable -> YLog.error("$TAG: failed to hook probe $label", throwable) }
        }
    }
}
