package com.yst.mkga.hook.dy.hook

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit

@InjectYukiHookWithXposed
object HookEntry : IYukiHookXposedInit {
    override fun onInit() = YukiHookAPI.configs{
        debugLog {
            tag = "DouyinEnhancer"
        }
        isDebug = false
    }

    override fun onHook() = YukiHookAPI.encase {
        loadApp(name = "com.ss.android.ugc.aweme") {
            "com.ss.android.ugc.aweme.comment.model.CommentImageStruct".toClass().resolve().firstMethod {
                name = "LIZ"
                emptyParameters()
                returnType("com.ss.android.ugc.aweme.base.model.UrlModel")
            }.hook {
                replaceAny {
                    instance.javaClass.getDeclaredMethod("getOriginUrl").invoke(instance)
                }
            }.result {
                onConductFailure { param, throwable ->
                    YLog.error("Failed to hook: ${throwable.message}")
                    param.result = null
                }
            }
        }
    }
}