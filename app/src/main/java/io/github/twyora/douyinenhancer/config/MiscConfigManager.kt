package io.github.twyora.douyinenhancer.config

class MiscConfigManager(kvConfig: KVStorage) : ConfigProvider(kvConfig) {
    var hiddenFeatureEnabled by property(ENABLE_HIDDEN_FEATURES, false)

    companion object {
        const val ENABLE_HIDDEN_FEATURES = "enable_hidden_features"
    }
}
