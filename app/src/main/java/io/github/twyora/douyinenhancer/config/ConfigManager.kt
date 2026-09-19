package io.github.twyora.douyinenhancer.config

import android.content.Context
import io.github.twyora.douyinenhancer.config.kvstorage.FastKVStorage
import io.github.twyora.douyinenhancer.config.kvstorage.IKVStorage
import io.github.twyora.douyinenhancer.config.rule.IRule
import io.github.twyora.douyinenhancer.config.provider.FeedConfigProvider
import io.github.twyora.douyinenhancer.config.provider.MiscConfigProvider
import io.github.twyora.douyinenhancer.config.provider.ModuleConfigProvider
import io.github.twyora.douyinenhancer.config.provider.PlaybackComponentBlockConfigProvider
import io.github.twyora.douyinenhancer.config.provider.RecommendedFeedFilterConfigProvider
import io.github.twyora.douyinenhancer.config.provider.SaveConfigProvider
import io.github.twyora.douyinenhancer.config.provider.UiConfigProvider

object ConfigManager {
    lateinit var settingsStorage: IKVStorage
        private set

    lateinit var moduleStorage: IKVStorage
        private set

    val recommendedFeedFilter by lazy {
        RecommendedFeedFilterConfigProvider(settingsStorage) {
            // read the raw flag directly; do not resolve it via the gated config layer
            IRule.Context(
                settingsStorage.get(
                    MiscConfigProvider.ENABLE_HIDDEN_FEATURES,
                    false
                )
            )
        }
    }

    val module by lazy {
        ModuleConfigProvider(moduleStorage)
    }

    val misc by lazy {
        MiscConfigProvider(settingsStorage)
    }

    val feed by lazy {
        FeedConfigProvider(settingsStorage)
    }

    val save by lazy {
        SaveConfigProvider(settingsStorage)
    }

    val ui by lazy {
        UiConfigProvider(settingsStorage)
    }

    val playbackComponentBlock by lazy {
        PlaybackComponentBlockConfigProvider(settingsStorage) {
            // read the raw flag directly; do not resolve it via the gated config layer
            IRule.Context(
                settingsStorage.get(
                    MiscConfigProvider.ENABLE_HIDDEN_FEATURES,
                    false
                )
            )
        }
    }

    fun init(context: Context) {
        settingsStorage = FastKVStorage.open(
            context.filesDir.absolutePath + "/fastkv/",
            "douyinenhancer_prefs"
        )
        moduleStorage = FastKVStorage.open(
            context.filesDir.absolutePath + "/fastkv/",
            "douyinenhancer_module"
        )
    }
}

