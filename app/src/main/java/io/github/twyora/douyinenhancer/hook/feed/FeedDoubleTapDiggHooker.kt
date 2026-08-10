package io.github.twyora.douyinenhancer.hook.feed

import com.highcapable.kavaref.extension.toClassOrNull
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
                YLog.debug("$TAG: double-tap digg disabling disabled, skip hook")
            }
            return
        }

        hookPanelDoubleClick()
        hookViewHolderDoubleClick()
        hookDiggPresenterDoubleClick()
        hookDiggLayoutHeartAnimation()
    }

    /**
     * Legacy 37.6/38.8 panel entry; method removed on 39.6.0+ (resolve fails safely).
     */
    private fun hookPanelDoubleClick() {
        packageInstance.baseListFragmentPanel.selfClass?.resolveMethod(
            packageInstance.baseListFragmentPanel.handleDoubleClick()
        )?.hook {
            before {
                if (verbose) {
                    YLog.debug("$TAG: disabling double-tap digg (panel)")
                }
                resultNull()
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: error while disabling double-tap digg", throwable)
            }
            onHookingFailure {
                YLog.error("$TAG: hook failed, double-tap action cannot be intercepted, double-tap digg may stay enabled")
            }
        }
    }

    /**
     * The double-tap gesture publishes through IFeedViewHolder.handleDoubleClick(Aweme)
     * implementers (VideoViewHolder / FeedImageViewHolder), which is the request
     * publisher — blocking it here stops the digg request before it reaches the
     * presenter. Class names and the interface method name are preserved across
     * 37.6/38.8/39.6/39.9, so this is pure reflection (no DexKit needed).
     */
    private fun hookViewHolderDoubleClick() {
        val holders = listOf(
            "com.ss.android.ugc.aweme.feed.adapter.VideoViewHolder" to "VideoViewHolder",
            "com.ss.android.ugc.aweme.feed.adapter.FeedImageViewHolder" to "FeedImageViewHolder",
        )
        for ((holderName, holderLabel) in holders) {
            val holderClass = holderName.toClassOrNull(appClassLoader) ?: continue
            val method = holderClass.declaredMethods.firstOrNull {
                it.name == "handleDoubleClick" &&
                    it.parameterTypes.size == 1 &&
                    it.parameterTypes[0].name == "com.ss.android.ugc.aweme.feed.model.Aweme"
            } ?: continue
            method.hook {
                before {
                    if (verbose) {
                        YLog.debug("$TAG: disabling double-tap digg via $holderLabel")
                    }
                    resultNull()
                }
            }
        }
    }

    /**
     * The real double-tap digg executor is FeedDiggPresenter (class name preserved
     * across versions). Its (Aweme, String) method is the final digg request entry,
     * called with tag "click_double_like" from the double-tap runnable. Blocking it
     * at the request layer leaves the right-side digg button working (tag "click_like").
     */
    private fun hookDiggPresenterDoubleClick() {
        val presenterClass =
            "com.ss.android.ugc.aweme.feed.quick.presenter.FeedDiggPresenter".toClassOrNull(appClassLoader)
                ?: run {
                    YLog.warn("$TAG: FeedDiggPresenter not found, skip request hook")
                    return
                }
        var hooked = 0
        for (method in presenterClass.declaredMethods) {
            val types = method.parameterTypes
            if (types.size != 2) continue
            if (types[0].name != "com.ss.android.ugc.aweme.feed.model.Aweme") continue
            if (types[1] != String::class.java) continue
            method.hook {
                before {
                    val tag = args.getOrNull(1) as? String
                    if (tag == "click_double_like") {
                        if (verbose) {
                            YLog.debug("$TAG: blocking FeedDiggPresenter.${method.name}(click_double_like)")
                        }
                        resultNull()
                    }
                }
            }
            hooked++
        }
        YLog.info("$TAG: hooked $hooked FeedDiggPresenter (Aweme,String) methods")
    }

    /**
     * DiggLayout spawns the floating heart ImageViews at (x,y).
     * Match by signature (FF)V so it works across method renames:
     * LIZJ(FF)V on 39.6.0, LIZIZ(FF)V on 37.6/38.8.
     */
    private fun hookDiggLayoutHeartAnimation() {
        val diggLayoutClass =
            "com.ss.android.ugc.aweme.common.widget.DiggLayout".toClassOrNull(appClassLoader)
                ?: run {
                    YLog.warn("$TAG: DiggLayout not found, skip heart animation hook")
                    return
                }
        val floatType = Float::class.javaPrimitiveType
        val methods = diggLayoutClass.declaredMethods.filter { method ->
            method.parameterTypes.size == 2 &&
                method.parameterTypes[0] == floatType &&
                method.parameterTypes[1] == floatType &&
                method.returnType == Void.TYPE
        }
        if (methods.isEmpty()) {
            YLog.warn("$TAG: no DiggLayout (FF)V method found")
            return
        }
        for (method in methods) {
            method.hook {
                before {
                    if (verbose) {
                        YLog.debug("$TAG: blocking DiggLayout.${method.name}(FF) heart animation")
                    }
                    resultNull()
                }
            }
            YLog.info("$TAG: hooked DiggLayout.${method.name}(FF)V")
        }
    }
}
