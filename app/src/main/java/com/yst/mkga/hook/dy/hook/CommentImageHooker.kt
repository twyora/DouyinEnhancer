package com.yst.mkga.hook.dy.hook

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import org.luckypray.dexkit.DexKitBridge

object CommentImageHooker : YukiBaseHooker() {
    override fun onHook() {
        loadApp(name = "com.ss.android.ugc.aweme") {
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
                }.singleOrNull()?.also { result ->
                    YLog.debug("CommentImageHooker: Target method found: ${result.className}.${result.methodName}")

                    result.className.toClass().resolve().firstMethod {
                        name = result.methodName
                    }.hook {
                        replaceAny {
                            instance.asResolver().firstMethod {
                                name = "getOriginUrl"
                            }.invoke()
                        }
                    }.result {
                        onConductFailure { param, throwable ->
                            YLog.error("CommentImageHooker: Unable to replace with original image URL: ${throwable.message}")
                            param.result = param.callOriginal()
                        }
                    }
                } ?: run {
                    YLog.warn("CommentImageHooker: Target method not found, watermark-free comment image download is not active")
                }
            }
        }
    }
}