package io.github.twyora.douyinenhancer.hook.ui

import com.highcapable.yukihookapi.extension.kavaref.intercept
import com.highcapable.yukihookapi.hook.core.YukiHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.reflect.getFieldOrNull
import io.github.twyora.douyinenhancer.utils.reflect.resolveMethodOrNull

@HookOnMainProcess
object FeedKeepCleanModeHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    override fun onHook() {
        installDismissWindowHook()
        installDismissWindowEventHook()
    }

    private fun installDismissWindowHook(): YukiHookCreator.MemberHooker<*>.Handle? {
        // Why does ktlint convert block returns to expression bodies sometimes, but not always?
        return packageInstance.pinchViewModel.selfClass?.resolveMethodOrNull(
            packageInstance.pinchViewModel.dismissWindow()
        )?.intercept {
            // NOTE: null: state-reset signal
            val dismiss = args.firstNotNullOfOrNull {
                it as? Boolean
            }

            // Intercepts window dismissal requests unless surface handoff
            // is explicitly requested (`true`).
            // NOTE: Allowing `true` to proceed will still exit clean mode,
            // as both values ultimately trigger the underlying exit sequence.
            if (dismiss != true) {
                YLog.info("$TAG: intercepted clean-mode exit, keeping clean mode")
            } else {
                proceed()
            }
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to hook clean-mode exit", throwable)
            }
        }
    }

    private fun installDismissWindowEventHook(): YukiHookCreator.MemberHooker<*>.Handle? {
        return packageInstance.pinchViewModel.selfClass?.resolveMethodOrNull(
            packageInstance.pinchViewModel.setDismissWindowEvent()
        )?.intercept {
            val event = args.firstOrNull {
                packageInstance.dismissWindowEvent.selfClass?.isInstance(it) == true
            } ?: run {
                YLog.error("$TAG: dismissWindowEvent argument not found in argument list")
                return@intercept proceed()
            }

            val dismissWithSurface = event.getFieldOrNull<Boolean>(
                packageInstance.dismissWindowEvent.dismissWithSurface()
            ) ?: run {
                YLog.error("$TAG: dismissWithSurface field not found in event")
                return@intercept proceed()
            }
            val enterMethod = event.getFieldOrNull<String>(
                packageInstance.dismissWindowEvent.enterMethod()
            ) ?: run {
                YLog.error("$TAG: enterMethod field not found in event")
                return@intercept proceed()
            }

            if (dismissWithSurface && enterMethod in listOf(
                    DouyinPackage.DismissWindowEventModule.ENTER_METHOD_SLIDE_UP,
                    DouyinPackage.DismissWindowEventModule.ENTER_METHOD_SLIDE_DOWN
                )
            ) {
                YLog.info("$TAG: intercepted slide-to-exit, keeping clean mode")
            } else {
                proceed()
            }
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to hook dismiss event publisher", throwable)
            }
        }
    }
}
