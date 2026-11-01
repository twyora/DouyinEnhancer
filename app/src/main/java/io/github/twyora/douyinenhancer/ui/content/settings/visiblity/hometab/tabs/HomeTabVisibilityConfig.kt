package io.github.twyora.douyinenhancer.ui.content.settings.visiblity.hometab.tabs


data class HomeTabVisibilityItem(
    val key: String,
    val title: Int,
    val checked: Boolean
)

data class HomeTabVisibilityConfig(
    val top: HomeTopTabVisibilityConfig,
    val bottom: HomeBottomTabVisibilityConfig
)
