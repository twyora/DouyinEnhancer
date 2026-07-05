package io.github.twyora.douyinenhancer.hook

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.hook.utils.getField
import io.github.twyora.douyinenhancer.hook.utils.invokeMethod
import io.github.twyora.douyinenhancer.hook.utils.invokeStaticMethod
import io.github.twyora.douyinenhancer.hook.utils.resolveMethod

object CommentVideoHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    override fun onHook() {
        withProcess(mainProcessName) {
            val packageInstance = DouyinPackage.instance

            packageInstance.commentExtensionsKt.selfClass?.resolveMethod(
                packageInstance.commentExtensionsKt.hasValidImageUrl()
            )?.hook {
                before {
                    val comment = args[0] ?: return@before
                    val commentAudio = comment.getField<Any>(
                        packageInstance.comment.commentAudio()
                    )
                    if (commentAudio != null) {
                        resultTrue()

                        val awemeId = comment.getField<String>(
                            packageInstance.comment.awemeId()
                        )
                        if (awemeId == null) {
                            YLog.warn("$TAG: awemeId is null")
                            return@before
                        }

                        val aweme = packageInstance.awemeService.selfClass?.invokeStaticMethod<Any>(
                            packageInstance.awemeService.getInstance()
                        )?.invokeMethod<Any>(
                            packageInstance.awemeService.getAwemeById(),
                            awemeId
                        )
                        if (aweme == null) {
                            YLog.warn("$TAG: aweme is null")
                            return@before
                        }
                    }
                }
            }?.result {
                onConductFailure { _, throwable ->
                    YLog.warn("$TAG: runtime error: ", throwable)
                }
            }
        }
    }
}
