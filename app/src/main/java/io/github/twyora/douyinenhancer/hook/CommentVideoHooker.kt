package io.github.twyora.douyinenhancer.hook

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.hook.utils.getField
import io.github.twyora.douyinenhancer.hook.utils.invokeMethod
import io.github.twyora.douyinenhancer.hook.utils.invokeStaticMethod
import io.github.twyora.douyinenhancer.hook.utils.resolveField
import io.github.twyora.douyinenhancer.hook.utils.resolveMethod

object CommentVideoHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    override fun onHook() {
        withProcess(name = mainProcessName) {
            val packageInstance = DouyinPackage.instance

            packageInstance.saveImageActionItem.selfClass?.resolveMethod(
               packageInstance.saveImageActionItem.isEnabled()
            )?.hook {
                before {
                    val comment = instance.getField<Any>(
                        packageInstance.commentLongPressItemModel.commentActionParams()
                    )?.getField<Any>(
                        packageInstance.commentActionParams.comment()
                    )?:run{
                        YLog.warn("$TAG: comment is null")
                        return@before
                    }
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

                        YLog.debug("$TAG: found aweme")
                    }
                }
            }?.result {
                onConductFailure { _, throwable ->
                    YLog.warn("$TAG: runtime error: ", throwable)
                }
            }

            packageInstance.commentLongPressWhiteListProvider.selfClass?.resolveMethod(
                packageInstance.commentLongPressWhiteListProvider.buildWhiteList()
            )?.hook{
                after {
                    val commentActionParams= args[0] ?:run{
                        YLog.warn("$TAG: commentActionParams is null")
                        return@after
                    }
                    @Suppress("UNCHECKED_CAST")
                    val whiteList = result as? MutableSet<String> ?: run {
                        YLog.warn("$TAG: whiteList is not a mutable set")
                        return@after
                    }

                    val comment=commentActionParams.getField<Any?>(
                        packageInstance.commentActionParams.comment()
                    )?:run{
                        YLog.warn("$TAG: comment is null")
                        return@after
                    }

                    if(comment.getField<Any?>(
                        packageInstance.comment.commentAudio()
                    )!=null){
                        whiteList.add("save_image")}
                }
            }
        }
    }
}
