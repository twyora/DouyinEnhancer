package com.yst.mkga.hook.dy.hook

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import org.luckypray.dexkit.DexKitBridge
import java.lang.reflect.Field

object CommentImageHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    val CommentImageStruct by lazyClass("com.ss.android.ugc.aweme.comment.model.CommentImageStruct")
    val originUrlField: Field by lazy {
        CommentImageStruct.resolve().firstField {
            name = "originUrl"
        }.self.apply {
            isAccessible = true
        }
    }

    override fun onHook() {
        withProcess(mainProcessName) {
            DexKitBridge.create(this.appInfo.sourceDir).use { bridge ->
                bridge.findMethod {
                    matcher {
                        declaredClass = "com.ss.android.ugc.aweme.comment.model.CommentImageStruct"
                        returnType = "com.ss.android.ugc.aweme.base.model.UrlModel"
                        paramCount = 0
                        addUsingField {
                            name = "downloadUrl"
                        }
                    }
                }.singleOrNull()?.also { match ->
                    YLog.debug("$TAG: Target method found: ${match.className}.${match.methodName}")

                    match.className.toClass().resolve().firstMethod {
                        name = match.methodName
                    }.hook {
                        before {
                            val originUrl = originUrlField.get(instance)
                            if (originUrl != null) {
                                result = originUrl
                            }
                        }
                    }.result {
                        onConductFailure { param, throwable ->
                            YLog.error("$TAG: Unable to replace with original image URL: ${throwable.message}")
                            param.result = param.callOriginal()
                        }
                    }
                } ?: run {
                    YLog.warn("$TAG: Target method not found, watermark-free comment image download is not active")
                }
            }
        }
    }
}