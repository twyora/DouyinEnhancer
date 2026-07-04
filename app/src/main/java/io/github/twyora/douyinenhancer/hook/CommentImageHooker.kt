package io.github.twyora.douyinenhancer.hook

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.hook.utils.getField
import io.github.twyora.douyinenhancer.hook.utils.resolveMethod

object CommentImageHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    override fun onHook() {
        if (!FastKVConfigManager.settings.getBoolean("purify_comment_image", false)) {
            return
        }

        withProcess(mainProcessName) {
            val packageInstance = DouyinPackage.instance

            packageInstance.commentImageStruct.selfClass?.resolveMethod(packageInstance.commentImageStruct.getDownloadUrl())?.hook {
                before {
                    val originUrl =
                        instance.getField<Any?>(packageInstance.commentImageStruct.originUrl())
                    if (originUrl != null) {
                        result = originUrl
                    }
                }
            } ?: run {
                YLog.warn(
                    "$TAG: Target method not found, watermark-free comment image download is not active"
                )
            }
        }
    }
}
