package io.github.twyora.douyinenhancer.hook.ui

import android.view.View
import com.highcapable.yukihookapi.extension.kavaref.intercept
import com.highcapable.yukihookapi.hook.core.YukiHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.reflect.getFieldOrNull
import io.github.twyora.douyinenhancer.utils.reflect.resolveMethodOrNull

@HookOnMainProcess
object FeedHideFollowButtonHooker : YukiBaseHooker() {
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
    }

    private fun installHideFollowButtonHook(): YukiHookCreator.MemberHooker<*>.Handle? =
        packageInstance.feedAvatarPresenter.selfClass?.resolveMethodOrNull(
            packageInstance.feedAvatarPresenter.updateFollowView()
        )?.intercept {
            val followStatusIndex = args.indexOfFirst {
                it is Int
            }.takeIf {
                it >= 0
            }
            val followStatus = followStatusIndex?.let {
                arg<Int>(it)
            }

            val result = if (followStatusIndex != null && followStatus == DouyinPackage.UserModule.FOLLOW_STATUS_UNFOLLOWED) {
                if (verbose) {
                    YLog.debug("$TAG: forcing unfollowed status to ${DouyinPackage.UserModule.FOLLOW_STATUS_FOLLOWING}")
                }
                // Spoof the following state so the host hides the follow button.
                proceed(
                    args.toTypedArray().apply {
                        set(followStatusIndex, DouyinPackage.UserModule.FOLLOW_STATUS_FOLLOWING)
                    }
                )
            } else {
                proceed()
            }

            if (
                // Fallback: force-hide the follow button when follow state cannot be resolved.
                // NOTE: A race condition during initial feed render may still leave the button visible.
                followStatusIndex == null ||
                // #242: Tapping the hidden follow area still triggers follow.
                // GONE takes the container out of hit‑testing, preventing taps from reaching the follow handler.
                followStatus == DouyinPackage.UserModule.FOLLOW_STATUS_UNFOLLOWED
            ) {
                instance.getFieldOrNull<View?>(
                    packageInstance.feedAvatarPresenter.mFollowContainerView()
                )?.let {
                    it.visibility = View.GONE
                }?.also {
                    if (verbose) {
                        YLog.debug("$TAG: follow button kept hidden on avatar view")
                    }
                }
            }

            result
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to hide follow button", throwable)
            }
        }
}
