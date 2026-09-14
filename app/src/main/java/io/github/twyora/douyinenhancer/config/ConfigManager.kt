package io.github.twyora.douyinenhancer.config

import android.content.Context
import io.fastkv.FastKV

// TODO: Consider renaming
object ConfigManager {
    lateinit var settings: KVStorage
        private set

    lateinit var module: KVStorage
        private set

    val recommendedFeedFilterConfig by lazy {
        RecommendedFeedFilterConfigManager(
            settings,
            gates = mapOf(
                FeatureGate.HIDDEN to {
                    miscConfig.hiddenFeatureEnabled
                })
        )
    }

    val moduleConfig by lazy {
        ModuleConfigManager(module)
    }

    val miscConfig by lazy {
        MiscConfigManager(settings)
    }

    val feedConfig by lazy {
        FeedConfigManager(settings)
    }

    val saveConfig by lazy {
        SaveConfigManager(settings)
    }

    val uiConfig by lazy {
        UiConfigManager(settings)
    }

    val playbackComponentBlockConfig by lazy {
        PlaybackComponentBlockConfigManager(
            settings,
            gates = mapOf(
                FeatureGate.HIDDEN to {
                    miscConfig.hiddenFeatureEnabled
                })
        )
    }

    fun init(context: Context) {
        settings = FastKVStorage(
            FastKV.Builder(context, "douyinenhancer_prefs").build()
        )
        module = FastKVStorage(
            FastKV.Builder(context, "douyinenhancer_module").build()
        )
    }
}

