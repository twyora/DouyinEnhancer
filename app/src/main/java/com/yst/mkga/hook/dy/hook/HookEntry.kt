package com.yst.mkga.hook.dy.hook

import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit

@InjectYukiHookWithXposed
object HookEntry : IYukiHookXposedInit {
    override fun onInit() = YukiHookAPI.configs {
        debugLog {
            tag = "DouyinEnhancer"
        }
        isDebug = false
    }

    override fun onHook() = encase {
        try {
            System.loadLibrary("dexkit")
        } catch (e: Throwable) {
            YLog.error("Failed to load DexKit native library: ${e.message}")
        }

        loadApp(hooker = CommentImageHooker)
        loadApp(hooker = CommentEmojiHooker)
    }
}