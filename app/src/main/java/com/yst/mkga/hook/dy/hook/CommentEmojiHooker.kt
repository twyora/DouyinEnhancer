package com.yst.mkga.hook.dy.hook

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import org.luckypray.dexkit.DexKitBridge
import java.lang.reflect.Modifier


object CommentEmojiHooker : YukiBaseHooker() {
    override fun onHook() {
        loadApp(name = "com.ss.android.ugc.aweme") {
            DexKitBridge.create(this.appInfo.sourceDir).use { bridge ->
                bridge.findMethod {
                    matcher {
                        modifiers = Modifier.STATIC
                        params {
                            add("com.ss.android.ugc.aweme.comment.model.Comment")
                            add("int")
                        }
                        returnType = "boolean"
                        invokeMethods {
                            // TODO: This method has already been looked up in CommentImageHooker, consider caching and sharing it here
                            add {
                                declaredClass =
                                    "com.ss.android.ugc.aweme.comment.model.CommentImageStruct"
                                returnType = "com.ss.android.ugc.aweme.base.model.UrlModel"
                                paramCount = 0
                                addUsingField {
                                    name = "downloadUrl"
                                }
                            }
                        }
                    }
                }.singleOrNull()?.also { result ->
                    YLog.debug("CommentEmojiHooker: Target method found: ${result.className}.${result.methodName}")

                    result.className.toClass().resolve().firstMethod {
                        name = result.methodName
                    }.hook {
                        before {
                            val comment = args[0]
                            val emojiUrlList = comment?.asResolver()?.firstField {
                                name = "emoji"
                            }?.get()?.asResolver()?.firstField {
                                name = "animateUrl"
                            }?.get()?.asResolver()?.firstField {
                                name = "urlList"
                            }?.get<List<String>>()

                            if (emojiUrlList?.isNotEmpty() == true) {
                                resultTrue()
                            }
                        }
                    }.result {
                        onConductFailure { param, throwable ->
                            YLog.error("CommentEmojiHooker: Unable to replace with original emoji URL: ${throwable.message}")
                            param.result = param.callOriginal()
                        }
                    }
                } ?: run {
                    YLog.warn("CommentEmojiHooker: Target method not found, comment emoji download is not active")
                }
            }
        }
    }
}