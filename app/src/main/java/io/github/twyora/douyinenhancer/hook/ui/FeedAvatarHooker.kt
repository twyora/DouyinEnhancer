package io.github.twyora.douyinenhancer.hook.ui

import android.view.View
import com.highcapable.yukihookapi.extension.kavaref.intercept
import com.highcapable.yukihookapi.hook.core.YukiHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.reflect.Method
import io.github.twyora.douyinenhancer.utils.reflect.resolveMethodOrNull

@HookOnMainProcess
object FeedAvatarHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !ConfigManager.module.verboseDisabled.value

    override fun onHook() {
        if (!ConfigManager.ui.hideFollowButton.value) {
            YLog.info("$TAG: follow button hiding disabled, skipping hook")
            return
        }
        installHideFollowButtonHook()
        installFollowClickRedirectHook()
    }

    private fun installFollowClickRedirectHook(): YukiHookCreator.MemberHooker<*>.Handle? {
        // Resource and field names below were identified in Douyin 39.4.0 (390401).
        if (packageInstance.hostVersionCode() != 390401) return null

        val presenterClass = packageInstance.feedAvatarPresenter.selfClass ?: return null
        val avatarField = runCatching {
            presenterClass.getDeclaredField("x").apply { isAccessible = true }
        }.getOrElse {
            YLog.error("$TAG: failed to find feed avatar field", it)
            return null
        }

        return presenterClass.resolveMethodOrNull(Method("onClick", listOf("android.view.View")))?.intercept {
            val clickedView = args.firstOrNull() as? View ?: return@intercept proceed()
            val resources = clickedView.resources
            val followId = resources.getIdentifier("j13", "id", clickedView.context.packageName)
            if (followId == 0 || clickedView.id != followId) return@intercept proceed()

            val avatarId = resources.getIdentifier("user_avatar", "id", clickedView.context.packageName)
            var avatar = runCatching { avatarField.get(instance) as? View }.getOrNull()
            while (avatar != null && avatar.id != avatarId) {
                avatar = avatar.parent as? View
            }

            if (avatar == null || avatarId == 0) {
                YLog.error("$TAG: unable to redirect follow click to the current avatar")
                return@intercept null
            }

            proceed(arrayOf(avatar))
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to redirect follow button click", throwable)
            }
        }
    }

    private fun installHideFollowButtonHook(): YukiHookCreator.MemberHooker<*>.Handle? {
        return packageInstance.feedAvatarPresenter.selfClass?.resolveMethodOrNull(
            packageInstance.feedAvatarPresenter.updateFollowView()
        )?.intercept {
            val followStatusIndex = args.indexOfFirst {
                it is Int
            }.takeIf {
                it != -1
            } ?: run {
                YLog.error("$TAG: follow status not found in argument list")
                return@intercept proceed()
            }

            val followStatus = arg<Int>(followStatusIndex)

            if (followStatus != DouyinPackage.UserModule.FOLLOW_STATUS_UNFOLLOWED) {
                return@intercept proceed()
            } else if (verbose) {
                YLog.debug("$TAG: forcing unfollowed status to ${DouyinPackage.UserModule.FOLLOW_STATUS_FOLLOWING}")
            }

            proceed(
                args.toTypedArray().apply {
                    set(followStatusIndex, DouyinPackage.UserModule.FOLLOW_STATUS_FOLLOWING)
                }
            )
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to hide follow button", throwable)
            }
        }
    }
}
