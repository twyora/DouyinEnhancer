package io.github.twyora.douyinenhancer.hook

import android.app.Application
import android.app.Instrumentation
import android.content.Context
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import io.github.twyora.douyinenhancer.config.FastKVConfigManager

@InjectYukiHookWithXposed
object HookEntry : IYukiHookXposedInit {
    private val TAG = this::class.simpleName

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

        // Load cached HookInfo and run hooks when app context is available
        withProcess(mainProcessName) {
            Instrumentation::class
                .resolve()
                .firstMethod {
                    name = "callApplicationOnCreate"
                    parameters(Application::class)
                }.hook {
                    before {
                        val context = args[0] as? Context ?: return@before

                        // Hook main process only; skip plugin sub-processes and cases
                        // where processName resolves to "android" unexpectedly (root cause unknown)
                        if ((appInfo.sourceDir != context.applicationInfo.sourceDir) ||
                            processName != context.packageName
                        ) {
                            return@before
                        }

                        DouyinPackage(appClassLoader!!, context)
                        FastKVConfigManager.init(context)

                        loadApp(hooker = SettingsHooker)
                        loadApp(hooker = CommentImageHooker)
                        loadApp(hooker = CommentEmojiHooker)
                        loadApp(hooker = RecommendedFeedHooker)
                    }
                }
        }
    }
}
