package io.github.twyora.douyinenhancer.config

import io.github.twyora.douyinenhancer.config.kvstorage.IKVStorage

class MiscConfigProvider(kvConfig: IKVStorage) : AbsConfigProvider(kvConfig) {
    var hiddenFeatureEnabled by property(ENABLE_HIDDEN_FEATURES, false)

    companion object {
        const val ENABLE_HIDDEN_FEATURES = "enable_hidden_features"
    }
}
