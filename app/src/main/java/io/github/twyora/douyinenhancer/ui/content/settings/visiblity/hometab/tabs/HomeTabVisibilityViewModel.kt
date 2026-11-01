package io.github.twyora.douyinenhancer.ui.content.settings.visiblity.hometab.tabs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.config.provider.HomeTabBlockConfigProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HomeTabVisibilityViewModel : ViewModel() {
    private val topTabCatalog = listOf(
        HomeTabBlockConfigProvider.HIDE_HOMEPAGE_HOT_CONTAINER to R.string.top_tab_hide_homepage_hot_container,
        HomeTabBlockConfigProvider.HIDE_HOMEPAGE_FOLLOW to R.string.top_tab_hide_homepage_follow,
        HomeTabBlockConfigProvider.HIDE_HOMEPAGE_FAMILIAR_TOP to R.string.top_tab_hide_homepage_familiar,
        HomeTabBlockConfigProvider.HIDE_HOMEPAGE_MALL_TOP to R.string.top_tab_hide_homepage_mall,
        HomeTabBlockConfigProvider.HIDE_HOMEPAGE_GROUPON to R.string.top_tab_hide_homepage_groupon,
        HomeTabBlockConfigProvider.HIDE_HOMEPAGE_TABLIVE to R.string.top_tab_hide_homepage_tablive,
        HomeTabBlockConfigProvider.HIDE_HOMEPAGE_PAD_HOT to R.string.top_tab_hide_homepage_pad_hot,
        HomeTabBlockConfigProvider.HIDE_HOMEPAGE_NEARBY to R.string.top_tab_hide_homepage_nearby,
        HomeTabBlockConfigProvider.HIDE_HOMEPAGE_HANGOUT to R.string.top_tab_hide_homepage_hangout,
        HomeTabBlockConfigProvider.HIDE_HOMEPAGE_MEDIUMVIDEO to R.string.top_tab_hide_homepage_mediumvideo,
    )

    private val topTabVisibilityItems = combine(
        topTabCatalog.map {
            it.first
        }.map {
            ConfigManager.homeTab.kvConfig.observe(it, false)
        }
    ) { checkedValues ->
        topTabCatalog.mapIndexed { index, (key, title) ->
            HomeTabVisibilityItem(key, title, checkedValues[index])
        }
    }

    private val bottomCatalog: List<Pair<String, Int>> = listOf(
        HomeTabBlockConfigProvider.HIDE_HOMEPAGE_HOME to R.string.bottom_tab_hide_homepage_home,
        HomeTabBlockConfigProvider.HIDE_HOMEPAGE_MALL_BOTTOM to R.string.bottom_tab_hide_homepage_mall,
        HomeTabBlockConfigProvider.HIDE_HOMEPAGE_FAMILIAR_BOTTOM to R.string.bottom_tab_hide_homepage_familiar,
        HomeTabBlockConfigProvider.HIDE_HOMEPAGE_PUBLISH to R.string.bottom_tab_hide_homepage_publish,
        HomeTabBlockConfigProvider.HIDE_HOMEPAGE_NOTIFICATION to R.string.bottom_tab_hide_homepage_notification,
        HomeTabBlockConfigProvider.HIDE_HOMEPAGE_PROFILE to R.string.bottom_tab_hide_homepage_profile,
    )

    private val bottomTabVisibilityItems = combine(
        bottomCatalog.map {
            it.first
        }.map {
            ConfigManager.homeTab.kvConfig.observe(it, false)
        }
    ) { checkedValues ->
        bottomCatalog.mapIndexed { index, (key, title) ->
            HomeTabVisibilityItem(key, title, checkedValues[index])
        }
    }

    private val topTabConfig = combine(
        ConfigManager.homeTab.topMainSwitch.observe(),
        topTabVisibilityItems
    ) { topMain, topItems ->
        HomeTopTabVisibilityConfig(topMain, topItems)
    }

    private val bottomTabConfig = combine(
        ConfigManager.homeTab.bottomMainSwitch.observe(),
        bottomTabVisibilityItems
    ) { bottomMain, bottomItems ->
        HomeBottomTabVisibilityConfig(bottomMain, bottomItems)
    }

    val tabConfig: StateFlow<HomeTabVisibilityConfig> = combine(
        topTabConfig,
        bottomTabConfig
    ) { topTabConfig, bottomTabConfig ->
        HomeTabVisibilityConfig(
            top = topTabConfig,
            bottom = bottomTabConfig,
        )
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeTabVisibilityConfig(
            top = HomeTopTabVisibilityConfig(
                ConfigManager.homeTab.topMainSwitch.defValue,
                topTabCatalog.map { (key, title) ->
                    HomeTabVisibilityItem(key, title, false)
                },
            ),
            bottom = HomeBottomTabVisibilityConfig(
                ConfigManager.homeTab.kvConfig.get(HomeTabBlockConfigProvider.BOTTOM_MAIN_SWITCH, false),
                bottomCatalog.map { (key, title) ->
                    HomeTabVisibilityItem(key, title, false)
                },
            ),
        )
    )

    fun onTopMainSwitchChange(checked: Boolean) {
        ConfigManager.homeTab.topMainSwitch.value = checked
    }

    fun onBottomMainSwitchChange(checked: Boolean) {
        ConfigManager.homeTab.bottomMainSwitch.value = checked
    }

    fun onSwitchChange(key: String, checked: Boolean) {
        ConfigManager.homeTab.kvConfig.put(key, checked)
    }
}
