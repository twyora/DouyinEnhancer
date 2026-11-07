package io.github.twyora.douyinenhancer

import com.highcapable.yukihookapi.hook.xposed.application.ModuleApplication
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.config.kvstorage.RemoteKVStorage

class DouyinEnhancerApplication : ModuleApplication() {
    override fun onCreate() {
        super.onCreate()

        ConfigManager.init(this, RemoteKVStorage.Companion)
    }
}
