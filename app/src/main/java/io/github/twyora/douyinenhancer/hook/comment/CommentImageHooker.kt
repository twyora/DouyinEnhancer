package io.github.twyora.douyinenhancer.hook.comment

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.getField
import io.github.twyora.douyinenhancer.utils.resolveMethod

@HookOnMainProcess
object CommentImageHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !ConfigManager.moduleConfig.verboseDisabled

    override fun onHook() {
        if (!ConfigManager.saveConfig.purifyCommentImage) {
            if (verbose) {
                YLog.debug("$TAG: purify comment image disabled, skipping hook")
            }
            return
        }

        packageInstance.commentImageStruct.selfClass?.resolveMethod(
            packageInstance.commentImageStruct.getDownloadUrl()
        )?.hook {
            before {
                val originUrl = instance.getField<Any?>(
                    packageInstance.commentImageStruct.originUrl()
                )
                if (originUrl != null) {
                    if (verbose) {
                        YLog.debug("$TAG: origin url present, override download url with origin url")
                    }
                    result = originUrl
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to override download url with origin url", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook for overriding download url with origin url", throwable)
            }
        }
    }
}
