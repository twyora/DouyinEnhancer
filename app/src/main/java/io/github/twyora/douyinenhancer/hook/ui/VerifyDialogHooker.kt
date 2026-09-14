package io.github.twyora.douyinenhancer.hook.ui

import android.app.Activity
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.ui.VerifyDialog
import io.github.twyora.douyinenhancer.utils.resolveMethod

@HookOnMainProcess
object VerifyDialogHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !ConfigManager.moduleConfig.verboseDisabled

    override fun onHook() {
        if (!VerifyDialog.shouldVerify()) {
            if (verbose) {
                YLog.debug("$TAG: no verification required, skipping verification check")
            }
            return
        }

        packageInstance.mainActivity.selfClass?.resolveMethod(
            packageInstance.mainActivity.onResume()
        )?.hook {
            after {
                val activity = instance as? Activity ?: run {
                    YLog.error("$TAG: ${instance::class.qualifiedName} is not an Activity")
                    return@after
                }
                VerifyDialog.show(activity)

                removeSelf {
                    if (verbose) {
                        YLog.debug("$TAG: verification check hook removed to prevent duplicate check")
                    }
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to show verify dialog on resume", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook for showing verify dialog on resume", throwable)
            }
        }
    }
}
