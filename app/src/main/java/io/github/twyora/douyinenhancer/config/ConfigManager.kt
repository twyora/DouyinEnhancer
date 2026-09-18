package io.github.twyora.douyinenhancer.config

import android.content.Context
import io.github.twyora.douyinenhancer.config.kvstorage.FastKVStorage
import io.github.twyora.douyinenhancer.config.kvstorage.IKVStorage

// TODO: Consider renaming
object ConfigManager {
    lateinit var settings: IKVStorage
        private set

    lateinit var module: IKVStorage
        private set

    val recommendedFeedFilterConfig by lazy {
        RecommendedFeedFilterConfigProvider(
            settings,
            gates = mapOf(
                FeatureGate.HIDDEN to {
                    miscConfig.hiddenFeatureEnabled
                })
        )
    }

    val moduleConfig by lazy {
        ModuleConfigProvider(module)
    }

    val miscConfig by lazy {
        MiscConfigProvider(settings)
    }

    val feedConfig by lazy {
        FeedConfigProvider(settings)
    }

    val saveConfig by lazy {
        SaveConfigProvider(settings)
    }

    val uiConfig by lazy {
        UiConfigProvider(settings)
    }

    val playbackComponentBlockConfig by lazy {
        PlaybackComponentBlockConfigProvider(
            settings,
            gates = mapOf(
                FeatureGate.HIDDEN to {
                    miscConfig.hiddenFeatureEnabled
                })
        )
    }

    fun init(context: Context) {
        settings = FastKVStorage.open(
            context.filesDir.absolutePath + "/fastkv/",
            "douyinenhancer_prefs"
        )
        module = FastKVStorage.open(
            context.filesDir.absolutePath + "/fastkv/",
            "douyinenhancer_module"
        )
    }
}

