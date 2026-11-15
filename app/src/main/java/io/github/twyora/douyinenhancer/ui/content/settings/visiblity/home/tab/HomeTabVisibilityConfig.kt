package io.github.twyora.douyinenhancer.ui.content.settings.visiblity.home.tab

data class HomeTabVisibilityItem(val key: String, val title: Int, val checked: Boolean)

data class HomeTabVisibilityConfig(val top: HomeTopTabVisibilityConfig, val bottom: HomeBottomTabVisibilityConfig)
