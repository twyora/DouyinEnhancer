package io.github.twyora.douyinenhancer.hook.ui

import android.view.View
import androidx.collection.ArraySet
import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.resolveMethod

@HookOnMainProcess
object DanmakuViewHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !ConfigManager.moduleConfig.verboseDisabled

    private val danmakuViewIds = ArraySet<Int>().apply {
        add(View.generateViewId())
    }

    override fun onHook() {
        if (!ConfigManager.uiConfig.keepDanmakuVisible) {
            if (verbose) {
                YLog.debug("$TAG: keep danmaku visible disabled, skip danmaku hooks")
            }
            return
        }
        installAssignDanmakuViewIdHook()
        installAddDanmakuViewIdToCleanModeWhiteListHook()
        installBlockDanmakuViewHidingHook()
    }

    private fun installAssignDanmakuViewIdHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        return packageInstance.danmakuView.selfClass?.resolveMethod(
            packageInstance.danmakuView.onAttachedToWindow()
        )?.hook {
            after {
                val danmakuView = instance as? View ?: run {
                    YLog.error("$TAG: ${instance::class.qualifiedName} is not View")
                    return@after
                }

                if (danmakuView.id == View.NO_ID) {
                    val danmakuViewId = danmakuViewIds.first()
                    if (verbose) {
                        YLog.debug("$TAG: assign danmaku view id($danmakuViewId) to view without id")
                    }
                    danmakuView.id = danmakuViewId
                } else {
                    if (verbose) {
                        YLog.debug("$TAG: collect existing danmaku view id(${danmakuView.id})")
                    }
                    danmakuViewIds.add(danmakuView.id)
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to assign danmaku view id", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook danmaku view id assignment", throwable)
            }
        }
    }

    private fun installAddDanmakuViewIdToCleanModeWhiteListHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        return packageInstance.cleanModePresenter.selfClass?.resolveMethod(
            packageInstance.cleanModePresenter.enterCleanMode()
        )?.hook {
            before {
                // The whiteList argument's position in the parameter list varies across host versions,
                // resolve it at runtime
                val whiteList = args.firstNotNullOfOrNull {
                    @Suppress("UNCHECKED_CAST")
                    it as? MutableList<Int>
                } ?: run {
                    YLog.error("$TAG: whiteList not found in the argument list")
                    return@before
                }

                if (verbose) {
                    YLog.debug("$TAG: add danmaku view ids into clean mode white list")
                }

                danmakuViewIds.forEach {
                    whiteList.add(it)
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to add danmaku view id to clean mode white list", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook clean mode white list adding", throwable)
            }
        }
    }

    private fun installBlockDanmakuViewHidingHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        return packageInstance.cleanModePresenter.selfClass?.resolveMethod(
            packageInstance.cleanModePresenter.setVisibility()
        )?.hook {
            before {
                val view = args[0] as? View ?: run {
                    YLog.error("$TAG: ${args[0]?.javaClass?.name} is not a View")
                    return@before
                }
                val visibility = args[1] as? Int ?: run {
                    YLog.error("$TAG: ${args[1]?.javaClass?.name} is not a visibility value int")
                    return@before
                }
                if (visibility == View.VISIBLE) {
                    return@before
                }

                if (danmakuViewIds.none {
                        view.findViewById<View?>(it) != null
                    }
                ) {
                    return@before
                }
                YLog.info(
                    "$TAG: ${view::class.qualifiedName}{id=0x${
                        view.id.toString(
                            16
                        )
                    }} is a danmaku view holder trying to hide itself; intercept it"
                )

                resultNull()
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to block danmaku view hiding", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook for blocking danmaku view hiding", throwable)
            }
        }
    }
}
