package com.yst.mkga.hook.dy.hook

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.random.Random
import org.json.JSONObject
import org.luckypray.dexkit.DexKitBridge

object FeedHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val AwemeClass by lazyClass("com.ss.android.ugc.aweme.feed.model.Aweme")
    private val AwemeClassGetAdMethod: Method by lazy {
        AwemeClass.resolve().firstMethod {
            name = "getAd"
        }.self.apply {
            isAccessible = true
        }
    }
    private val AwemeClassDescriptionField: Field by lazy {
        AwemeClass.resolve().firstField {
            name = "desc"
        }.self.apply {
            isAccessible = true
        }
    }
    private val AwemeClassAuthorField: Field by lazy {
        AwemeClass.resolve().firstField {
            name = "author"
        }.self.apply {
            isAccessible = true
        }
    }

    private val UserClass by lazyClass("com.ss.android.ugc.aweme.profile.model.User")
    private val UserClassNickNameField: Field by lazy {
        UserClass.resolve().firstField {
            name = "nickname"
        }.self.apply {
            isAccessible = true
        }
    }

    override fun onHook() {
        withProcess(mainProcessName) {
            DexKitBridge.create(this.appInfo.sourceDir).use { bridge ->
                installFeedHook(bridge)
            }
        }
    }

    private fun installFeedHook(bridge: DexKitBridge): YukiMemberHookCreator.MemberHookCreator.Result? {
        var ret: YukiMemberHookCreator.MemberHookCreator.Result? = null

        bridge.findMethod {
            matcher {
                modifiers = Modifier.PUBLIC + Modifier.STATIC
                returnType = "void"
                params {
                    add("int")
                    add("java.lang.String")
                    add("java.util.List")
                }
                invokeMethods {
                    add {
                        descriptor = "Ljava/util/List;->size()I"
                    }
                    add {
                        descriptor = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;->setRequestId(Ljava/lang/String;)V"
                    }
                    add {
                        descriptor = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;->getAd()Z"
                    }
                    add {
                        descriptor =
                            "Lcom/ss/android/ugc/aweme/awemeservice/api/IAwemeService;->updateAweme(Lcom/ss/android/ugc/aweme/feed/model/Aweme;I)Lcom/ss/android/ugc/aweme/feed/model/Aweme;"
                    }
                    add {
                        descriptor = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;->isLive()Z"
                    }
                }
            }
        }.singleOrNull()?.also { match ->
            YLog.info("$TAG: Target method found: ${match.className}.${match.methodName}")

            match.className.toClass().resolve().firstMethod {
                name = match.methodName
            }.hook {
                before {
                    val awemeList = args[2] as? List<*> ?: return@before
                    if (awemeList !is MutableList<*>) {
                        YLog.error("$TAG: awemeList is not MutableList, cannot remove items")
                        return@before
                    }

                    val iter = awemeList.iterator()
                    while (iter.hasNext()) {
                        val awemeObj = iter.next()

                        val awemeDesc = AwemeClassDescriptionField.get(awemeObj) as? String ?: continue
                        if (awemeDesc.isNotEmpty() && awemeDesc.contains("我")) {
                            val awemeAuthor = AwemeClassAuthorField.get(awemeObj)
                            val awemeAuthorNickName = if (awemeAuthor != null) {
                                UserClassNickNameField.get(awemeAuthor) as? String ?: "unknown"
                            } else {
                                "unknown"
                            }
                            YLog.warn(
                                "$TAG: Aweme desc contains specific word, author name: $awemeAuthorNickName, aweme description: $awemeDesc, removing from aweme list"
                            )
                            iter.remove()
                            continue
                        }
                    }
                }
            }.result {
                onConductFailure { param, throwable ->
                    YLog.error("$TAG: Feed hook runtime error", throwable)
                }
                onHookingFailure { throwable ->
                    YLog.error("$TAG: Failed to hook feed method", throwable)
                }
                onHooked {
                    YLog.info("$TAG: Feed hook installed")
                }.also {
                    ret = it
                }
            }
        } ?: run {
            YLog.error("$TAG: Target method not found, feed hook will not be installed")
        }

        return ret
    }
}
