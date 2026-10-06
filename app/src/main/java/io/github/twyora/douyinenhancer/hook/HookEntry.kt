package io.github.twyora.douyinenhancer.hook

import com.highcapable.yukihookapi.annotation.xposed.YukiHookLibXposedEntry
import com.highcapable.yukihookapi.hook.factory.configure
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.xposed.YukiHookXposedModule
import io.github.twyora.douyinenhancer.BuildConfig
import io.github.twyora.douyinenhancer.bridge.ModuleApp
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.config.kvstorage.FastKVStorage

@YukiHookLibXposedEntry(
    targetApiVersion = 102,
    scope = ["com.ss.android.ugc.aweme"],
    staticScope = true
)
object HookEntry : YukiHookXposedModule {
    private val TAG = this::class.simpleName

    override fun onInit() = configure {
        logging {
            tag = "DouyinEnhancer"
            enabled = true
            recording = false
        }
        debug = BuildConfig.DEBUG
        dataChannel = false
    }

    override fun onHook() = encase {
        loadApp(name = "com.ss.android.ugc.aweme") {
            withProcess(mainProcessName) {
                withApplicationReady { application ->
                    ModuleApp.init(this@withProcess)
                    ConfigManager.init(application, FastKVStorage.Companion)
                    // load cached HookInfo and run hooks when app context is available
                    DouyinPackage.init(application.classLoader, application)

                    val verboseDisabled = ConfigManager.module.verboseDisabled.value
                    YLog.info("$TAG: verbose log disabling is $verboseDisabled")

                    HookerRegistry.mainProcessHookers.forEach {
                        loadHooker(it)
                    }
                }
            }
        }
    }
}
